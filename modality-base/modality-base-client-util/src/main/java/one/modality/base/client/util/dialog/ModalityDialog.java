package one.modality.base.client.util.dialog;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;

/**
 * @author Bruno Salmon
 */
public class ModalityDialog {

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, Runnable onConfirmed) {
        showConfirmationDialog(confirmationMessageI18nKey, onConfirmed, null);
    }

    public static void showConfirmationDialog(Object confirmationMessageI18nKey, Runnable onConfirmed, Runnable onCancelled) {
        Text titleConfirmationText = I18n.bindI18nProperties(new Text(), "AreYouSure"); // ???
        Bootstrap.textSuccess(Bootstrap.strong(Bootstrap.h3(titleConfirmationText)));
        BorderPane dialog = new BorderPane();
        dialog.setTop(titleConfirmationText);
        BorderPane.setAlignment(titleConfirmationText, Pos.CENTER);
        Text confirmationText = I18n.bindI18nProperties(new Text(), confirmationMessageI18nKey);
        dialog.setCenter(confirmationText);
        BorderPane.setAlignment(confirmationText, Pos.CENTER);
        BorderPane.setMargin(confirmationText, new Insets(30, 0, 30, 0));
        Button confirmButton = Bootstrap.largeDangerButton(I18nControls.bindI18nProperties(new Button(), "Confirm")); // ???
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), "Cancel")); // ???

        HBox buttonsHBox = new HBox(cancelButton, confirmButton);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setSpacing(30);
        dialog.setBottom(buttonsHBox);
        BorderPane.setAlignment(buttonsHBox, Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        confirmButton.setOnAction(e -> runClosingDialogAction(dialogCallback, onConfirmed));
        cancelButton.setOnAction(e -> runClosingDialogAction(dialogCallback, onCancelled));
    }

    private static void runClosingDialogAction(DialogCallback dialogCallback, Runnable action) {
        dialogCallback.closeDialog();
        if (action != null)
            action.run();
    }


}
