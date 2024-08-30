package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class MarkDocumentAsUnknownRequest extends AbstractSetDocumentFieldsRequest<MarkDocumentAsUnknownRequest> {

    private final static String OPERATION_CODE = "MarkDocumentAsUnknown";

    public MarkDocumentAsUnknownRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentAsUnknownRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MarkDocumentAsUnknownRequest, Void> getOperationExecutor() {
        return MarkDocumentAsUnknownExecutor::executeRequest;
    }
}
