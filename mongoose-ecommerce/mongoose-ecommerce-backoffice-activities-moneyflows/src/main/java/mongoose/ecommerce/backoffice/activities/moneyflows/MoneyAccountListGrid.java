package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dan Newman
 */
public abstract class MoneyAccountListGrid extends GridPane {

    private final ObservableList<MoneyAccountPane> moneyAccountPanes;
    private final ObjectProperty<MoneyAccount> excludedMoneyAccount = new SimpleObjectProperty<>();
    private final ObservableList<MoneyFlowArrowView> moneyFlowArrowViews;

    public ObjectProperty<MoneyAccount> excludedMoneyAccount() { return excludedMoneyAccount; }

    public MoneyAccountListGrid(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        this.moneyAccountPanes = moneyAccountPanes;
        this.moneyFlowArrowViews = moneyFlowArrowViews;
        moneyAccountPanes.addListener((ListChangeListener<? super MoneyAccountPane>) e -> populate());
        excludedMoneyAccount.addListener(e -> populate());
        moneyFlowArrowViews.addListener((ListChangeListener<? super MoneyFlowArrowView>) e -> populate());
    }

    private void populate() {
        getChildren().clear();

        Set<MoneyAccount> moneyAccountList = moneyFlowArrowViews.stream()
                .map(arrow -> getAccountFromFlow(arrow.moneyFlowProperty().get()))
                .filter(moneyAccount -> !moneyAccount.equals(excludedMoneyAccount.get()))
                .collect(Collectors.toSet());

        moneyAccountPanes.stream()
                .sorted((pane1, pane2) -> pane1.moneyAccountProperty().get().getName().compareToIgnoreCase(pane2.moneyAccountProperty().get().getName()))
                .map(pane -> pane.moneyAccountProperty().get())
                .filter(moneyAccount -> !moneyAccount.equals(excludedMoneyAccount.get()))
                .forEach(moneyAccount -> {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(moneyAccountList.contains(moneyAccount));
                    Label label = new Label(moneyAccount.getName());
                    int rowIndex = getRowCount();
                    add(checkBox, 0, rowIndex);
                    add(label, 1, rowIndex);
                });
    }

    protected abstract MoneyAccount getAccountFromFlow(MoneyFlow moneyFlow);
}
