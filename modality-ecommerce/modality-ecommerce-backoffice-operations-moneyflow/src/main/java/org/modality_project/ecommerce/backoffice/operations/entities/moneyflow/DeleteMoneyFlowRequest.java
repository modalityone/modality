package org.modality_project.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyFlow;

public final class DeleteMoneyFlowRequest implements HasOperationCode,
        HasOperationExecutor<DeleteMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "DeleteMoneyFlow";

    private final MoneyFlow moneyFlow;
    private final Pane parentContainer;

    public DeleteMoneyFlowRequest(MoneyFlow moneyFlow, Pane parentContainer) {
        this.moneyFlow = moneyFlow;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public MoneyFlow getMoneyFlow() {
        return moneyFlow;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteMoneyFlowRequest, Void> getOperationExecutor() {
        return DeleteMoneyFlowExecutor::executeRequest;
    }
}
