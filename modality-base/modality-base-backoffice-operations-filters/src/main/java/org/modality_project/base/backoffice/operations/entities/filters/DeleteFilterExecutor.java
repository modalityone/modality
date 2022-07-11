package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.stack.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.async.Future;
import dev.webfx.stack.db.submit.SubmitArgument;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

final class DeleteFilterExecutor {

    static Future<Void> executeRequest(DeleteFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        if (filter == null) {
            DialogContent dialogContent = new DialogContent().setContentText("No filter selected.");
            DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> dialogCallback.closeDialog());
        } else {
            String msg = "Please confirm.\n\nDelete filter \"" + filter.getName() + "\"?";
            DialogContent dialogContent = new DialogContent().setContentText(msg);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                deleteFilter(filter);
                dialogCallback.closeDialog();
            });
        }
        return Future.succeededFuture();
    }

    private static void deleteFilter(Filter filter) {
        UpdateStore updateStore = UpdateStore.createAbove(filter.getStore());
        updateStore.deleteEntity(filter);
        updateStore.submitChanges(SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(false)")
                .setDataSourceId(updateStore.getDataSourceId())
                .build());
    }
}