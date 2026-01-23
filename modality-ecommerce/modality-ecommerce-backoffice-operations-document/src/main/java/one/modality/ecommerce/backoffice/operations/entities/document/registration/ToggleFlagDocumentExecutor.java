package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.FlagDocumentEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleFlagDocumentExecutor {

    static Future<Void> executeRequest(ToggleFlagDocumentRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("flagged")
            .compose(document -> {
                boolean flagged = !document.isFlagged(); // toggling flagged
                return ModalityDialog.showConfirmationDialogForAsyncOperation(
                    "Are you sure you want to " + (flagged ? "flag" : "unflag") + " this booking?"
                    , rq.getParentContainer(),
                    () -> DocumentService.submitDocumentChanges(
                        SubmitDocumentChangesArgument.of(
                            flagged ? "Flagged booking" : "Unflagged booking",
                            new FlagDocumentEvent(document, flagged)
                        )
                    ));
            });
    }

}
