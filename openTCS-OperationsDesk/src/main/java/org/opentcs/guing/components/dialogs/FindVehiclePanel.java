/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JPanel;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 * Panel to select a Vehicle that will be searched for in the view.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FindVehiclePanel
    extends JPanel {

  /**
   * The list of existing vehicles.
   */
  private final List<VehicleModel> fVehicles;
  /**
   * The view to show the found vehicle in.
   */
  private final OpenTCSDrawingView fDrawingView;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;

  /**
   * Creates a new instance.
   *
   * @param vehicles A list of existing vehicles.
   * @param drawingView The view to show the found vehicle in.
   * @param modelManager The model manager.
   */
  @Inject
  public FindVehiclePanel(@Assisted Collection<VehicleModel> vehicles,
                          @Assisted OpenTCSDrawingView drawingView,
                          ModelManager modelManager) {
    fVehicles = new ArrayList<>(requireNonNull(vehicles, "vehicles"));
    fDrawingView = requireNonNull(drawingView, "drawingView");
    this.modelManager = requireNonNull(modelManager, "modelManager");

    initComponents();

    for (VehicleModel vehicle : vehicles) {
      comboBoxVehicles.addItem(vehicle.getName());
    }
  }

  /**
   * Returns the selected vehicle.
   *
   * @return The selected vehicle.
   */
  public VehicleModel getSelectedVehicle() {
    int index = comboBoxVehicles.getSelectedIndex();

    if (index == -1) {
      return null;
    }

    return fVehicles.get(index);
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    labelVehicles = new javax.swing.JLabel();
    comboBoxVehicles = new javax.swing.JComboBox<>();
    buttonFind = new javax.swing.JButton();

    labelVehicles.setFont(labelVehicles.getFont());
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/operating/dialogs/findVehicle"); // NOI18N
    labelVehicles.setText(bundle.getString("findVehiclePanel.label_vehicles.text")); // NOI18N
    add(labelVehicles);

    comboBoxVehicles.setFont(comboBoxVehicles.getFont());
    add(comboBoxVehicles);

    buttonFind.setFont(buttonFind.getFont());
    buttonFind.setText(bundle.getString("findVehiclePanel.button_find.text")); // NOI18N
    buttonFind.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonFindActionPerformed(evt);
      }
    });
    add(buttonFind);
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON

  /**
   * Starts the search for the vehicle.
   */
    private void buttonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFindActionPerformed
      VehicleModel vehicle = getSelectedVehicle();
      if (vehicle == null) {
        return;
      }

      VehicleFigure figure = (VehicleFigure) modelManager.getModel().getFigure(vehicle);
      if (figure != null) {
        fDrawingView.scrollTo(figure);
      }
    }//GEN-LAST:event_buttonFindActionPerformed

  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonFind;
  private javax.swing.JComboBox<String> comboBoxVehicles;
  private javax.swing.JLabel labelVehicles;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
