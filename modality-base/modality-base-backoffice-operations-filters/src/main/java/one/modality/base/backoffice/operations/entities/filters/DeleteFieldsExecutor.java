package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;
import one.modality.base.shared.entities.triggers.Triggers;

final class DeleteFieldsExecutor {

    static Future<Void> executeRequest(DeleteFieldsRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        if (filter == null) {
            DialogContent dialogContent = new DialogContent().setContentText("No field set selected.");
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
        } else {
            String msg = "Please confirm.\n\nDelete field set \"" + filter.getName() + "\"?";
            DialogContent dialogContent = new DialogContent().setContentText(msg);
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                deleteFilter(filter);
                dialogCallback.closeDialog();
            });
        }
        return Future.succeededFuture();
    }

    private static void deleteFilter(Filter filter) {
        UpdateStore updateStore = UpdateStore.createAbove(filter.getStore());
        updateStore.deleteEntity(filter);
        updateStore.submitChanges(Triggers.backOfficeTransaction(updateStore));
    }
}