package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsVerifiedEvent;

/**
 * @author Bruno Salmon
 */
final class MarkDocumentAsVerifiedExecutor {

    static Future<Void> executeRequest(MarkDocumentAsVerifiedRequest rq) {
        return DocumentService.submitDocumentChanges(
                new SubmitDocumentChangesArgument(
                        "Marked as verified",
                        new MarkDocumentAsVerifiedEvent(rq.getDocument()))
        ).map(ignored -> null);
    }

}
