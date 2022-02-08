package mongoose.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.platform.shared.util.async.AsyncFunction;

public final class MoveToResourceConfigurationRequest implements HasOperationCode,
        HasOperationExecutor<MoveToResourceConfigurationRequest, Void> {

    private final static String OPERATION_CODE = "MoveToResourceConfiguration";

    private final Entity resourceConfiguration;
    private final Object[] documentLinePrimaryKeys;

    public MoveToResourceConfigurationRequest(Entity resourceConfiguration, Object[] documentLinePrimaryKeys) {
        this.resourceConfiguration = resourceConfiguration;
        this.documentLinePrimaryKeys = documentLinePrimaryKeys;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Entity getResourceConfiguration() {
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
