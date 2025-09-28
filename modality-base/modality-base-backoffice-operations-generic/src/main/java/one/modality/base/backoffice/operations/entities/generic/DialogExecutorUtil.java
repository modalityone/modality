package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.alert.AlertUtil;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.exceptions.UserCancellationException;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public final class DialogExecutorUtil {

    public static Future<Void> executeOnUserConfirmation(String confirmationText, Pane parentContainer, Supplier<Future<?>> executor) {
        Promise<Void> promise = Promise.promise();
        UiScheduler.runInUiThread(() -> {
            DialogContent dialogContent = new DialogContent().setHeaderText(I18n.getI18nText(BaseI18nKeys.AreYouSure)).setContentText(confirmationText);
            boolean[] executing = {false};
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(() -> {
                if (!executing[0])
                    promise.fail(new UserCancellationException());
            });
            DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                executing[0] = true;
                Button executingButton = dialogContent.getPrimaryButton();
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    executor.get()
                        .inUiThread()
                        .onFailure(cause -> {
                            promise.fail(cause);
                            reportException(dialogCallback, parentContainer, cause); // Actually, just print stack trace for now...
                            if (dialogCallback != null) // So we close the window
                                dialogCallback.closeDialog();
                        })
                        .onSuccess(b -> {
                            promise.complete();
                            if (dialogCallback != null)
                                dialogCallback.closeDialog();
                        })
                    , executingButton, dialogContent.getSecondaryButton());
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
