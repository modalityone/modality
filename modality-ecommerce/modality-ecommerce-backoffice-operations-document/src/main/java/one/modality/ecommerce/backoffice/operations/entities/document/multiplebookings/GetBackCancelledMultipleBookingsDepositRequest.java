package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class GetBackCancelledMultipleBookingsDepositRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "GetBackCancelledMultipleBookingsDeposit";

    public GetBackCancelledMultipleBookingsDepositRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public GetBackCancelledMultipleBookingsDepositRequest(Document document, Pane parentContainer) {
        super(document, "triggerTransferFromOtherMultipleBookings", "true", "Please confirm", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
