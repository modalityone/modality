package one.modality.ecommerce.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

public final class ToggleCancelDocumentLineRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleCancelDocumentLine";

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine) {
        this(documentLine, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        super (documentLine, "cancelled", "Are you sure you want to cancel this option?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
