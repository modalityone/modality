package org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.shared.operation.HasOperationExecutor;
import dev.webfx.stack.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyAccount;

public final class EditMoneyAccountRequest implements HasOperationCode,
        HasOperationExecutor<EditMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "EditMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final Pane parentContainer;

    public EditMoneyAccountRequest(MoneyAccount moneyAccount, Pane parentContainer) {
        this.moneyAccount = moneyAccount;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public MoneyAccount getMoneyAccount() { return moneyAccount; }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditMoneyAccountRequest, Void> getOperationExecutor() {
        return EditMoneyAccountExecutor::executeRequest;
    }
}
