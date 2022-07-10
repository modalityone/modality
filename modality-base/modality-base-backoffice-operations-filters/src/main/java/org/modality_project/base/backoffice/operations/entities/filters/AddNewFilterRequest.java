package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.shared.operation.HasOperationExecutor;
import dev.webfx.stack.framework.shared.orm.entity.EntityStore;
import dev.webfx.stack.platform.async.AsyncFunction;
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
