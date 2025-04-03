package com.dingxun.adapter.mqtt;

import static java.util.Objects.requireNonNull;

import com.google.gson.*;
import com.google.inject.Inject;
import jakarta.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import javax.validation.constraints.NotNull;
import org.eclipse.paho.client.mqttv3.*;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.*;
import org.opentcs.data.order.*;
import org.opentcs.drivers.vehicle.*;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
/**
 * MQTT通信适配器核心类，负责与MQTT代理通信：
 * - 连接/断开MQTT代理
 * - 发送移动命令（JSON格式）
 * - 订阅主题并处理接收到的消息
 * 继承自BasicVehicleCommAdapter，实现MqttCallback接口
 */
public class MQTTCommAdapter extends BasicVehicleCommAdapter implements MqttCallback {

  private static final Logger LOG = LoggerFactory.getLogger(MQTTCommAdapter.class);
  private final MQTTConfig config;        // MQTT配置（代理地址、主题等）
  private MqttClient client;              // MQTT客户端实例
  private volatile boolean connected;     // 连接状态标志
  private final BasicVehicleProcessModel processModel; // 车辆状态模型
  private VehicleService vehicleService; // 使用OpenTCS官方VehicleService接口
  private  RoutePlanner routePlanner;    // 路径规划器（伪实现）
  /**
   * 构造函数
   * @param vehicle      车辆对象
   * @param config       MQTT配置（从配置文件注入）
   * @param processModel 车辆状态模型
   * @param executor     调度执行器（用于异步任务）
   */
  @Inject
  public MQTTCommAdapter(
      VehicleService vehicleService,
      RoutePlanner routePlanner,
      Vehicle vehicle,
      MQTTConfig config,
      BasicVehicleProcessModel processModel,
      ScheduledExecutorService executor) {
    super(processModel, 1, "MQTT", executor);
    this.config = config;
    this.processModel = processModel;
    this.vehicleService = vehicleService;
    this.routePlanner = routePlanner;
    initialize(); // 初始化逻辑
  }

  /**
   * 连接MQTT代理并订阅命令主题
   * - 代理地址从MQTTConfig中读取（如tcp://localhost:1883）
   * - 客户端ID格式为"OpenTCS-{车辆名称}"
   */
  @Override
  protected void connectVehicle() {
    try {
      client = new MqttClient(
          config.brokerUrl(), // 代理地址，如tcp://192.168.1.100:1883
          "OpenTCS-" + processModel.getVehicle().getName() // 客户端唯一ID
      );
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true); // 启用自动重连
      client.connect(options);

      // 订阅配置中的命令主题（如opentcs/commands）
      client.subscribe(config.commandTopic(), config.qosLevel());
      client.setCallback(this); // 设置消息回调

      connected = true;
      processModel.setCommAdapterConnected(true);
      LOG.info("MQTT Connected to {} on topic {}", config.brokerUrl(), config.commandTopic());
    } catch (MqttException e) {
      LOG.error("MQTT Connection failed", e);
      processModel.setCommAdapterConnected(false);
    }
  }
  @Override
  protected void disconnectVehicle() {
    try {
      if (client != null && client.isConnected()) {
        client.disconnect();
        LOG.info("MQTT Disconnected");
      }
    } catch (MqttException e) {
      LOG.error("Disconnect failed", e);
    } finally {
      connected = false;
      processModel.setCommAdapterConnected(false);
    }
  }

  @Override
  protected boolean isVehicleConnected() {
    return connected;
  }

  /**
   * 发送移动命令到MQTT代理
   * @param cmd 移动命令对象，包含路径信息
   * 命令格式示例：{"dest":"PointA","path":["PointStart","PointA"]}
   */
  @Override
  public void sendCommand(MovementCommand cmd) {
    requireNonNull(cmd, "cmd");

    if (!connected) {
      throw new IllegalStateException("Not connected to MQTT broker");
    }

    try {
      String payload = buildCommandPayload(cmd);
      client.publish(
          config.commandTopic(), // 发布到配置的命令主题
          new MqttMessage(payload.getBytes())
      );
      LOG.debug("Command sent: {}", payload);
    } catch (MqttException e) {
      LOG.error("Command delivery failed", e);
    }
  }

  private String buildCommandPayload(MovementCommand cmd) {
    Route.Step step = cmd.getStep();
    return String.format(
        "{\"dest\":\"%s\",\"path\":[\"%s\",\"%s\"]}",
        step.getDestinationPoint().getName(),
        step.getSourcePoint().getName(),
        step.getDestinationPoint().getName()
    );
  }


  @Override
  public void messageArrived(String topic, MqttMessage message) {
    String payload = new String(message.getPayload());
    LOG.info("收到外部命令: {}", payload);

    try {
      // 解析JSON
      JsonObject cmdJson = JsonParser.parseString(payload).getAsJsonObject();
      String vehicleId = cmdJson.get("vehicleId").getAsString();
      String targetPointName = cmdJson.get("targetPoint").getAsString();

      // 1. 获取车辆和点的引用
      TCSObjectReference<Vehicle> vehicleRef = vehicleService.fetchObject(Vehicle.class, vehicleId).getReference();
      TCSObjectReference<Point> targetPointRef = vehicleService.fetchObject(Point.class, targetPointName).getReference();

      // 2. 获取车辆当前位置（通过Vehicle对象）
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, vehicleId);
      TCSObjectReference<Point> currentPointRef = vehicle.getCurrentPosition();

      // 3. 构建DriveOrder（使用现有接口）
      DriveOrder.Destination destination = new DriveOrder.Destination(targetPointRef).withOperation("MOVE");
      DriveOrder driveOrder = new DriveOrder(destination).withState(DriveOrder.State.PRISTINE);

      // 4. 创建TransportOrder（使用TransportOrder构造函数）
      TransportOrder order = new TransportOrder(
          "TO-" + System.currentTimeMillis(), // 唯一订单名称
          Collections.singletonList(driveOrder)
      );

      // 5. 通过VehicleService分配订单（使用sendCommAdapterMessage）
      vehicleService.sendCommAdapterMessage(vehicleRef, order);

      LOG.info("订单已分配: {}", order.getName());

    } catch (ObjectUnknownException e) {
      LOG.error("对象不存在: {}", e.getMessage());
    } catch (KernelRuntimeException e) {
      LOG.error("内核异常: {}", e.getMessage());
    }
  }


    // 伪方法：从ProcessModel获取当前位置
    private Point getCurrentPositionFromModel(TCSObjectReference<Vehicle> vehicleRef) {
      // 实际实现需通过vehicleService.fetchProcessModel()获取
      return new Point("PointA");
    }

  // 查找车辆引用（伪实现）
  private TCSObjectReference<Vehicle> findVehicleReference(String vehicleId) {
    try {
      return vehicleService.fetchObject(Vehicle.class, vehicleId).getReference();
    } catch (ObjectUnknownException e) {
      return null;
    }
  }


  @Override
  public void connectionLost(Throwable cause) {
    LOG.error("MQTT connection lost", cause);
    processModel.setCommAdapterConnected(false);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // 消息投递完成回调
  }

  @NotNull
  @Override
  public ExplainedBoolean canProcess(
      @NotNull
      TransportOrder order
  ) {
    return null;
  }

  @Override
  public void onVehiclePaused(boolean paused) {

  }

  @Override
  public void processMessage(
      @Nullable
      Object message
  ) {

  }
}
