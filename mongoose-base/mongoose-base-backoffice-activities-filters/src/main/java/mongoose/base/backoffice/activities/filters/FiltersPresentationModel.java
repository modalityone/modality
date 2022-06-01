package mongoose.base.backoffice.activities.filters;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

/**
 * @author Ben Vickers
 */
public class FiltersPresentationModel extends EventDependentGenericTablePresentationModel {

    private final ObjectProperty<VisualResult> filtersVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> filtersVisualResultProperty() { return filtersVisualResultProperty; }

    private final ObjectProperty<VisualSelection> filtersVisualSelectionProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualSelection> filtersVisualSelectionProperty() { return filtersVisualSelectionProperty; }

    private final ObjectProperty<VisualResult> fieldsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> fieldsVisualResultProperty() { return fieldsVisualResultProperty; }

    private final ObjectProperty<VisualSelection> fieldsVisualSelectionProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualSelection> fieldsVisualSelectionProperty() { return fieldsVisualSelectionProperty; }


    private final ObjectProperty<VisualResult> filterFieldsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> filterFieldsVisualResultProperty() { return filterFieldsVisualResultProperty; }
}
