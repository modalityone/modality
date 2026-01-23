package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsKnownEvent;

/**
 * @author Bruno Salmon
 */
final class MarkDocumentAsKnownExecutor {

    static Future<Void> executeRequest(MarkDocumentAsKnownRequest rq) {
        return DocumentService.submitDocumentChanges(
            SubmitDocumentChangesArgument.of(
                "Marked as known",
                new MarkDocumentAsKnownEvent(rq.getDocument())
            )
        ).map(ignored -> null);
    }

}
