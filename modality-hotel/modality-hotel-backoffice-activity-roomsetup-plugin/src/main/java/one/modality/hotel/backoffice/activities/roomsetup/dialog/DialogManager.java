package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;

/**
 * Utility class for managing dialog creation and display in a consistent manner.
 * Implements the Factory pattern to centralize dialog opening logic.
 *
 * <p>This eliminates ~20 lines of duplicated dialog setup code from each view.
 *
 * @author Claude Code
 */
public final class DialogManager {

    private DialogManager() {
        // Utility class - prevent instantiation
    }

    /**
     * Interface for dialogs that can be managed by DialogManager.
     * All dialogs in the room setup module should implement this interface.
     */
    public interface ManagedDialog {
        /**
         * Builds and returns the dialog's UI content.
         */
        Node buildView();

        /**
         * Returns a property indicating whether the form has unsaved changes.
         */
        BooleanProperty hasChangesProperty();

        /**
         * Sets a callback to be invoked after successful save.
         */
        void setOnSaveCallback(Runnable callback);

        /**
         * Returns true if the dialog should attempt to save.
         */
        boolean shouldSave();

        /**
         * Returns true if the dialog should attempt to delete.
         */
        default boolean shouldDelete() {
            return false;
        }

        /**
         * Saves the dialog's data.
         */
        void save(DialogCallback dialogCallback);

        /**
         * Deletes the entity (if supported).
         */
        default void delete(DialogCallback dialogCallback) {
            dialogCallback.closeDialog();
        }
    }

    /**
     * Opens a dialog with standard Save/Cancel buttons.
     *
     * @param dialog The dialog to open
     * @param onSaveRefresh Callback to invoke after successful save (e.g., refresh data)
     */
    public static void openDialog(ManagedDialog dialog, Runnable onSaveRefresh) {
        openDialog(dialog, onSaveRefresh, false);
    }

    /**
     * Opens a dialog with standard Save/Cancel buttons and optional delete support.
     *
     * @param dialog The dialog to open
     * @param onSaveRefresh Callback to invoke after successful save (e.g., refresh data)
     * @param supportsDelete Whether the dialog supports deletion
     */
    public static void openDialog(ManagedDialog dialog, Runnable onSaveRefresh, boolean supportsDelete) {
        dialog.setOnSaveCallback(onSaveRefresh);

        DialogContent dialogContent = new DialogContent()
                .setContent(dialog.buildView());
        dialogContent.setCustomButtons("Save", "Cancel");

        // Disable Save button until there are changes
        dialogContent.getPrimaryButton()
                .disableProperty()
                .bind(dialog.hasChangesProperty().not());

        DialogBuilderUtil.showModalNodeInGoldLayout(
                dialogContent,
                FXMainFrameDialogArea.getDialogArea()
        );

        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            if (dialog.shouldSave()) {
                dialog.save(dialogCallback);
            } else if (supportsDelete && dialog.shouldDelete()) {
                dialog.delete(dialogCallback);
            } else {
                dialogCallback.closeDialog();
            }
        });
    }

}
