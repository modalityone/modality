package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsReadRequest extends ToggleBooleanEntityFieldRequest {

  private static final String OPERATION_CODE = "ToggleMarkDocumentAsRead";

  public ToggleMarkDocumentAsReadRequest(Document document, Pane parentContainer) {
    super(document, "read", null, parentContainer);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
