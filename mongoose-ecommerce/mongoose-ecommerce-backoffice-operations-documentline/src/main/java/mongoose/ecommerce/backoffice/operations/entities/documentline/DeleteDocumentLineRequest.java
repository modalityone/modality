package mongoose.ecommerce.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.DocumentLine;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.util.async.AsyncFunction;

public final class DeleteDocumentLineRequest implements HasOperationCode,
        HasOperationExecutor<DeleteDocumentLineRequest, Void> {

    private final static String OPERATION_CODE = "DeleteDocumentLine";

    private final DocumentLine documentLine;
    private final Pane parentContainer;

    public DeleteDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    DocumentLine getDocumentLine() {
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
    public AsyncFunction<DeleteDocumentLineRequest, Void> getOperationExecutor() {
        return DeleteDocumentLineExecutor::executeRequest;
    }
}
