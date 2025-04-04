// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.peripherals.dispatching;

import static org.opentcs.util.Assertions.checkArgument;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opentcs.data.model.Location;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.util.Comparators;

/**
 * The default implementation of {@link JobSelectionStrategy}.
 * Selects a job by applying the following rules:
 * <ul>
 * <li>The location of a job's operation has to match the given location.</li>
 * <li>If this applies to multiple jobs, the oldest one is selected.</li>
 * </ul>
 */
public class DefaultJobSelectionStrategy
    implements
      JobSelectionStrategy {

  /**
   * Creates a new instance.
   */
  public DefaultJobSelectionStrategy() {
  }

  @Override
  public Optional<PeripheralJob> select(Collection<PeripheralJob> jobs, Location location) {
    checkArgument(
        jobs.stream().allMatch(job -> matchesLocation(job, location)),
        "All jobs are expected to match the given location: %s", location.getName()
    );

    return jobs.stream()
        .sorted(Comparators.jobsByAge())
        .findFirst();
  }

  private boolean matchesLocation(PeripheralJob job, Location location) {
    return Objects.equals(job.getPeripheralOperation().getLocation(), location.getReference());
  }
}
