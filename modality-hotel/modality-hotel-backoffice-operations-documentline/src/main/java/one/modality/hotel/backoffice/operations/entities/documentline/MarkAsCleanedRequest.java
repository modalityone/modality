package one.modality.hotel.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.DocumentLine;

public final class MarkAsCleanedRequest implements HasOperationCode,
        HasOperationExecutor<MarkAsCleanedRequest, Void> {

    private final static String OPERATION_CODE = "MarkRoomAsCleaned";

    private final DocumentLine documentLine;
    private final Pane parentContainer;

    public MarkAsCleanedRequest(DocumentLine documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MarkAsCleanedRequest, Void> getOperationExecutor() {
        return MarkAsCleanedExecutor::executeRequest;
    }
}
