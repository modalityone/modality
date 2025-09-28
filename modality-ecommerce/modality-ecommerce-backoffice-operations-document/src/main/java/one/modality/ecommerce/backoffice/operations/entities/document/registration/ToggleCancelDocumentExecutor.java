package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.book.CancelDocumentEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleCancelDocumentExecutor {

    static Future<Void> executeRequest(ToggleCancelDocumentRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("cancelled,passReady")
                .compose(document -> {
                    boolean cancelled = !document.isCancelled();
                    boolean read = !document.isPassReady();
                    return ModalityDialog.showConfirmationDialogForAsyncOperation(
                        "Are you sure you want to " + (cancelled ? "cancel" : "uncancel") + " this booking?"
                        , rq.getParentContainer(),
                        () -> DocumentService.submitDocumentChanges(
                            new SubmitDocumentChangesArgument(
                                cancelled ? "Cancelled booking" : "Uncancelled booking",
                                new CancelDocumentEvent(document, cancelled, read))
                        ));
                });
    }

}
