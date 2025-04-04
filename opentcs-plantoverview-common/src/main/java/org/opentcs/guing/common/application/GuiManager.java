// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * Provides some central services for various parts of the plant overview application.
 */
public interface GuiManager {

  /**
   * Called when an object was selected in the tree view.
   *
   * @param modelComponent The selected object.
   */
  void selectModelComponent(ModelComponent modelComponent);

  /**
   * Called when an additional object was selected in the tree view.
   *
   * @param modelComponent The selected object.
   */
  void addSelectedModelComponent(ModelComponent modelComponent);

  /**
   * Called when an object was removed from the tree view (by user interaction).
   *
   * @param fDataObject The object to be removed.
   * @return Indicates whether the object was really removed from the model.
   */
  boolean treeComponentRemoved(ModelComponent fDataObject);

  /**
   * Notifies about a figure object being selected.
   *
   * @param modelComponent The selected object.
   */
  void figureSelected(ModelComponent modelComponent);

  /**
   * Creates a new, empty model and initializes it.
   */
  void createEmptyModel();

  /**
   * Loads a plant model.
   */
  void loadModel();

  /**
   * Imports a plant model using the given importer.
   *
   * @param importer The importer.
   */
  void importModel(PlantModelImporter importer);

  /**
   * @return
   */
  boolean saveModel();

  /**
   *
   * @return
   */
  boolean saveModelAs();

  /**
   * Exports a plant model using the given exporter.
   *
   * @param exporter The exporter.
   */
  void exportModel(PlantModelExporter exporter);
}
