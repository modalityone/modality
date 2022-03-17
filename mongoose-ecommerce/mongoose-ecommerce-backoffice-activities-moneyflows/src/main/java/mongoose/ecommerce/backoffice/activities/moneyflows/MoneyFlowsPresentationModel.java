package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.framework.client.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.framework.shared.orm.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

public class MoneyFlowsPresentationModel extends EventDependentGenericTablePresentationModel implements
        HasGroupVisualResultProperty,
        HasMasterVisualResultProperty,
        HasSelectedMasterProperty<Entity> {

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> masterVisualResultProperty() { return masterVisualResultProperty; }

    private final ObjectProperty<Entity> selectedMasterProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Entity> selectedMasterProperty() { return selectedMasterProperty; }
}
