// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.selection.ParkVehicleSelectionFilter;

/**
 * A collection of {@link ParkVehicleSelectionFilter}s.
 */
public class CompositeParkVehicleSelectionFilter
    implements
      ParkVehicleSelectionFilter {

  /**
   * The {@link ParkVehicleSelectionFilter}s.
   */
  private final Set<ParkVehicleSelectionFilter> filters;

  @Inject
  public CompositeParkVehicleSelectionFilter(Set<ParkVehicleSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public Collection<String> apply(Vehicle vehicle) {
    return filters.stream()
        .flatMap(filter -> filter.apply(vehicle).stream())
        .collect(Collectors.toList());
  }
}
