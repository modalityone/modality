package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.Future;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.multiplebookings.MarkNotMultipleBookingEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleMarkNotMultipleBookingExecutor {

    static Future<Void> executeRequest(ToggleMarkNotMultipleBookingRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("notMultipleBooking,multipleBooking")
                .compose(document -> {
                    Document notMultipleBooking = document.evaluate("notMultipleBooking = null ? multipleBooking : null"); // toggling confirmed

                    return ModalityDialog.showConfirmationDialogForAsyncOperation(
                            "Are you sure you want to " + (notMultipleBooking == null ? "unmark" : "mark") + " this booking as multiple booking?"
                            , rq.getParentContainer(),
                            () -> DocumentService.submitDocumentChanges(
                                    new SubmitDocumentChangesArgument(
                                            notMultipleBooking == null ? "Unmarked as multiple booking" : "Marked as multiple booking",
                                            new MarkNotMultipleBookingEvent(document, notMultipleBooking))
                            ));
                });
    }

}
