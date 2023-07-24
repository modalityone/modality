package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsWillPayRequest extends ToggleBooleanEntityFieldRequest {

  private static final String OPERATION_CODE = "ToggleMarkDocumentAsWillPay";

  public ToggleMarkDocumentAsWillPayRequest(Document document, Pane parentContainer) {
    super(document, "willPay", null, parentContainer);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
