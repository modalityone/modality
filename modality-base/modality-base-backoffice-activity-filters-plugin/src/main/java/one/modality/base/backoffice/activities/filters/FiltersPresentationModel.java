package one.modality.base.backoffice.activities.filters;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;

/**
 * @author Ben Vickers
 */
public class FiltersPresentationModel extends OrganizationDependentGenericTablePresentationModel {

    private final ObjectProperty<VisualResult> filtersVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> filtersVisualResultProperty() { return filtersVisualResultProperty; }

    private final ObjectProperty<VisualSelection> filtersVisualSelectionProperty = VisualSelection.createVisualSelectionProperty();
    public ObjectProperty<VisualSelection> filtersVisualSelectionProperty() { return filtersVisualSelectionProperty; }

    private final ObjectProperty<VisualResult> fieldsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> fieldsVisualResultProperty() { return fieldsVisualResultProperty; }

    private final ObjectProperty<VisualSelection> fieldsVisualSelectionProperty = VisualSelection.createVisualSelectionProperty();
    public ObjectProperty<VisualSelection> fieldsVisualSelectionProperty() { return fieldsVisualSelectionProperty; }

    private final ObjectProperty<VisualResult> filterFieldsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> filterFieldsVisualResultProperty() { return filterFieldsVisualResultProperty; }

    private final ObjectProperty<String> filterClassProperty = new SimpleObjectProperty<>();
    public ObjectProperty<String> filterClassProperty() { return filterClassProperty; }

    private final ObjectProperty<String> fieldsSearchTextProperty = new SimpleObjectProperty<>();
    public ObjectProperty<String> fieldsSearchTextProperty() { return fieldsSearchTextProperty; }
}
