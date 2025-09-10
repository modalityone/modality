package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class ToggleResourceConfigurationOnlineOfflineRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<ToggleResourceConfigurationOnlineOfflineRequest, Void> {

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
    public AsyncFunction<ToggleResourceConfigurationOnlineOfflineRequest, Void> getOperationExecutor() {
        return ToggleResourceConfigurationOnlineOfflineExecutor::executeRequest;
    }
}
