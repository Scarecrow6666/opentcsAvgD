/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;

import java.util.ResourceBundle;

/**
 */
public class MultipleDifferentValues
    implements
      AcceptableInvalidValue {

  /**
   * Creates a new instance.
   */
  public MultipleDifferentValues() {
  }

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("multipleDifferentValues.description");
  }

  @Override
  public String getHelptext() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("multipleDifferentValues.helptext");
  }
}
