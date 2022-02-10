package mongoose.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import mongoose.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import mongoose.shared.entities.Document;

public final class ToggleMarkDocumentAsReadRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsRead";

    public ToggleMarkDocumentAsReadRequest(Document document, Pane parentContainer) {
        super(document, "read", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
