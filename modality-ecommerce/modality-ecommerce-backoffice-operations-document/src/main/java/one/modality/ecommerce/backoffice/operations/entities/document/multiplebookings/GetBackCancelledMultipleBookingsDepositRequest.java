package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class GetBackCancelledMultipleBookingsDepositRequest extends AbstractSetDocumentFieldsRequest<GetBackCancelledMultipleBookingsDepositRequest> {

    private final static String OPERATION_CODE = "GetBackCancelledMultipleBookingsDeposit";

    public GetBackCancelledMultipleBookingsDepositRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public GetBackCancelledMultipleBookingsDepositRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<GetBackCancelledMultipleBookingsDepositRequest, Void> getOperationExecutor() {
        return GetBackCancelledMultipleBookingsDepositExecutor::executeRequest;
    }
}
