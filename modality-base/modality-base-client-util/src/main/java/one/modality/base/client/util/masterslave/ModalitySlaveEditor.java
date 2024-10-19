package one.modality.base.client.util.masterslave;

import dev.webfx.extras.util.masterslave.AbstractSlaveEditor;
import one.modality.base.client.util.dialog.ModalityDialog;

import java.util.function.Consumer;

public abstract class ModalitySlaveEditor<T> extends AbstractSlaveEditor<T> {

    @Override
    public void showSlaveSwitchApprovalDialog(Consumer<Boolean> approvalCallback) {
        ModalityDialog.showConfirmationDialog("CancelChangesConfirmation", () -> approvalCallback.accept(true), () -> approvalCallback.accept(false));
    }
}
