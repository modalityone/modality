package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.platform.async.AsyncFunction;
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
