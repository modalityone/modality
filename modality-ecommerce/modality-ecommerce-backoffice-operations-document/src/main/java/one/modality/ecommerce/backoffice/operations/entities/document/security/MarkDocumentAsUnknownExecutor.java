package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsUnknownEvent;

/**
 * @author Bruno Salmon
 */
final class MarkDocumentAsUnknownExecutor {

    static Future<Void> executeRequest(MarkDocumentAsUnknownRequest rq) {
        return DocumentService.submitDocumentChanges(
            new SubmitDocumentChangesArgument(
                "Marked as unknown",
                new MarkDocumentAsUnknownEvent(rq.getDocument())
            )
        ).map(ignored -> null);
    }

}
