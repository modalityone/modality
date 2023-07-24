package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleFlagDocumentRequest extends ToggleBooleanEntityFieldRequest {

  private static final String OPERATION_CODE = "ToggleFlagDocument";

  public ToggleFlagDocumentRequest(Document document, Pane parentContainer) {
    super(document, "flagged", "Are you sure you want to flag this booking?", parentContainer);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
