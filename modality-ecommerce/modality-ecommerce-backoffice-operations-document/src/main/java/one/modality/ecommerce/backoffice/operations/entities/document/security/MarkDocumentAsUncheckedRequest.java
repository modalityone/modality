package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class MarkDocumentAsUncheckedRequest extends AbstractSetDocumentFieldsRequest<MarkDocumentAsUncheckedRequest> {

    private final static String OPERATION_CODE = "MarkDocumentAsUnchecked";

    public MarkDocumentAsUncheckedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentAsUncheckedRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MarkDocumentAsUncheckedRequest, Void> getOperationExecutor() {
        return MarkDocumentAsUncheckedExecutor::executeRequest;
    }
}
