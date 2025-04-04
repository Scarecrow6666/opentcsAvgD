// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.themes;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * Default location theme implementation.
 */
public class DefaultLocationTheme
    implements
      LocationTheme {

  /**
   * The path containing the images.
   */
  private static final String PATH = "/org/opentcs/guing/plugins/themes/symbols/location/";
  /**
   * The available symbols.
   */
  private static final String[] LOCTYPE_REPRESENTATION_SYMBOLS
      = {
          "TransferStation.20x20.png", // 0
          "WorkingStation.20x20.png", // 1
          "ChargingStation.20x20.png", // 2
          "None.20x20.png", // 3
      };
  /**
   * A map of property values to image file names.
   */
  private final Map<LocationRepresentation, Image> symbolMap
      = new EnumMap<>(LocationRepresentation.class);

  /**
   * Creates a new instance.
   */
  public DefaultLocationTheme() {
    initSymbolMap();
  }

  @Override
  @Nonnull
  public Image getImageFor(
      @Nonnull
      LocationRepresentation representation
  ) {
    requireNonNull(representation, "representation");

    return symbolMap.getOrDefault(
        representation,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[3])
    );
  }

  @Override
  @Nonnull
  public Image getImageFor(
      @Nonnull
      Location location,
      @Nonnull
      LocationType locationType
  ) {
    requireNonNull(location, "location");
    requireNonNull(locationType, "locationType");

    LocationRepresentation representation = location.getLayout().getLocationRepresentation();
    if (representation == null || representation == LocationRepresentation.DEFAULT) {
      representation = locationType.getLayout().getLocationRepresentation();
    }
    return getImageFor(representation);
  }

  private void initSymbolMap() {
    // NONE: A location without further description
    symbolMap.put(
        LocationRepresentation.NONE,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[3])
    );

    // LOAD_TRANSFER_GENERIC: A generic location for vehicle load transfers.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_GENERIC,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );
    // LOAD_TRANSFER_ALT_1: A location for vehicle load transfers, variant 1.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_ALT_1,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );
    // LOAD_TRANSFER_ALT_2: A location for vehicle load transfers, variant 2.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_ALT_2,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );
    // LOAD_TRANSFER_ALT_3: A location for vehicle load transfers, variant 3.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_ALT_3,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );
    // LOAD_TRANSFER_ALT_4: A location for vehicle load transfers, variant 4.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_ALT_4,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );
    // LOAD_TRANSFER_ALT_5: A location for vehicle load transfers, variant 5.
    symbolMap.put(
        LocationRepresentation.LOAD_TRANSFER_ALT_5,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[0])
    );

    // WORKING_GENERIC: A location for some generic processing, generic variant.
    symbolMap.put(
        LocationRepresentation.WORKING_GENERIC,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[1])
    );
    // WORKING_ALT_1: A location for some generic processing, variant 1.
    symbolMap.put(
        LocationRepresentation.WORKING_ALT_1,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[1])
    );
    // WORKING_ALT_2: A location for some generic processing, variant 2.
    symbolMap.put(
        LocationRepresentation.WORKING_ALT_2,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[1])
    );

    // RECHARGE_GENERIC: A location for recharging a vehicle, generic variant.
    symbolMap.put(
        LocationRepresentation.RECHARGE_GENERIC,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[2])
    );
    // RECHARGE_ALT_1: A location for recharging a vehicle, variant 1.
    symbolMap.put(
        LocationRepresentation.RECHARGE_ALT_1,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[2])
    );
    // RECHARGE_ALT_2: A location for recharging a vehicle, variant 2.
    symbolMap.put(
        LocationRepresentation.RECHARGE_ALT_2,
        loadImageFromPath(LOCTYPE_REPRESENTATION_SYMBOLS[2])
    );
  }

  private Image loadImageFromPath(String fileName) {
    return loadImage(PATH + fileName);
  }

  private Image loadImage(String fileName) {
    requireNonNull(fileName, "fileName");

    URL url = getClass().getResource(fileName);
    if (url == null) {
      throw new IllegalArgumentException("Invalid image file name " + fileName);
    }
    try {
      return ImageIO.read(url);
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Exception loading image", exc);
    }
  }
}
