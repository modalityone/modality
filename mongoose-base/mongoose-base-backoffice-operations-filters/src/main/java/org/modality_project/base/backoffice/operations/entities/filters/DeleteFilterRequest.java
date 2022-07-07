package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

public final class DeleteFilterRequest implements HasOperationCode,
        HasOperationExecutor<DeleteFilterRequest, Void> {

    private final static String OPERATION_CODE = "DeleteFilter";

    private final Filter filter;
    private final Pane parentContainer;

    public DeleteFilterRequest(Filter filter, Pane parentContainer) {
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
    public AsyncFunction<DeleteFilterRequest, Void> getOperationExecutor() {
        return DeleteFilterExecutor::executeRequest;
    }
}
