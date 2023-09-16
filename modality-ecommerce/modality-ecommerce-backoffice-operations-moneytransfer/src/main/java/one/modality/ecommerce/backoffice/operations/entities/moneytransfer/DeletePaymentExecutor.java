package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.MoneyTransfer;

final class DeletePaymentExecutor {

    static Future<Void> executeRequest(DeletePaymentRequest rq) {
        return execute(rq.getPayment(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyTransfer payment, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure you want to delete this payment?"));
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(promise::complete);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            UpdateStore updateStore = UpdateStore.create(payment.getStore().getDataSourceModel());
            updateStore.deleteEntity(payment);
            updateStore.submitChanges()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(resultBatch -> dialogCallback.closeDialog());
        });
        return promise.future();
    }
}
