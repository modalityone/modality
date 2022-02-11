package mongoose.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import mongoose.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import mongoose.base.shared.entities.Document;

public final class ToggleMarkDocumentAsArrivedRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsArrived";

    public ToggleMarkDocumentAsArrivedRequest(Document document, Pane parentContainer) {
        super(document, "arrived", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
