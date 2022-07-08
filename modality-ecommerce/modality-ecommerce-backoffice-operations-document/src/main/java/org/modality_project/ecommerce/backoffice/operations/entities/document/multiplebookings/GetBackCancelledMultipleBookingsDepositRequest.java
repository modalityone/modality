package org.modality_project.ecommerce.backoffice.operations.entities.document.multiplebookings;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class GetBackCancelledMultipleBookingsDepositRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "GetBackCancelledMultipleBookingsDeposit";

    public GetBackCancelledMultipleBookingsDepositRequest(Document document, Pane parentContainer) {
        super(document, "triggerTransferFromOtherMultipleBookings", "true", "Please confirm", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
