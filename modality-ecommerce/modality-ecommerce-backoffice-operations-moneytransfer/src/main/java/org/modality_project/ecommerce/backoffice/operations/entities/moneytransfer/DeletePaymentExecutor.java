package org.modality_project.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.stack.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.stack.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.platform.async.Future;
import dev.webfx.stack.platform.async.Promise;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.modality_project.base.shared.entities.MoneyTransfer;

final class DeletePaymentExecutor {

    static Future<Void> executeRequest(DeletePaymentRequest rq) {
        return execute(rq.getPayment(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyTransfer payment, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to delete this payment?"));
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.create(payment.getStore().getDataSourceModel());
            updateStore.deleteEntity(payment);
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(resultBatch -> dialogCallback.closeDialog());
        });
        return promise.future();
    }
}
