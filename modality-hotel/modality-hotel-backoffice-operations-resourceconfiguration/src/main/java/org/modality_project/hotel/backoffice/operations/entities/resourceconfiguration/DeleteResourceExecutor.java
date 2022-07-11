package org.modality_project.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.stack.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.stack.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.async.Future;
import dev.webfx.stack.async.Promise;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

final class DeleteResourceExecutor {

    static Future<Void> executeRequest(DeleteResourceRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity resourceConfiguration, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to delete room " + resourceConfiguration.evaluate("name") + '?'));
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.create(resourceConfiguration.getStore().getDataSourceModel());
            updateStore.deleteEntity(resourceConfiguration);
            updateStore.deleteEntity(resourceConfiguration.<Entity>getForeignEntity("resource"));
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(resultBatch -> dialogCallback.closeDialog());
        });
        return promise.future();
    }
}
