package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsArrivedRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsArrived";

    public ToggleMarkDocumentAsArrivedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleMarkDocumentAsArrivedRequest(Document document, Pane parentContainer) {
        super(document, "arrived", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
