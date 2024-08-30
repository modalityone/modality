package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class MarkDocumentAsVerifiedRequest extends AbstractSetDocumentFieldsRequest<MarkDocumentAsVerifiedRequest> {

    private final static String OPERATION_CODE = "MarkDocumentAsVerified";

    public MarkDocumentAsVerifiedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentAsVerifiedRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MarkDocumentAsVerifiedRequest, Void> getOperationExecutor() {
        return MarkDocumentAsVerifiedExecutor::executeRequest;
    }
}
