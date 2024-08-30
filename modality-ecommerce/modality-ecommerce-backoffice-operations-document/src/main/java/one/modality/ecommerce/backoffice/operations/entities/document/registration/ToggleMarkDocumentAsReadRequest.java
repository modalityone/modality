package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsReadRequest extends AbstractSetDocumentFieldsRequest<ToggleMarkDocumentAsReadRequest> {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsRead";

    public ToggleMarkDocumentAsReadRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleMarkDocumentAsReadRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleMarkDocumentAsReadRequest, Void> getOperationExecutor() {
        return ToggleMarkDocumentAsReadExecutor::executeRequest;
    }
}
