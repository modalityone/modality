package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class MarkBedAsCleanedRequest implements HasOperationCode,
        HasOperationExecutor<MarkBedAsCleanedRequest, Void> {

    private final static String OPERATION_CODE = "MarkRoomAsCleaned";

    private final ResourceConfiguration roomConfiguration;
    private final int bedIndex;
    private final Pane parentContainer;

    public MarkBedAsCleanedRequest(ResourceConfiguration roomConfiguration, int bedIndex, Pane parentContainer) {
        this.roomConfiguration = roomConfiguration;
        this.bedIndex = bedIndex;
        this.parentContainer = parentContainer;
    }

    ResourceConfiguration getRoomConfiguration() {
        return roomConfiguration;
    }

    public int getBedIndex() {
        return bedIndex;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MarkBedAsCleanedRequest, Void> getOperationExecutor() {
        return MarkBedAsCleanedExecutor::executeRequest;
    }
}
