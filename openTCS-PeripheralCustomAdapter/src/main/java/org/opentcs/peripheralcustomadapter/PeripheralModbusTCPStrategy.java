package org.opentcs.peripheralcustomadapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.util.event.EventHandler;

@Singleton
public class PeripheralModbusTCPStrategy
    implements
      StrategyCreator {

  @Inject
  PeripheralModbusTCPStrategy() {
  }

  @Override
  public PeripheralCommunicationAdapter createAdapter(
      TCSResourceReference<Location> location,
      EventHandler eventHandler,
      ScheduledExecutorService kernelExecutor,
      PeripheralDeviceConfiguration config,
      PeripheralService peripheralService
  ) {
    return new ModbusTCPPeripheralCommunicationAdapter(
        location,
        eventHandler,
        kernelExecutor,
        config.host(),
        config.port(),
        peripheralService
    );
  }
}
