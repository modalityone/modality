package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ResourceConfiguration;

final class DeleteResourceExecutor {

    static Future<Void> executeRequest(DeleteResourceRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getParentContainer());
    }

    private static Future<Void> execute(ResourceConfiguration resourceConfiguration, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to delete room " + resourceConfiguration.evaluate("name") + '?'));
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
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
