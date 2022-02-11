package mongoose.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import mongoose.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import mongoose.base.shared.entities.Document;

public final class ToggleFlagDocumentRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleFlagDocument";

    public ToggleFlagDocumentRequest(Document document, Pane parentContainer) {
        super(document, "flagged", "Are you sure you want to flag this booking?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
