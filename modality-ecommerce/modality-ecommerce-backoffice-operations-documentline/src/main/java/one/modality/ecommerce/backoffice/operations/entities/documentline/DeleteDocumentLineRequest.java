package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.DocumentLine;

public final class DeleteDocumentLineRequest
    implements HasOperationCode, HasOperationExecutor<DeleteDocumentLineRequest, Void> {

  private static final String OPERATION_CODE = "DeleteDocumentLine";

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
