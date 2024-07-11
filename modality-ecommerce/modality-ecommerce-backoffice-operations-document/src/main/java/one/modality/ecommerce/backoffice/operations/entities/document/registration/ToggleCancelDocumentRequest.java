package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleCancelDocumentRequest extends AbstractSetDocumentFieldsRequest<ToggleCancelDocumentRequest> {

    private final static String OPERATION_CODE = "ToggleCancelDocument";

    public ToggleCancelDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleCancelDocumentRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleCancelDocumentRequest, Void> getOperationExecutor() {
        return ToggleCancelDocumentExecutor::executeRequest;
    }
}
