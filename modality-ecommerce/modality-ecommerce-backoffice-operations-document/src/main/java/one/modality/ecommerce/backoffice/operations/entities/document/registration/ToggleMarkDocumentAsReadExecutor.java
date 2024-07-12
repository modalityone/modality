package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsReadEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleMarkDocumentAsReadExecutor {

    static Future<Void> executeRequest(ToggleMarkDocumentAsReadRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("read")
                .compose(document -> {
                    boolean read = !document.isRead();
                    return DocumentService.submitDocumentChanges(
                            new SubmitDocumentChangesArgument(
                                    read ? "Marked as read" : "Unmarked as read",
                                    new MarkDocumentAsReadEvent(document, read))
                    ).map(x -> null);
                });
    }

}
