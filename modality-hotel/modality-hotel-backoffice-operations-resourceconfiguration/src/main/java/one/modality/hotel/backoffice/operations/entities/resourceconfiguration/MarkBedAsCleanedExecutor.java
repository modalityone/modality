package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ResourceConfiguration;

import java.time.LocalDate;

final class MarkBedAsCleanedExecutor {

    static Future<Void> executeRequest(MarkBedAsCleanedRequest rq) {
        return execute(rq.getRoomConfiguration(), rq.getBedIndex(), rq.getParentContainer());
    }

    private static Future<Void> execute(ResourceConfiguration roomConfiguration, int bedIndex, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to mark bed " + roomConfiguration.evaluate("name") + " - " + (bedIndex + 1) + " as cleaned?"));
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.create(roomConfiguration.getStore().getDataSourceModel());
            ResourceConfiguration rc = updateStore.updateEntity(roomConfiguration);
            rc.setLastCleaningDate(LocalDate.now());
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(resultBatch -> dialogCallback.closeDialog());
        });
        return promise.future();
    }

}
