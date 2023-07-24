package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import one.modality.base.shared.entities.DocumentLine;

final class DeleteDocumentLineExecutor {

    static Future<Void> executeRequest(DeleteDocumentLineRequest rq) {
        return execute(rq.getDocumentLine(), rq.getParentContainer());
    }

    private static Future<Void> execute(DocumentLine documentLine, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent =
                new DialogContent()
                        .setContent(new Text("Are you sure you want to delete this option?"));
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer)
                .addCloseHook(promise::complete);
        DialogUtil.armDialogContentButtons(
                dialogContent,
                dialogCallback -> {
                    UpdateStore updateStore =
                            UpdateStore.create(documentLine.getStore().getDataSourceModel());
                    updateStore.deleteEntity(documentLine);
                    updateStore
                            .submitChanges()
                            .onFailure(dialogCallback::showException)
                            .onSuccess(resultBatch -> dialogCallback.closeDialog());
                });
        return promise.future();
    }
}
