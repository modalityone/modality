package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

public final class DeleteDocumentLineRequest implements HasOperationCode,
        HasOperationExecutor<DeleteDocumentLineRequest, Void> {

    private final static String OPERATION_CODE = "DeleteDocumentLine";

    private final DocumentLine documentLine;
    private final Pane parentContainer;

    public DeleteDocumentLineRequest(DocumentLine documentLine) {
        this(documentLine, FXMainFrameDialogArea.getDialogArea());
    }

    public DeleteDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public Pane getParentContainer() {
        return parentContainer;
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
