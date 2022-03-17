package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.framework.client.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.framework.shared.orm.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import mongoose.base.shared.entities.MoneyAccount;

public class MoneyFlowsPresentationModel extends EventDependentGenericTablePresentationModel implements
        HasGroupVisualResultProperty,
        HasMasterVisualResultProperty,
        HasSelectedMasterProperty<MoneyAccount> {

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> masterVisualResultProperty() { return masterVisualResultProperty; }

    private final ObjectProperty<MoneyAccount> selectedMasterProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<MoneyAccount> selectedMasterProperty() { return selectedMasterProperty; }

}
