package one.modality.ecommerce.backoffice.operations.entities.document.cart;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

public final class OpenBookingCartRequest
    implements HasOperationCode, HasOperationExecutor<OpenBookingCartRequest, Void> {

  private static final String OPERATION_CODE = "OpenBookingCart";

  private final Document document;
  private final Pane parentContainer;

  public OpenBookingCartRequest(Document document, Pane parentContainer) {
    this.document = document;
    this.parentContainer = parentContainer;
  }

  Document getDocument() {
    return document;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<OpenBookingCartRequest, Void> getOperationExecutor() {
    return OpenBookingCartExecutor::executeRequest;
  }
}
