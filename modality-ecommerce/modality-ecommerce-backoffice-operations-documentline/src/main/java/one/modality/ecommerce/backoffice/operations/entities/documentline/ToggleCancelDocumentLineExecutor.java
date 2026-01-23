package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Objects;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.documentline.CancelDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
final class ToggleCancelDocumentLineExecutor {

    static Future<Void> executeRequest(ToggleCancelDocumentLineRequest rq) {
        return rq.getDocumentLine().<DocumentLine>onExpressionLoaded("cancelled,item.name")
            .compose(documentLine -> {
                boolean cancelled = !documentLine.isCancelled();
                boolean read = true;
                String itemName = Objects.coalesce(documentLine.evaluate("item.name"), "option");
                return ModalityDialog.showConfirmationDialogForAsyncOperation(
                    "Are you sure you want to " + (cancelled ? "cancel " : "uncancel ") + itemName + "?"
                    , rq.getParentContainer(),
                    () -> DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                            (cancelled ? "Cancelled " : "Uncancelled ") + itemName,
                            new CancelDocumentLineEvent(documentLine, cancelled, read)
                        )
                    ));
            });
    }

}
