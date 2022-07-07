package org.modality_project.ecommerce.backoffice.operations.entities.document.multiplebookings;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class ToggleMarkMultipleBookingRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkMultipleBooking";

    public ToggleMarkMultipleBookingRequest(Document document, Pane parentContainer) {
        super(document, "notMultipleBooking", "notMultipleBooking = null ? multipleBooking : null", "Please confirm", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
