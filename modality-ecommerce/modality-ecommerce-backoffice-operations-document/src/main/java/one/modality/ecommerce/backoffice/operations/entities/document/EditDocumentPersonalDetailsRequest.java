package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

public final class EditDocumentPersonalDetailsRequest
    implements HasOperationCode, HasOperationExecutor<EditDocumentPersonalDetailsRequest, Void> {

  private static final String OPERATION_CODE = "EditDocumentPersonalDetails";

  private final Document document;
  private final ButtonFactoryMixin buttonFactoryMixin;
  private final Pane parentContainer;

  public EditDocumentPersonalDetailsRequest(
      Document document, ButtonFactoryMixin buttonFactoryMixin, Pane parentContainer) {
    this.document = document;
    this.buttonFactoryMixin = buttonFactoryMixin;
    this.parentContainer = parentContainer;
  }

  Document getDocument() {
    return document;
  }

  public ButtonFactoryMixin getButtonFactoryMixin() {
    return buttonFactoryMixin;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<EditDocumentPersonalDetailsRequest, Void> getOperationExecutor() {
    return EditDocumentPersonalDetailsExecutor::executeRequest;
  }
}
