package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import one.modality.base.shared.entities.ResourceConfiguration;

public final class MoveToResourceConfigurationRequest
        implements HasOperationCode,
                HasOperationExecutor<MoveToResourceConfigurationRequest, Void> {

    private static final String OPERATION_CODE = "MoveToResourceConfiguration";

    private final ResourceConfiguration resourceConfiguration;
    private final Object[] documentLinePrimaryKeys;

    public MoveToResourceConfigurationRequest(
            ResourceConfiguration resourceConfiguration, Object[] documentLinePrimaryKeys) {
        this.resourceConfiguration = resourceConfiguration;
        this.documentLinePrimaryKeys = documentLinePrimaryKeys;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public Object[] getDocumentLinePrimaryKeys() {
        return documentLinePrimaryKeys;
    }

    @Override
    public AsyncFunction<MoveToResourceConfigurationRequest, Void> getOperationExecutor() {
        return MoveToResourceConfigurationExecutor::executeRequest;
    }
}
