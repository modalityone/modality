package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class DeleteResourceRequest implements HasOperationCode,
        HasOperationExecutor<DeleteResourceRequest, Void> {

    private final static String OPERATION_CODE = "DeleteResource";

    private final ResourceConfiguration resourceConfiguration;
    private final Pane parentContainer;

    public DeleteResourceRequest(ResourceConfiguration resourceConfiguration, Pane parentContainer) {
        this.resourceConfiguration = resourceConfiguration;
        this.parentContainer = parentContainer;
    }

    ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<DeleteResourceRequest, Void> getOperationExecutor() {
        return DeleteResourceExecutor::executeRequest;
    }
}
