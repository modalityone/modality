package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.async.Batch;
import dev.webfx.stack.db.submit.SubmitResult;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class ToggleResourceConfigurationOnlineOfflineRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<ToggleResourceConfigurationOnlineOfflineRequest, Batch<SubmitResult>> {

    private final static String OPERATION_CODE = "ToggleResourceConfigurationOnlineOffline";

    private final ResourceConfiguration resourceConfiguration;

    public ToggleResourceConfigurationOnlineOfflineRequest(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return ResourceConfigurationI18nKeys.ToggleOnlineOffline;
    }


    @Override
    public AsyncFunction<ToggleResourceConfigurationOnlineOfflineRequest, Batch<SubmitResult>> getOperationExecutor() {
        return ToggleResourceConfigurationOnlineOfflineExecutor::executeRequest;
    }
}
