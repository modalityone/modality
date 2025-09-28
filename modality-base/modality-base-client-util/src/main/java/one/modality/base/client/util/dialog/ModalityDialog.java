package one.modality.base.client.util.dialog;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.async.AsyncSupplier;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.util.AsyncUtil;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;

/**
 * @author Bruno Salmon
 */
public final class ModalityDialog {

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, Runnable onConfirmed) {
        showConfirmationDialog(confirmationMessageI18nKey, onConfirmed, null);
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, Runnable onConfirmed, Runnable onCancelled) {
        showConfirmationDialog(confirmationMessageI18nKey, AsyncUtil.toAsyncSupplier(onConfirmed), AsyncUtil.toAsyncSupplier(onCancelled));
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, AsyncSupplier onConfirmed) {
        showConfirmationDialog(confirmationMessageI18nKey, onConfirmed, null);
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, AsyncSupplier onConfirmed, AsyncSupplier onCancelled) {
        showConfirmationDialog(confirmationMessageI18nKey
            , dialogCallback -> onConfirmed.get().onSuccess(x -> dialogCallback.closeDialog())
            , dialogCallback -> (onCancelled == null ? Future.succeededFuture() : onCancelled.get()).onSuccess(x -> dialogCallback.closeDialog())
        );
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, AsyncFunction<DialogCallback, ?> onConfirmed) {
        showConfirmationDialog(confirmationMessageI18nKey, onConfirmed, null);
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, AsyncFunction<DialogCallback, ?> onConfirmed, AsyncFunction<DialogCallback, ?> onCancelled) {
        Text titleConfirmationText = I18n.newText(BaseI18nKeys.AreYouSure);
        Bootstrap.textSuccess(Bootstrap.strong(Bootstrap.h3(titleConfirmationText)));
        BorderPane dialog = new BorderPane();
        dialog.setTop(titleConfirmationText);
        BorderPane.setAlignment(titleConfirmationText, Pos.CENTER);
        Text confirmationText = I18n.newText(confirmationMessageI18nKey);
        dialog.setCenter(confirmationText);
        BorderPane.setAlignment(confirmationText, Pos.CENTER);
        BorderPane.setMargin(confirmationText, new Insets(30, 0, 30, 0));
        Button confirmButton = Bootstrap.largeDangerButton(I18nControls.newButton(BaseI18nKeys.Confirm));
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(BaseI18nKeys.Cancel));

        HBox buttonsHBox = new HBox(cancelButton, confirmButton);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setSpacing(30);
        dialog.setBottom(buttonsHBox);
        BorderPane.setAlignment(buttonsHBox, Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        confirmButton.setOnAction(e -> runClosingDialogAction(dialogCallback, onConfirmed, confirmButton, cancelButton));
        cancelButton.setOnAction(e -> runClosingDialogAction(dialogCallback, onCancelled, cancelButton, confirmButton));
    }

    private static void runClosingDialogAction(DialogCallback dialogCallback, AsyncFunction<DialogCallback, ?> action, Labeled... buttons) {
        if (action != null)
            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(action.apply(dialogCallback), buttons);
    }

}
