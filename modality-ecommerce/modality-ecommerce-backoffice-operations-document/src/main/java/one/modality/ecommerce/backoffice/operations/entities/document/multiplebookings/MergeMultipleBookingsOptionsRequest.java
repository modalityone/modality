package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import javafx.scene.layout.Pane;

import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class MergeMultipleBookingsOptionsRequest extends SetEntityFieldRequest {

    private static final String OPERATION_CODE = "MergeMultipleBookingsOptions";

    public MergeMultipleBookingsOptionsRequest(Document document, Pane parentContainer) {
        super(
                document,
                "triggerMergeFromOtherMultipleBookings",
                "true",
                "Please confirm",
                parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
