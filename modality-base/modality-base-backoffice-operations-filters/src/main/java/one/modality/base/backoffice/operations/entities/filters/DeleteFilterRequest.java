package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Filter;

public final class DeleteFilterRequest
        implements HasOperationCode, HasOperationExecutor<DeleteFilterRequest, Void> {

    private static final String OPERATION_CODE = "DeleteFilter";

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
