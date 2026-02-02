package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentPassAsReadyEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleMarkDocumentPassAsReadyExecutor {

    static Future<Void> executeRequest(ToggleMarkDocumentPassAsReadyRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("passReady")
            .compose(document -> {
                boolean passReady = !document.isPassReady();
                return DocumentService.submitDocumentChanges(
                    SubmitDocumentChangesArgument.of(
                        passReady ? "Marked pass as ready" : "Unmarked pass as ready",
                        new MarkDocumentPassAsReadyEvent(document, passReady, true)
                    )
                ).map(ignored -> null);
            });
    }

}
