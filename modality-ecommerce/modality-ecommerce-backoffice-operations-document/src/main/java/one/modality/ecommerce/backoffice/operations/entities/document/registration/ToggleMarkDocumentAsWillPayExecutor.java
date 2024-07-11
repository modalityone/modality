package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.MarkDocumentAsWillPayEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleMarkDocumentAsWillPayExecutor {

    static Future<Void> executeRequest(ToggleMarkDocumentAsWillPayRequest rq) {
        return rq.getDocument().<Document>onExpressionLoaded("willPay")
                .compose(document -> {
                    boolean willPay = !document.isWillPay();
                    return DocumentService.submitDocumentChanges(
                            new SubmitDocumentChangesArgument(
                                    willPay ? "Marked as will pay" : "Unmarked as will pay",
                                    new MarkDocumentAsWillPayEvent(document, willPay))
                    ).map(ignored -> null);
                });
    }

}
