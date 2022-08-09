package org.modality_project.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;

public final class AddNewMoneyFlowRequest implements HasOperationCode,
        HasOperationExecutor<AddNewMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyFlow";

    private final Pane parentContainer;

    public AddNewMoneyFlowRequest(Pane parentContainer) {
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewMoneyFlowRequest, Void> getOperationExecutor() {
        return AddNewMoneyFlowExecutor::executeRequest;
    }
}
