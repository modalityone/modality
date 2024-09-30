package one.modality.base.client.util.masterslave;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.masterslave.SlaveEditor;
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

import java.util.function.Consumer;

public abstract class ModalitySlaveEditor<T> implements SlaveEditor<T> {

    @Override
    public void showSlaveSwitchApprovalDialog(Consumer approvalCallback) {
        Text titleConfirmationText = I18n.bindI18nProperties(new Text(),"AreYouSure");
        Bootstrap.textSuccess(Bootstrap.h3(Bootstrap.strong(titleConfirmationText)));
        BorderPane dialog = new BorderPane();
        dialog.setTop(titleConfirmationText);
        BorderPane.setAlignment(titleConfirmationText, Pos.CENTER);
        Text confirmationText = I18n.bindI18nProperties(new Text(),"CancelChangesConfirmation");
        dialog.setCenter(confirmationText);
        BorderPane.setAlignment(confirmationText, Pos.CENTER);
        BorderPane.setMargin(confirmationText, new Insets(30, 0, 30, 0));
        Button okButton = Bootstrap.largeDangerButton(I18nControls.bindI18nProperties(new Button(),"Confirm"));
        Button cancelActionButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nTextProperty(new Button(),"Cancel"));

        HBox buttonsHBox = new HBox(cancelActionButton, okButton);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setSpacing(30);
        dialog.setBottom(buttonsHBox);
        BorderPane.setAlignment(buttonsHBox, Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        okButton.setOnAction(l -> {
            dialogCallback.closeDialog();
            approvalCallback.accept(true);
        });
        cancelActionButton.setOnAction(l -> {
            dialogCallback.closeDialog();
            approvalCallback.accept(false);
        });
    }
}
