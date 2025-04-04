// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.util;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all plugin panel factories.
 */
public class PanelRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PanelRegistry.class);
  /**
   * The registered factories.
   */
  private final List<PluggablePanelFactory> factories = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param factories The factories.
   */
  @Inject
  public PanelRegistry(Set<PluggablePanelFactory> factories) {
    requireNonNull(factories, "factories");

    // Auto-detect generic client panel factories.
    for (PluggablePanelFactory factory : factories) {
      LOG.info("Setting up plugin panel factory: {}", factory.getClass().getName());
      this.factories.add(factory);
    }
    if (this.factories.isEmpty()) {
      LOG.info("No plugin panel factories found.");
    }
  }

  /**
   * Returns the registered factories.
   *
   * @return The registered factories.
   */
  public List<PluggablePanelFactory> getFactories() {
    return new ArrayList<>(factories);
  }
}
