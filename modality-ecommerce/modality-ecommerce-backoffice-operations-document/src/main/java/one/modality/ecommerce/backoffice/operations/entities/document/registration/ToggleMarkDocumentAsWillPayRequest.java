package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsWillPayRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsWillPay";

    public ToggleMarkDocumentAsWillPayRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }


    public ToggleMarkDocumentAsWillPayRequest(Document document, Pane parentContainer) {
        super(document, "willPay", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
