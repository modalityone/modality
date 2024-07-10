package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.expression.Expression;
import dev.webfx.stack.ui.controls.alert.AlertUtil;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.exceptions.UserCancellationException;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

final class SetEntityFieldExecutor {

    static Future<Void> executeRequest(SetEntityFieldRequest rq) {
        return execute(rq.getEntity(), rq.getLeftExpression(), rq.getRightExpression(), rq.getConfirmationText(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity entity, Expression<Entity> leftExpression, Expression<Entity> rightExpression, String confirmationText, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        if (confirmationText == null) {
            return updateAndSave(entity, leftExpression, rightExpression, null, parentContainer);
        }
        DialogContent dialogContent = new DialogContent().setContent(new Text(confirmationText));
        boolean[] executing = { false };
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(() -> {
            if (!executing[0])
                promise.fail(new UserCancellationException());
        });
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            executing[0] = true;
            updateAndSave(entity, leftExpression, rightExpression, dialogCallback, parentContainer)
                    .onFailure(promise::fail)
                    .onSuccess(promise::complete);
        });
        return promise.future();
    }

    private static Future<Void> updateAndSave(Entity entity, Expression<Entity> leftExpression, Expression<Entity> rightExpression, DialogCallback dialogCallback, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        entity.onExpressionLoaded(rightExpression)
            .onFailure(cause -> {
                reportException(dialogCallback, parentContainer, cause);
                promise.fail(cause);
            })
            .onSuccess(v -> {
                UpdateStore updateStore = UpdateStore.createAbove(entity.getStore());
                Entity updateEntity = updateStore.updateEntity(entity);
                leftExpression.setValue(updateEntity, rightExpression.evaluate(updateEntity, updateStore.getEntityDataWriter()), updateStore.getEntityDataWriter());
                updateStore.submitChanges(SubmitArgument.builder()
                        .setStatement("select set_transaction_parameters(true)")
                        .setDataSourceId(entity.getStore().getDataSourceId())
                        .build())
                    .onFailure(cause -> {
                        reportException(dialogCallback, parentContainer, cause);
                        promise.fail(cause);
                    })
                    .onSuccess(b -> {
                        if (dialogCallback != null)
                            dialogCallback.closeDialog();
                        promise.complete();
                    });
            });
        return promise.future();
    }

    private static void reportException(DialogCallback dialogCallback, Pane parentContainer, Throwable cause) {
        if (dialogCallback != null)
            dialogCallback.showException(cause);
        else
            AlertUtil.showExceptionAlert(cause, parentContainer.getScene().getWindow());
    }
}
