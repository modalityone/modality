package org.modality_project.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyFlow;

final class DeleteMoneyFlowExecutor {

    static Future<Void> executeRequest(DeleteMoneyFlowRequest rq) {
        return execute(rq.getMoneyFlow(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyFlow deleteEntity, Pane parentContainer) {
        if (deleteEntity != null) {
            String msg = "Delete money flow from " + deleteEntity.getFromMoneyAccount().getName() + " to " + deleteEntity.getToMoneyAccount().getName() + "?";
            DialogContent dialogContent = new DialogContent().setTitle("Confirm money flow deletion").setContentText(msg);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                deleteMoneyFlow(deleteEntity);
                dialogCallback.closeDialog();
            });
        }
        return Future.succeededFuture();
    }

    private static void deleteMoneyFlow(MoneyFlow deleteEntity) {
        UpdateStore updateStore = UpdateStore.createAbove(deleteEntity.getStore());
        updateStore.deleteEntity(deleteEntity);
        updateStore.submitChanges(SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(false)")
                .setDataSourceId(updateStore.getDataSourceId())
                .build());
    }
}