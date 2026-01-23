package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsUncheckedEvent;

/**
 * @author Bruno Salmon
 */
final class MarkDocumentAsUncheckedExecutor {

    static Future<Void> executeRequest(MarkDocumentAsUncheckedRequest rq) {
        return DocumentService.submitDocumentChanges(
            new SubmitDocumentChangesArgument(
                "Marked as unchecked",
                new MarkDocumentAsUncheckedEvent(rq.getDocument())
            )
        ).map(ignored -> null);
    }

}
