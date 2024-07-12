package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Objects;
import one.modality.base.backoffice.operations.entities.generic.DialogExecutorUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.line.RemoveDocumentLineEvent;

final class DeleteDocumentLineExecutor {

    static Future<Void> executeRequest(DeleteDocumentLineRequest rq) {
        return rq.getDocumentLine().<DocumentLine>onExpressionLoaded("item.name")
                .compose(documentLine -> {
                    String itemName = Objects.coalesce(documentLine.evaluate("item.name"), "option");
                    return DialogExecutorUtil.executeOnUserConfirmation(
                            "Are you sure you want to delete " + itemName + "?"
                            , rq.getParentContainer(),
                            () -> DocumentService.submitDocumentChanges(
                                    new SubmitDocumentChangesArgument(
                                            "Deleted " + itemName,
                                            new RemoveDocumentLineEvent(documentLine))
                            ));
                });
    }
}
