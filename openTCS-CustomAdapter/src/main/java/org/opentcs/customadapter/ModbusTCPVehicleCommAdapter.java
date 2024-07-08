package org.opentcs.customadapter;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;

public class ModbusTCPVehicleCommAdapter
    extends
      CustomVehicleCommAdapter {

  private static final Map<String, ModbusTCPVehicleCommAdapter.ModbusRegister> REGISTER_MAP
      = new HashMap<>();
  private static final Logger LOG = Logger.getLogger(ModbusTCPVehicleCommAdapter.class.getName());
  private final String host;
  private final int port;
  private boolean isConnected;
  private int maxVelocity;
  private int currentVelocity;
  private ModbusTcpMaster master;
  private VelocityController velocityController;

  /**
   * Initializes a new instance of ModbusTCPVehicleCommAdapter.
   *
   * @param processModel The process model associated with the vehicle.
   * @param rechargeOperation The name of the recharge operation.
   * @param commandsCapacity The maximum capacity for storing commands.
   * @param executor The executor for scheduled tasks.
   * @param host The host address for the TCP connection.
   * @param port The port number for the TCP connection.
   */
  @SuppressWarnings("checkstyle:TodoComment")
  public ModbusTCPVehicleCommAdapter(
      VehicleProcessModel processModel,
      String rechargeOperation,
      int commandsCapacity,
      @KernelExecutor
      ScheduledExecutorService executor,
      String host,
      int port
  ) {
    super(processModel, rechargeOperation, commandsCapacity, executor);
    this.host = host;
    this.port = port;
    this.isConnected = false;
    // this.maxVelocity = processModel
    this.currentVelocity = 0;

    // Initialize VelocityController with default values unit: meter
    double maxAcceleration = 1.6;
    double maxDeceleration = -1.6;
    double maxFwdVelocity = 2.4;
    double maxRevVelocity = -2.4;
    this.velocityController = new VelocityController(
        maxAcceleration, maxDeceleration, maxFwdVelocity, maxRevVelocity
    );
  }

  @Override
  @Nonnull
  public CustomProcessModel getProcessModel() {
    return (CustomProcessModel) super.getProcessModel();
  }

  @Override
  protected void sendSpecificCommand(MovementCommand cmd) {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot send command.");
      return;
    }

    // Set destination
    int destination = Integer.parseInt(cmd.getStep().getDestinationPoint().getName());
    sendModbusCommand("SET_DESTINATION", destination);

    // Set direction (assume 0 for forward, 1 for backward)
    int direction = cmd.getStep().getVehicleOrientation() == Vehicle.Orientation.FORWARD ? 0 : 1;
    sendModbusCommand("SET_DIRECTION", direction);

    // Set speed
    double speed = velocityController.getCurrentVelocity();
    // TODO: overload a double type parameter version of sendModbusCommand for speed.
    sendModbusCommand("SET_SPEED", (int) speed);

    // Start command
    sendModbusCommand("SET_COMMAND", 1);  // Assume 1 means start

    // Verify commands were written correctly
    verifyModbusCommands();
  }

  private void sendModbusCommand(String command, int value) {
    ModbusRegister register = REGISTER_MAP.get(command);
    if (register == null || register.function() != ModbusFunction.WRITE_MULTIPLE_REGISTERS) {
      LOG.warning("Invalid command or function: " + command);
      return;
    }

    ByteBuf buffer = Unpooled.buffer(2);
    buffer.writeShort(value);

    sendModbusRequest(new WriteMultipleRegistersRequest(register.address(), 1, buffer))
        .thenAccept(response -> LOG.info(command + " set successfully"))
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to set " + command, throwable);
          return null;
        });
  }

  private void verifyModbusCommands() {
    ModbusRegister destinationRegister = REGISTER_MAP.get("SET_DESTINATION");
    sendModbusRequestAndHandleResponse(destinationRegister);
  }

  private void sendModbusRequestAndHandleResponse(ModbusRegister register) {
    sendModbusRequest(new ReadHoldingRegistersRequest(register.address(), 1))
        .thenAccept(response -> handleSuccessResponse(response, register))
        .exceptionally(throwable -> handleErrorResponse(throwable, register));
  }

  private void handleSuccessResponse(ModbusResponse response, ModbusRegister register) {
    if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
      ByteBuf buffer = holdingRegistersResponse.getRegisters();
      try {
        if (buffer.readableBytes() >= 2) {
          int readValue = buffer.readShort();
          LOG.info("Verified " + register.function().name() + ": " + readValue);
        }
        else {
          LOG.warning(
              "Insufficient data in response for register: " + register.function().name()
          );
        }
      }
      finally {
        buffer.release();
      }
    }
    else {
      LOG.warning("Unexpected response type: " + response.getClass().getSimpleName());
    }
  }

  private Void handleErrorResponse(Throwable throwable, ModbusRegister register) {
    LOG.log(Level.SEVERE, "Failed to verify " + register.function().name(), throwable);
    return null;
  }

  @Override
  protected boolean performConnection() {
    LOG.info("Connecting to Modbus TCP server at " + host + ":" + port);
    ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
        .setPort(port)
        .build();

    return CompletableFuture.supplyAsync(() -> new ModbusTcpMaster(config))
        .thenCompose(newMaster -> {
          this.master = newMaster;
          return newMaster.connect();
        })
        .thenRun(() -> {
          this.isConnected = true;
          LOG.info("Successfully connected to Modbus TCP server");
          getProcessModel().setCommAdapterConnected(true);
        })
        .exceptionally(ex -> {
          LOG.log(Level.SEVERE, "Failed to connect to Modbus TCP server", ex);
          this.isConnected = false;
          return null;
        })
        .isDone();
  }

  @Override
  protected boolean performDisconnection() {
    LOG.info("Disconnecting from Modbus TCP server");
    if (master != null) {
      return master.disconnect()
          .thenRun(() -> {
            LOG.info("Successfully disconnected from Modbus TCP server");
            this.isConnected = false;
            getProcessModel().setCommAdapterConnected(false);
            this.master = null;
          })
          .exceptionally(ex -> {
            LOG.log(Level.SEVERE, "Failed to disconnect from Modbus TCP server", ex);
            return null;
          })
          .isDone();
    }
    return true;
  }

  @Override
  protected boolean isVehicleConnected() {
    return isConnected && master != null;
  }

  @Override
  protected void updateVehiclePosition() {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update position.");
      return;
    }

    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("POSITION").address(), 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int position = buffer.readShort();
                getProcessModel().setPosition(String.valueOf(position));
                LOG.info("Updated vehicle position: " + position);
              }
              else {
                LOG.warning("Insufficient data in response for POSITION register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for POSITION: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read vehicle position", throwable);
          return null;
        });
  }

  @Override
  protected void updateVehicleState() {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update state.");
      return;
    }

    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("STATUS").address(), 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int state = buffer.readShort();
                Vehicle.State newState = mapModbusStateToVehicleState(state);
                getProcessModel().setState(newState);
              }
              else {
                LOG.warning("Insufficient data in response for STATUS register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for STATUS: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read vehicle state", throwable);
          return null;
        });

    // Read current speed from Modbus (register 303)
    sendModbusRequest(new ReadHoldingRegistersRequest(303, 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int currentSpeed = buffer.readShort();
                velocityController.setCurrentVelocity(currentSpeed);
                LOG.info("Current vehicle speed: " + currentSpeed);
              }
              else {
                LOG.warning("Insufficient data in response for SPEED register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for SPEED: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read current speed", throwable);
          return null;
        });
  }

  private Vehicle.State mapModbusStateToVehicleState(int modbusState) {
    return switch (modbusState) {
      case 0 -> Vehicle.State.IDLE;
      case 1 -> Vehicle.State.EXECUTING;
      case 2 -> Vehicle.State.CHARGING;
      default -> Vehicle.State.UNKNOWN;
    };
  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      com.digitalpetri.modbus.requests.ModbusRequest request
  ) {
    return master.sendRequest(request, 0);
  }

  @Nonnull
  @Override
  public ExplainedBoolean canProcess(@Nonnull
  org.opentcs.data.order.TransportOrder order) {
    return new ExplainedBoolean(true, "ModbusTCP adapter can process all orders.");
  }

  @Override
  public void processMessage(@Nullable
  Object message) {
    LOG.info("Received message: " + message);
    // Implement specific message processing logic
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new CustomProcessModelTO()
        .setCustomProperty(getProcessModel().getCustomProperty());
  }

  /**
   * Updates the vehicle's speed.
   *
   * @param newSpeed The new speed value to set.
   */
  // Add a method to update the vehicle's speed
  public void updateVehicleSpeed(int newSpeed) {
    velocityController.setCurrentVelocity(newSpeed);
    // TODO: overload a double type parameter version of sendModbusCommand for speed.
    sendModbusCommand("SET_SPEED", (int) velocityController.getCurrentVelocity());
  }

  static {
    // OHT movement handshake position - status (0x04)
    REGISTER_MAP.put("HEART_BIT", new ModbusRegister(300, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("DIRECTION", new ModbusRegister(301, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("FORK", new ModbusRegister(302, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("SPEED", new ModbusRegister(303, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("OBSTACLE", new ModbusRegister(304, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("STATUS", new ModbusRegister(305, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("ERROR_CODE", new ModbusRegister(306, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("DESTINATION", new ModbusRegister(308, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("MARK_NO", new ModbusRegister(309, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("POSITION", new ModbusRegister(310, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("IO_IN", new ModbusRegister(318, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("IO_OUT", new ModbusRegister(319, ModbusFunction.READ_INPUT_REGISTERS));

    // OHT movement handshake position - command (0x03, 0x10)
    REGISTER_MAP.put(
        "SET_HEART_BIT", new ModbusRegister(300, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_DIRECTION", new ModbusRegister(301, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_FORK", new ModbusRegister(
            302,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_SPEED", new ModbusRegister(
            303,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_OBSTACLE", new ModbusRegister(304, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_COMMAND", new ModbusRegister(305, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_MODE", new ModbusRegister(
            306,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_DESTINATION", new ModbusRegister(308, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_JOG_MOVE", new ModbusRegister(312, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
  }

  private enum ModbusFunction {
//    READ_COILS(0x01),
//    READ_DISCRETE_INPUTS(0x02),
//    READ_HOLDING_REGISTERS(0x03),
    READ_INPUT_REGISTERS(0x04),
//    WRITE_SINGLE_COIL(0x05),
//    WRITE_SINGLE_REGISTER(0x06),
//    WRITE_MULTIPLE_COILS(0x0F),
    WRITE_MULTIPLE_REGISTERS(0x10);

    private final int functionCode;

    ModbusFunction(int functionCode) {
      this.functionCode = functionCode;
    }

    public int getFunctionCode() {
      return functionCode;
    }
  }

  private record ModbusRegister(int address, ModbusFunction function) {
  }

}
