package org.opentcs.peripheralcustomadapter;

import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventHandler;

public interface StrategyCreator {
  /**
   * Creates a {@link PeripheralCommunicationAdapter} for a {@link Vehicle} with the given
   * configuration.
   *
   * @param location The reference to the location this adapter is attached to.
   * @param eventHandler The handler used to send events to.
   * @param kernelExecutor The kernel's executor.
   * @param config The Peripheral Device Configuration.
   * @return A new instance of {@link PeripheralCommunicationAdapter}.
   */
  PeripheralCommunicationAdapter createAdapter(
      TCSResourceReference<Location> location,
      EventHandler eventHandler,
      ScheduledExecutorService kernelExecutor,
      PeripheralDeviceConfiguration config
  );
}
