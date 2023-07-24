package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;

import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleCancelDocumentRequest extends SetEntityFieldRequest {

    private static final String OPERATION_CODE = "ToggleCancelDocument";

    public ToggleCancelDocumentRequest(Document document, Pane parentContainer) {
        super(
                document,
                "cancelled,read",
                "!cancelled,passReady?false:true",
                "Are you sure you want to cancel this booking?",
                parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
