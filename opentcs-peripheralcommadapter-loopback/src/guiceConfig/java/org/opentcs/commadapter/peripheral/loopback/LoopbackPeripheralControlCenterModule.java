// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * Loopback adapter-specific Gucie configuration for the Kernel Control Center.
 */
public class LoopbackPeripheralControlCenterModule
    extends
      ControlCenterInjectionModule {

  /**
   * Creates a new instance.
   */
  public LoopbackPeripheralControlCenterModule() {
  }

  @Override
  protected void configure() {
    install(
        new FactoryModuleBuilder().build(LoopbackPeripheralAdapterPanelComponentsFactory.class)
    );

    peripheralCommAdapterPanelFactoryBinder()
        .addBinding().to(LoopbackPeripheralCommAdapterPanelFactory.class);
  }
}
