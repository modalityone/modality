package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Filter;

public final class EditFieldsRequest implements HasOperationCode,
        HasOperationExecutor<EditFieldsRequest, Void> {

    private final static String OPERATION_CODE = "EditFields";

    private final Filter filter;
    private final Pane parentContainer;

    public EditFieldsRequest(Filter filter, Pane parentContainer) {
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
    public AsyncFunction<EditFieldsRequest, Void> getOperationExecutor() {
        return EditFieldsExecutor::executeRequest;
    }
}
