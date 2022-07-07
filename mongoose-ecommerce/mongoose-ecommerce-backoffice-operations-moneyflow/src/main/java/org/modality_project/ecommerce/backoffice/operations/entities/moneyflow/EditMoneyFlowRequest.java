package org.modality_project.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyFlow;

public final class EditMoneyFlowRequest implements HasOperationCode,
        HasOperationExecutor<EditMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "EditMoneyFlow";

    private final MoneyFlow moneyFlow;
    private final Pane parentContainer;

    public EditMoneyFlowRequest(MoneyFlow moneyFlow, Pane parentContainer) {
        this.moneyFlow = moneyFlow;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public MoneyFlow getMoneyFlow() { return moneyFlow; }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditMoneyFlowRequest, Void> getOperationExecutor() {
        return EditMoneyFlowExecutor::executeRequest;
    }
}
