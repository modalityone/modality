package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;

/**
 * Utility class for managing dialog creation and display in the Reception module.
 *
 * @author David Hello
 * @author Claude Code
 */
public final class ReceptionDialogManager {

    private ReceptionDialogManager() {
        // Utility class - prevent instantiation
    }

    /**
     * Interface for dialogs that can be managed by ReceptionDialogManager.
     */
    public interface ManagedDialog {
        /**
         * Builds and returns the dialog's UI content.
         */
        Node buildView();

        /**
         * Returns a property indicating whether the action can be performed.
         */
        BooleanProperty canProceedProperty();

        /**
         * Sets a callback to be invoked after successful action.
         */
        void setOnSuccessCallback(Runnable callback);

        /**
         * Performs the primary action (e.g., check-in, check-out, payment).
         */
        void performAction(DialogCallback dialogCallback);

        /**
         * Gets the primary button text (e.g., "Check In", "Check Out", "Confirm Payment").
         */
        String getPrimaryButtonText();

        /**
         * Gets the cancel button text.
         */
        default String getCancelButtonText() {
            return "Cancel";
        }
    }

    /**
     * Opens a dialog with custom action buttons.
     *
     * @param dialog The dialog to open
     * @param onSuccessRefresh Callback to invoke after successful action
     */
    public static void openDialog(ManagedDialog dialog, Runnable onSuccessRefresh) {
        dialog.setOnSuccessCallback(onSuccessRefresh);

        DialogContent dialogContent = new DialogContent()
                .setContent(dialog.buildView());
        dialogContent.setCustomButtons(dialog.getPrimaryButtonText(), dialog.getCancelButtonText());

        // Bind primary button enabled state
        dialogContent.getPrimaryButton()
                .disableProperty()
                .bind(dialog.canProceedProperty().not());

        DialogBuilderUtil.showModalNodeInGoldLayout(
                dialogContent,
                FXMainFrameDialogArea.getDialogArea()
        );

        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            dialog.performAction(dialogCallback);
        });
    }

    /**
     * Opens a simple confirmation dialog.
     *
     * @param content The dialog content node
     * @param primaryButtonText Primary button text
     * @param onConfirm Callback when confirmed
     */
    public static void openConfirmationDialog(Node content, String primaryButtonText, Runnable onConfirm) {
        DialogContent dialogContent = new DialogContent()
                .setContent(content);
        dialogContent.setCustomButtons(primaryButtonText, "Cancel");

        DialogBuilderUtil.showModalNodeInGoldLayout(
                dialogContent,
                FXMainFrameDialogArea.getDialogArea()
        );

        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            onConfirm.run();
            dialogCallback.closeDialog();
        });
    }
}
