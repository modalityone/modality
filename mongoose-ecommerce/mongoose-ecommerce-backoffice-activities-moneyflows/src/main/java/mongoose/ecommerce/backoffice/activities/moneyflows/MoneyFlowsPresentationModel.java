package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.framework.client.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyFlowsPresentationModel extends EventDependentGenericTablePresentationModel {

    private final ObjectProperty<VisualResult> moneyAccountsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> moneyAccountsVisualResultProperty() { return moneyAccountsVisualResultProperty; }

    private final ObjectProperty<VisualSelection> moneyAccountsVisualSelectionProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualSelection> moneyAccountsVisualSelectionProperty() { return moneyAccountsVisualSelectionProperty; }

    private final ObjectProperty<VisualResult> moneyFlowsVisualResultProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualResult> moneyFlowsVisualResultProperty() { return moneyFlowsVisualResultProperty; }

    private final ObjectProperty<VisualSelection> moneyFlowsVisualSelectionProperty = new SimpleObjectProperty<>();
    public ObjectProperty<VisualSelection> moneyFlowsVisualSelectionProperty() { return moneyFlowsVisualSelectionProperty; }
}
