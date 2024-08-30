package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.Future;
import one.modality.base.backoffice.operations.entities.generic.DialogExecutorUtil;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.multiplebookings.MergeMultipleBookingsOptionsEvent;

/**
 * @author Bruno Salmon
 */
final class MergeMultipleBookingsOptionsExecutor {

    static Future<Void> executeRequest(MergeMultipleBookingsOptionsRequest rq) {
        return DialogExecutorUtil.executeOnUserConfirmation(
                "Please confirm"
                , rq.getParentContainer(),
                () -> DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                                "Merged options from other multiple bookings",
                                new MergeMultipleBookingsOptionsEvent(rq.getDocument()))
                ));
    }

}
