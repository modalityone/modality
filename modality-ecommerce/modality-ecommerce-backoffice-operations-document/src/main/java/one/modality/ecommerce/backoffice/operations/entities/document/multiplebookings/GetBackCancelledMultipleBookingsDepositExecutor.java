package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.Future;
import one.modality.base.backoffice.operations.entities.generic.DialogExecutorUtil;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.multiplebookings.GetBackCancelledMultipleBookingsDepositEvent;

/**
 * @author Bruno Salmon
 */
final class GetBackCancelledMultipleBookingsDepositExecutor {

    static Future<Void> executeRequest(GetBackCancelledMultipleBookingsDepositRequest rq) {
        return DialogExecutorUtil.executeOnUserConfirmation(
                "Please confirm"
                , rq.getParentContainer(),
                () -> DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                                "Got deposit back from cancelled multiple bookings",
                                new GetBackCancelledMultipleBookingsDepositEvent(rq.getDocument()))
                ));
    }

}
