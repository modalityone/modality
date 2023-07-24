package one.modality.catering.backoffice.activities.diningareas;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

/**
 * @author Bruno Salmon
 */
final class DiningAreasPresentationModel extends EventDependentGenericTablePresentationModel {

  private final ObjectProperty<VisualResult> sittingVisualResultProperty =
      new SimpleObjectProperty<>();

  public ObjectProperty<VisualResult> sittingVisualResultProperty() {
    return sittingVisualResultProperty;
  }

  private final ObjectProperty<VisualResult> rulesVisualResultProperty =
      new SimpleObjectProperty<>();

  public ObjectProperty<VisualResult> rulesVisualResultProperty() {
    return rulesVisualResultProperty;
  }

  private final ObjectProperty<VisualSelection> rulesVisualSelectionProperty =
      new SimpleObjectProperty<>();

  public ObjectProperty<VisualSelection> rulesVisualSelectionProperty() {
    return rulesVisualSelectionProperty;
  }
}
