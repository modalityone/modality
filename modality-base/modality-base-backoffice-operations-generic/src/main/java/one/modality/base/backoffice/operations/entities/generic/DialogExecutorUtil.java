package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.extras.async.AsyncDialog;
import dev.webfx.platform.async.Future;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public final class DialogExecutorUtil {

    public static Future<Void> executeOnUserConfirmation(String confirmationText, Pane parentContainer, Supplier<Future<?>> executor) {
        DialogContent dialogContent = new DialogContent()
            .setHeaderText(I18n.getI18nText(BaseI18nKeys.AreYouSure))
            .setContentText(confirmationText);
        return AsyncDialog.showDialogWithAsyncOperationOnPrimaryButton(dialogContent, parentContainer, executor);
    }

}
