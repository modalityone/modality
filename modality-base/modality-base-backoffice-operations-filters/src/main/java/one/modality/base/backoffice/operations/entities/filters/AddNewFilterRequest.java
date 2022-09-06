package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;

public final class AddNewFilterRequest implements HasOperationCode,
        HasOperationExecutor<AddNewFilterRequest, Void> {

    private final static String OPERATION_CODE = "AddNewFilter";

    private final EntityStore entityStore;
    private final Pane parentContainer;

    public AddNewFilterRequest(EntityStore entityStore, Pane parentContainer) {
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
    public AsyncFunction<AddNewFilterRequest, Void> getOperationExecutor() {
        return AddNewFilterExecutor::executeRequest;
    }
}
