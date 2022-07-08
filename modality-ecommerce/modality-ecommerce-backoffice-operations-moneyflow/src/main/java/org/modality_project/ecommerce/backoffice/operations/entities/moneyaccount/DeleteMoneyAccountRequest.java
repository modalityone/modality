package org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyAccount;
import org.modality_project.base.shared.entities.MoneyFlow;

import java.util.Collections;
import java.util.List;

public final class DeleteMoneyAccountRequest implements HasOperationCode,
        HasOperationExecutor<DeleteMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "DeleteMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final List<MoneyFlow> moneyFlows;
    private final Pane parentContainer;

    public DeleteMoneyAccountRequest(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows, Pane parentContainer) {
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

    public List<MoneyFlow> getMoneyFlows() { return Collections.unmodifiableList(moneyFlows); }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteMoneyAccountRequest, Void> getOperationExecutor() {
        return DeleteMoneyAccountExecutor::executeRequest;
    }
}
