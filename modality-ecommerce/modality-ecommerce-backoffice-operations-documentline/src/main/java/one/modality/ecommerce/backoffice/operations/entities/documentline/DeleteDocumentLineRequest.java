package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

public final class DeleteDocumentLineRequest extends AbstractSetDocumentLineFieldsRequest<DeleteDocumentLineRequest> {

    private final static String OPERATION_CODE = "DeleteDocumentLine";

    public DeleteDocumentLineRequest(DocumentLine documentLine) {
        this(documentLine, FXMainFrameDialogArea.getDialogArea());
    }

    public DeleteDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        super(documentLine, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<DeleteDocumentLineRequest, Void> getOperationExecutor() {
        return DeleteDocumentLineExecutor::executeRequest;
    }
}
