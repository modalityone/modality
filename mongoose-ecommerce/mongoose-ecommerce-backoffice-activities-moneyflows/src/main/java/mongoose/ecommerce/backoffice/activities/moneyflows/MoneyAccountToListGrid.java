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
public class MoneyAccountToListGrid extends MoneyAccountListGrid {

    public MoneyAccountToListGrid(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        super(moneyAccountPanes, moneyFlowArrowViews);
    }

    @Override
    protected Set<MoneyAccount> getAccountsConnectedByFlow(MoneyAccount selectedMoneyAccount, List<MoneyFlow> moneyFlows) {
        return moneyFlows.stream()
                .filter(moneyFlow -> moneyFlow.getFromMoneyAccount().equals(selectedMoneyAccount))
                .map(MoneyFlow::getToMoneyAccount)
                .collect(Collectors.toSet());
    }

    @Override
    protected void populateInsertEntity(MoneyFlow insertEntity, MoneyAccount selectedMoneyAccount, MoneyAccount otherAccount) {
        insertEntity.setFromMoneyAccount(selectedMoneyAccount);
        insertEntity.setToMoneyAccount(otherAccount);
    }

    @Override
    protected Entity findEntityToDelete(List<MoneyFlow> moneyFlows, MoneyAccount selectedMoneyAccount, MoneyAccount otherAccount) {
        return moneyFlows.stream()
                .filter(moneyFlow -> moneyFlow.getFromMoneyAccount().equals(selectedMoneyAccount) &&
                        moneyFlow.getToMoneyAccount().equals(otherAccount))
                .findAny()
                .orElse(null);
    }

}
