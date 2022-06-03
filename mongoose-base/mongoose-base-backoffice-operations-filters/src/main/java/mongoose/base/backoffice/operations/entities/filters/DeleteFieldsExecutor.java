package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.platform.shared.async.Future;
import dev.webfx.platform.shared.services.submit.SubmitArgument;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Filter;

final class DeleteFieldsExecutor {

    static Future<Void> executeRequest(DeleteFieldsRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        if (filter == null) {
            DialogContent dialogContent = new DialogContent().setContentText("No field set selected.");
            DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> dialogCallback.closeDialog());
        } else {
            String msg = "Please confirm.\n\nDelete field set \"" + filter.getName() + "\"?";
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