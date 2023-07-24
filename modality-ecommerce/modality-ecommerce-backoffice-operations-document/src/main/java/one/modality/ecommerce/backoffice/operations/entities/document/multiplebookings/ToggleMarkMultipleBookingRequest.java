package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import javafx.scene.layout.Pane;

import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkMultipleBookingRequest extends SetEntityFieldRequest {

    private static final String OPERATION_CODE = "ToggleMarkMultipleBooking";

    public ToggleMarkMultipleBookingRequest(Document document, Pane parentContainer) {
        super(
                document,
                "notMultipleBooking",
                "notMultipleBooking = null ? multipleBooking : null",
                "Please confirm",
                parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
