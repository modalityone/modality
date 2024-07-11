package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.ui.controls.alert.AlertUtil;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.exceptions.UserCancellationException;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public final class DialogExecutorUtil {

    public static Future<Void> executeOnUserConfirmation(String confirmationText, Pane parentContainer, Supplier<Future<?>> executor) {
        Promise<Void> promise = Promise.promise();
        DialogContent dialogContent = new DialogContent().setContent(new Text(confirmationText));
        boolean[] executing = { false };
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(() -> {
            if (!executing[0])
                promise.fail(new UserCancellationException());
        });
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            executing[0] = true;
            executor.get()
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
