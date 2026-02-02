package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.Future;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.multiplebookings.MergeMultipleBookingsOptionsEvent;

/**
 * @author Bruno Salmon
 */
final class MergeMultipleBookingsOptionsExecutor {

    static Future<Void> executeRequest(MergeMultipleBookingsOptionsRequest rq) {
        return ModalityDialog.showConfirmationDialogForAsyncOperation(
            "Please confirm"
            , rq.getParentContainer(),
            () -> DocumentService.submitDocumentChanges(
                SubmitDocumentChangesArgument.of(
                    "Merged options from other multiple bookings",
                    new MergeMultipleBookingsOptionsEvent(rq.getDocument())
                )
            ));
    }

}
