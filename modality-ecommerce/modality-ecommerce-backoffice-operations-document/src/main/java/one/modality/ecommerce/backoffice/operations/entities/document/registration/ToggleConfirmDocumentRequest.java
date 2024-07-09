package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleConfirmDocumentRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleConfirmDocument";

    public ToggleConfirmDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleConfirmDocumentRequest(Document document, Pane parentContainer) {
        super(document, "confirmed,read", "!confirmed,passReady?false:true", "Are you sure you want to confirm this booking?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
