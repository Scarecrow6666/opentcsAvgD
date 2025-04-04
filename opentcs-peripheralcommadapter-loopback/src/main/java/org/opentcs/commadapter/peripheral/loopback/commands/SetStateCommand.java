// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback.commands;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.commadapter.peripheral.loopback.LoopbackPeripheralCommAdapter;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;

/**
 * A command to set the peripheral device's state.
 */
public class SetStateCommand
    implements
      PeripheralAdapterCommand {

  /**
   * The peripheral device state to set.
   */
  private final PeripheralInformation.State state;

  /**
   * Creates a new instance.
   *
   * @param state The peripheral device state to set.
   */
  public SetStateCommand(
      @Nonnull
      PeripheralInformation.State state
  ) {
    this.state = requireNonNull(state, "state");
  }

  @Override
  public void execute(PeripheralCommAdapter adapter) {
    if (!(adapter instanceof LoopbackPeripheralCommAdapter)) {
      return;
    }

    LoopbackPeripheralCommAdapter loopbackAdapter = (LoopbackPeripheralCommAdapter) adapter;
    loopbackAdapter.updateState(state);
  }
}
