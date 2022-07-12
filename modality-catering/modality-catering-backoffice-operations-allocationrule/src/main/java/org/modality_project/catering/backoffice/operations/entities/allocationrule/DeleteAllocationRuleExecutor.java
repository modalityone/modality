package org.modality_project.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.async.Future;
import dev.webfx.stack.async.Promise;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

final class DeleteAllocationRuleExecutor {

    static Future<Void> executeRequest(DeleteAllocationRuleRequest rq) {
        return execute(rq.getDocumentLine(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity documentLine, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to delete this rule?"));
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.create(documentLine.getStore().getDataSourceModel());
            updateStore.deleteEntity(documentLine);
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(batchResult -> dialogCallback.closeDialog());
        });
        return promise.future();
    }
}
