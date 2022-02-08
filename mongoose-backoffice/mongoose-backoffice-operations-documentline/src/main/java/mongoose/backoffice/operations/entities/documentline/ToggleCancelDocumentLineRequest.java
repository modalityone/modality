package mongoose.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import mongoose.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import mongoose.shared.entities.DocumentLine;

public final class ToggleCancelDocumentLineRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleCancelDocumentLine";

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        super (documentLine, "cancelled", "Are you sure you want to cancel this option?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
