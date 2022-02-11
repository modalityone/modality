package mongoose.ecommerce.backoffice.operations.entities.document.security;

import javafx.scene.layout.Pane;
import mongoose.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import mongoose.base.shared.entities.Document;

public final class ToggleMarkDocumentAsKnownRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsKnown";

    public ToggleMarkDocumentAsKnownRequest(Document document, Pane parentContainer) {
        super(document, "person_unknown,person_known,person_verified", "false,true,false", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
