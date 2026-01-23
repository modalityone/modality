package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.extras.async.AsyncDialog;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.geometry.HPos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Letter;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.ConfirmDocumentEvent;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class ToggleConfirmDocumentExecutor {

    static Future<Void> executeRequest(ToggleConfirmDocumentRequest rq) {
        return Future.all(
            rq.getDocument().<Document>onExpressionLoaded("confirmed,passReady"),
            rq.getDocument().getStore().executeQuery("select subject_en from Letter where event=? and active and type.confirmation", rq.getDocument().getEvent())
        ).compose(compositeFuture -> {
            Document document = compositeFuture.resultAt(0);
            List<Letter> letters = compositeFuture.resultAt(1);
            boolean confirmed = !document.isConfirmed(); // toggling confirmed
            boolean read = !document.isPassReady(); // read if pass is not ready, otherwise
            String confirmationText = "Are you sure you want to " + (confirmed ? "confirm" : "unconfirm") + " this booking?";
            DialogContent dialogContent = new DialogContent()
                .setHeaderText(I18n.getI18nText(BaseI18nKeys.AreYouSure))
                .setContentText(confirmationText)
                .setYesNo();
            Letter confirmationLetter = letters.size() == 1 ? letters.get(0) : null;
            CheckBox sendConfirmationLetterCheckBox;
            if (confirmed && confirmationLetter != null) {
                sendConfirmationLetterCheckBox = new CheckBox("Send the confirmation letter");
                sendConfirmationLetterCheckBox.setSelected(true);
                dialogContent.setContent(sendConfirmationLetterCheckBox);
                GridPane.setHalignment(sendConfirmationLetterCheckBox, HPos.CENTER);
            } else
                sendConfirmationLetterCheckBox = null;
            return AsyncDialog.showDialogWithAsyncOperationOnPrimaryButton(dialogContent, rq.getParentContainer(), () -> {
                boolean sendConfirmationLetter = sendConfirmationLetterCheckBox != null && sendConfirmationLetterCheckBox.isSelected();
                Future<?> preDocumentSubmit;
                if (sendConfirmationLetter) {
                    UpdateStore updateStore = UpdateStore.createAbove(document.getStore());
                    document.setForeignField("triggerSendLetter", confirmationLetter);
                    preDocumentSubmit = updateStore.submitChanges();
                } else
                    preDocumentSubmit = Future.succeededFuture();
                return preDocumentSubmit.compose(ignored ->
                    DocumentService.submitDocumentChanges(
                        new SubmitDocumentChangesArgument(
                            sendConfirmationLetter ? "Sent '" + confirmationLetter.getFieldValue("subject_en") + "'" : confirmed ? "Confirmed booking" : "Unconfirmed booking",
                            new ConfirmDocumentEvent(document, confirmed, read)
                        )
                    ));
            });
        });
    }
}
