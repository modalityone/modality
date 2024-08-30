package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsReadEvent;

/**
 * @author Bruno Salmon
 */
final class MarkDocumentPassAsUpdatedExecutor {

    static Future<Void> executeRequest(MarkDocumentPassAsUpdatedRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("read")
                .compose(document -> DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                                "Marked pass as updated",
                                new MarkDocumentAsReadEvent(document, true))
                ).map(ignored -> null));
    }

}
