package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.dialog.DialogCallback;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;
import one.modality.base.shared.entities.triggers.Triggers;

final class DeleteFilterExecutor {

    static Future<Void> executeRequest(DeleteFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        if (filter == null) {
            DialogContent dialogContent = new DialogContent().setContentText("No filter selected.");
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
        } else {
            String msg = "Please confirm.\n\nDelete filter \"" + filter.getName() + "\"?";
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