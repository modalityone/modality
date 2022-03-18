package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.collections.ObservableList;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

/**
 * @author Dan Newman
 */
public class MoneyAccountFromListGrid extends MoneyAccountListGrid {

    public MoneyAccountFromListGrid(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        super(moneyAccountPanes, moneyFlowArrowViews);
    }

    @Override
    protected MoneyAccount getAccountFromFlow(MoneyFlow moneyFlow) {
        return moneyFlow.getFromMoneyAccount();
    }
}
