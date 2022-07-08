package org.modality_project.base.backoffice.operations.snapshot;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.platform.shared.async.AsyncFunction;

import java.util.Collection;

public final class CreateSnapshotRequest implements HasOperationCode,
        HasOperationExecutor<CreateSnapshotRequest, Void> {

    private final static String OPERATION_CODE = "CreateSnapshot";

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
