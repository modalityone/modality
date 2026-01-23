package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsArrivedEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleMarkDocumentAsArrivedExecutor {

    static Future<Void> executeRequest(ToggleMarkDocumentAsArrivedRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("arrived")
            .compose(document -> {
                boolean arrived = !document.isArrived();
                boolean read = true;
                return DocumentService.submitDocumentChanges(
                    SubmitDocumentChangesArgument.of(
                        arrived ? "Marked as arrived" : "Unmarked as arrived",
                        new MarkDocumentAsArrivedEvent(document, arrived, read)
                    )
                ).map(x -> null);
            });
    }

}
