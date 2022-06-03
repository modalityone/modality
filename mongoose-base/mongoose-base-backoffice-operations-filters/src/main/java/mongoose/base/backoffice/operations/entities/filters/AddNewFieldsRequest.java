package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;

public final class AddNewFieldsRequest implements HasOperationCode,
        HasOperationExecutor<AddNewFieldsRequest, Void> {

    private final static String OPERATION_CODE = "AddNewFields";

    private final EntityStore entityStore;
    private final Pane parentContainer;

    public AddNewFieldsRequest(EntityStore entityStore, Pane parentContainer) {
        this.entityStore = entityStore;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public EntityStore getEntityStore() {
        return entityStore;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewFieldsRequest, Void> getOperationExecutor() {
        return AddNewFieldsExecutor::executeRequest;
    }
}
