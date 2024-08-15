package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleConfirmDocumentRequest extends AbstractSetDocumentFieldsRequest<ToggleConfirmDocumentRequest> {

    private final static String OPERATION_CODE = "ToggleConfirmDocument";

    public ToggleConfirmDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleConfirmDocumentRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleConfirmDocumentRequest, Void> getOperationExecutor() {
        return ToggleConfirmDocumentExecutor::executeRequest;
    }
}
