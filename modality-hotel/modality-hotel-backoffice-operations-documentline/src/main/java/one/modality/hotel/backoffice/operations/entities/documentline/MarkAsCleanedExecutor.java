package one.modality.hotel.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.DocumentLine;

final class MarkAsCleanedExecutor {

    static Future<Void> executeRequest(MarkAsCleanedRequest rq) {
        return execute(rq.getDocumentLine(), rq.getParentContainer());
    }

    private static Future<Void> execute(DocumentLine documentLine, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to mark as cleaned?"));
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.createAbove(documentLine.getStore());
            DocumentLine dl = updateStore.updateEntity(documentLine);
            dl.setCleaned(true);
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(resultBatch -> dialogCallback.closeDialog());
        });
        return promise.future();
    }

}
