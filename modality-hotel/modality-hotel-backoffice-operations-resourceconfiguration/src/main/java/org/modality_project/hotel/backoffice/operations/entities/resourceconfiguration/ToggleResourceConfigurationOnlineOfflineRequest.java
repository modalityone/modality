package org.modality_project.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.db.submit.SubmitResult;
import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.async.Batch;

public final class ToggleResourceConfigurationOnlineOfflineRequest implements HasOperationCode,
        HasOperationExecutor<ToggleResourceConfigurationOnlineOfflineRequest, Batch<SubmitResult>> {

    private final static String OPERATION_CODE = "ToggleResourceConfigurationOnlineOffline";

    private final Entity resourceConfiguration;

    public ToggleResourceConfigurationOnlineOfflineRequest(Entity resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    Entity getResourceConfiguration() {
        return resourceConfiguration;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleResourceConfigurationOnlineOfflineRequest, Batch<SubmitResult>> getOperationExecutor() {
        return ToggleResourceConfigurationOnlineOfflineExecutor::executeRequest;
    }
}
