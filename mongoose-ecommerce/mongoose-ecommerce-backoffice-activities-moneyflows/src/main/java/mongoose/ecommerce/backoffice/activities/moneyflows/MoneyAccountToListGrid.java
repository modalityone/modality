package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.collections.ObservableList;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

/**
 * @author Dan Newman
 */
public class MoneyAccountToListGrid extends MoneyAccountListGrid {

    public MoneyAccountToListGrid(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        super(moneyAccountPanes, moneyFlowArrowViews);
    }

    @Override
    protected MoneyAccount getAccountFromFlow(MoneyFlow moneyFlow) {
        return moneyFlow.getToMoneyAccount();
    }
}
