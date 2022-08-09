package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

public final class DeleteFieldsRequest implements HasOperationCode,
        HasOperationExecutor<DeleteFieldsRequest, Void> {

    private final static String OPERATION_CODE = "DeleteFields";

    private final Filter filter;
    private final Pane parentContainer;

    public DeleteFieldsRequest(Filter filter, Pane parentContainer) {
        this.filter = filter;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Filter getFilter() {
        return filter;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteFieldsRequest, Void> getOperationExecutor() {
        return DeleteFieldsExecutor::executeRequest;
    }
}
