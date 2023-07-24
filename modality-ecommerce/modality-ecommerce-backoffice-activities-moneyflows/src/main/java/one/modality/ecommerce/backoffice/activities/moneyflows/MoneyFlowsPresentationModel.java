package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

/**
 * @author Dan Newman
 */
public class MoneyFlowsPresentationModel extends EventDependentGenericTablePresentationModel {

    private final ObjectProperty<VisualResult> moneyAccountsVisualResultProperty =
            new SimpleObjectProperty<>();

    public ObjectProperty<VisualResult> moneyAccountsVisualResultProperty() {
        return moneyAccountsVisualResultProperty;
    }

    private final ObjectProperty<VisualSelection> moneyAccountsVisualSelectionProperty =
            new SimpleObjectProperty<>();

    public ObjectProperty<VisualSelection> moneyAccountsVisualSelectionProperty() {
        return moneyAccountsVisualSelectionProperty;
    }

    private final ObjectProperty<VisualResult> moneyFlowsVisualResultProperty =
            new SimpleObjectProperty<>();

    public ObjectProperty<VisualResult> moneyFlowsVisualResultProperty() {
        return moneyFlowsVisualResultProperty;
    }

    private final ObjectProperty<VisualSelection> moneyFlowsVisualSelectionProperty =
            new SimpleObjectProperty<>();

    public ObjectProperty<VisualSelection> moneyFlowsVisualSelectionProperty() {
        return moneyFlowsVisualSelectionProperty;
    }
}
