package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.MoneyAccount;

public final class EditMoneyAccountRequest
        implements HasOperationCode, HasOperationExecutor<EditMoneyAccountRequest, Void> {

    private static final String OPERATION_CODE = "EditMoneyAccount";

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

    public MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditMoneyAccountRequest, Void> getOperationExecutor() {
        return EditMoneyAccountExecutor::executeRequest;
    }
}
