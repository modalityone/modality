package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasLimitProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import javafx.beans.property.*;
import one.modality.base.client.presentationmodel.HasSearchTextProperty;
import one.modality.base.shared.entities.Person;

/**
 * Presentation model for the Customers activity.
 * Manages state and data bindings for customer/person management.
 *
 * @author David Hello
 * @author Claude Code
 */
final class CustomersPresentationModel implements
    HasMasterVisualResultProperty,
    HasMasterVisualSelectionProperty,
    HasSearchTextProperty,
    HasLimitProperty {

    // Master table properties
    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<VisualResult>();
    @Override
    public ObjectProperty<VisualResult> masterVisualResultProperty() {
        return masterVisualResultProperty;
    }

    private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = new SimpleObjectProperty<VisualSelection>();
    @Override
    public ObjectProperty<VisualSelection> masterVisualSelectionProperty() {
        return masterVisualSelectionProperty;
    }

    // Search and limit properties
    private final StringProperty searchTextProperty = new SimpleStringProperty();
    @Override
    public StringProperty searchTextProperty() {
        return searchTextProperty;
    }

    private final IntegerProperty limitProperty = new SimpleIntegerProperty(100);
    @Override
    public IntegerProperty limitProperty() {
        return limitProperty;
    }

    // Selected person property
    private final ObjectProperty<Person> selectedPersonProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Person> selectedPersonProperty() {
        return selectedPersonProperty;
    }

    public Person getSelectedPerson() {
        return selectedPersonProperty.get();
    }

    // Organization filter property
    private final ObjectProperty<Object> organizationIdProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Object> organizationIdProperty() {
        return organizationIdProperty;
    }

    public Object getOrganizationId() {
        return organizationIdProperty.get();
    }

    // Active property for lifecycle management
    private final ObjectProperty<Boolean> activeProperty = new SimpleObjectProperty<>(false);
    public ObjectProperty<Boolean> activeProperty() {
        return activeProperty;
    }

    public void setActive(boolean active) {
        activeProperty.set(active);
    }
}
