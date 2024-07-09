package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class MarkDocumentPassAsUpdatedRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "MarkDocumentPassAsUpdated";

    public MarkDocumentPassAsUpdatedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentPassAsUpdatedRequest(Document document, Pane parentContainer) {
        super(document, "read", "true", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
