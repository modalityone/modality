package one.modality.base.backoffice.operations.snapshot;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import java.util.Collection;

public final class CreateSnapshotRequest
        implements HasOperationCode, HasOperationExecutor<CreateSnapshotRequest, Void> {

    private static final String OPERATION_CODE = "CreateSnapshot";

    private final Collection<? extends Entity> entities;

    CreateSnapshotRequest(Collection<? extends Entity> entities) {
        this.entities = entities;
    }

    Collection<? extends Entity> getEntities() {
        return entities;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<CreateSnapshotRequest, Void> getOperationExecutor() {
        return CreateSnapshotExecutor::executeRequest;
    }
}
