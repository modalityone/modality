package one.modality.ecommerce.backoffice.operations.entities.document.security;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsVerifiedRequest extends SetEntityFieldRequest {

  private static final String OPERATION_CODE = "ToggleMarkDocumentAsVerified";

  public ToggleMarkDocumentAsVerifiedRequest(Document document, Pane parentContainer) {
    super(
        document,
        "person_unknown,person_known,person_verified",
        "false,true,true",
        null,
        parentContainer);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
