package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.MoneyFlow;

import java.util.Collections;
import java.util.List;

public final class DeleteMoneyAccountRequest
        implements HasOperationCode, HasOperationExecutor<DeleteMoneyAccountRequest, Void> {

    private static final String OPERATION_CODE = "DeleteMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final List<MoneyFlow> moneyFlows;
    private final Pane parentContainer;

    public DeleteMoneyAccountRequest(
            MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows, Pane parentContainer) {
        this.moneyAccount = moneyAccount;
        this.moneyFlows = moneyFlows;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    public List<MoneyFlow> getMoneyFlows() {
        return Collections.unmodifiableList(moneyFlows);
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteMoneyAccountRequest, Void> getOperationExecutor() {
        return DeleteMoneyAccountExecutor::executeRequest;
    }
}
