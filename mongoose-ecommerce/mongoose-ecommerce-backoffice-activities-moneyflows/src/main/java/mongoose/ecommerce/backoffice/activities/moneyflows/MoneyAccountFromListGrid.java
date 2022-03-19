package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.shared.orm.entity.Entity;
import javafx.collections.ObservableList;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dan Newman
 */
public class MoneyAccountFromListGrid extends MoneyAccountListGrid {

    public MoneyAccountFromListGrid(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        super(moneyAccountPanes, moneyFlowArrowViews);
    }

    @Override
    protected Set<MoneyAccount> getAccountsConnectedByFlow(MoneyAccount selectedMoneyAccount, List<MoneyFlow> moneyFlows) {
        return moneyFlows.stream()
                .filter(moneyFlow -> moneyFlow.getToMoneyAccount().equals(selectedMoneyAccount))
                .map(MoneyFlow::getFromMoneyAccount)
                .collect(Collectors.toSet());
    }

    @Override
    protected void populateInsertEntity(MoneyFlow insertEntity, MoneyAccount selectedMoneyAccount, MoneyAccount otherAccount) {
        insertEntity.setFromMoneyAccount(otherAccount);
        insertEntity.setToMoneyAccount(selectedMoneyAccount);
    }

    @Override
    protected Entity findEntityToDelete(List<MoneyFlow> moneyFlows, MoneyAccount selectedMoneyAccount, MoneyAccount otherAccount) {
        return moneyFlows.stream()
                .filter(moneyFlow -> moneyFlow.getFromMoneyAccount().equals(otherAccount) &&
                        moneyFlow.getToMoneyAccount().equals(selectedMoneyAccount))
                .findAny()
                .orElse(null);
    }

}
