package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;

public final class EditFilterRequest implements HasOperationCode,
        HasOperationExecutor<EditFilterRequest, Void> {

    private final static String OPERATION_CODE = "EditFilter";

    private final Filter filter;
    private final Pane parentContainer;

    public EditFilterRequest(Filter filter, Pane parentContainer) {
        this.filter = filter;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Filter getFilter() { return filter; }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditFilterRequest, Void> getOperationExecutor() {
        return EditFilterExecutor::executeRequest;
    }
}
