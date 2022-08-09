package org.modality_project.catering.backoffice.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.platform.async.AsyncFunction;

public final class DeleteAllocationRuleRequest implements HasOperationCode,
        HasOperationExecutor<DeleteAllocationRuleRequest, Void> {

    private final static String OPERATION_CODE = "DeleteAllocationRule";

    private final Entity documentLine;
    private final Pane parentContainer;

    public DeleteAllocationRuleRequest(Entity documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    Entity getDocumentLine() {
        return documentLine;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<DeleteAllocationRuleRequest, Void> getOperationExecutor() {
        return DeleteAllocationRuleExecutor::executeRequest;
    }
}
