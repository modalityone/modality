package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.backoffice.operations.entities.generic.DialogExecutorUtil;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.ConfirmDocumentEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleConfirmDocumentExecutor {

    static Future<Void> executeRequest(ToggleConfirmDocumentRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("confirmed,passReady")
                .compose(document -> {
                    boolean confirmed = !document.isConfirmed(); // toggling confirmed
                    boolean read = !document.isPassReady(); // read if pass is not ready, otherwise
                    return DialogExecutorUtil.executeOnUserConfirmation(
                            "Are you sure you want to " + (confirmed ? "confirm" : "unconfirm") + " this booking?"
                            , rq.getParentContainer(),
                            () -> DocumentService.submitDocumentChanges(
                                    new SubmitDocumentChangesArgument(
                                            confirmed ? "Marked as confirmed" : "Unmarked as confirmed",
                                            new ConfirmDocumentEvent(document, confirmed, read))
                            ));
                });
    }

}
