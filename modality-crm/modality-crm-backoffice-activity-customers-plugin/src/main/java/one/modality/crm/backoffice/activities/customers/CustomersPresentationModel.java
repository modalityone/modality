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
    HasLimitProperty,
    HasAccountTypeFilterProperty,
    HasActiveStatusFilterProperty {

    // Master table properties
    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override
    public ObjectProperty<VisualResult> masterVisualResultProperty() {
        return masterVisualResultProperty;
    }

    private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = new SimpleObjectProperty<>();
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

    // Organization filter property

    // Account type filter property (null = all, "frontoffice" or "backoffice")
    private final StringProperty accountTypeFilterProperty = new SimpleStringProperty();
    @Override
    public StringProperty accountTypeFilterProperty() {
        return accountTypeFilterProperty;
    }

    // Active status filter property (null = all, "active" or "inactive")
    private final StringProperty activeStatusFilterProperty = new SimpleStringProperty("active");
    @Override
    public StringProperty activeStatusFilterProperty() {
        return activeStatusFilterProperty;
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
