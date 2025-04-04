// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.draw;

import static javax.swing.Action.SMALL_ICON;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.TOOLBAR_PATH;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * ToFrontAction.
 *
 * @author Werner Randelshofer
 */
public class BringToFrontAction
    extends
      AbstractSelectedAction {

  /**
   * This action's ID.
   */
  public static final String ID = "edit.bringToFront";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  @SuppressWarnings("this-escape")
  public BringToFrontAction(DrawingEditor editor) {
    super(editor);

    putValue(NAME, BUNDLE.getString("bringToFrontAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("bringToFrontAction.shortDescription"));
    putValue(SMALL_ICON, ImageDirectory.getImageIcon("/toolbar/object-order-front.png"));

    updateEnabledState();
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    final DrawingView view = getView();
    final Collection<Figure> figures = new ArrayList<>(view.getSelectedFigures());
    bringToFront(view, figures);
    fireUndoableEditHappened(new AbstractUndoableEdit() {
      @Override
      public String getPresentationName() {
        return ResourceBundleUtil.getBundle(TOOLBAR_PATH)
            .getString("bringToFrontAction.undo.presentationName");
      }

      @Override
      public void redo()
          throws CannotRedoException {
        super.redo();
        BringToFrontAction.bringToFront(view, figures);
      }

      @Override
      public void undo()
          throws CannotUndoException {
        super.undo();
        SendToBackAction.sendToBack(view, figures);
      }
    });
  }

  public static void bringToFront(DrawingView view, Collection<Figure> figures) {
    Drawing drawing = view.getDrawing();

    for (Figure figure : drawing.sort(figures)) {
      drawing.bringToFront(figure);
    }
  }
}
