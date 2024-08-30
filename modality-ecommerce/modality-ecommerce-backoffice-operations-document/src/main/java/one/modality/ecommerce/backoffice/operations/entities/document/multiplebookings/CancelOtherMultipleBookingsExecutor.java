package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.Future;
import one.modality.base.backoffice.operations.entities.generic.DialogExecutorUtil;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.multiplebookings.CancelOtherMultipleBookingsEvent;

/**
 * @author Bruno Salmon
 */
final class CancelOtherMultipleBookingsExecutor {

    static Future<Void> executeRequest(CancelOtherMultipleBookingsRequest rq) {
        return DialogExecutorUtil.executeOnUserConfirmation(
                "Please confirm"
                , rq.getParentContainer(),
                () -> DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                                "Cancelled other multiple bookings",
                                new CancelOtherMultipleBookingsEvent(rq.getDocument()))
                ));
    }

}
