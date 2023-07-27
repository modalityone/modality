package one.modality.catering.backoffice2018.activities.diningareas;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;

/**
 * @author Bruno Salmon
 */
final class DiningAreasPresentationModel extends EventDependentGenericTablePresentationModel {

    private final ObjectProperty<VisualResult> sittingVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> sittingVisualResultProperty() { return sittingVisualResultProperty; }

    private final ObjectProperty<VisualResult> rulesVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> rulesVisualResultProperty() { return rulesVisualResultProperty; }

    private final ObjectProperty<VisualSelection> rulesVisualSelectionProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualSelection> rulesVisualSelectionProperty() { return rulesVisualSelectionProperty; }

}
