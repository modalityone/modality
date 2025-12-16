package one.modality.ecommerce.frontoffice.order;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.event.client.lifecycle.EventLifeCycle;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public final class OrderActions {

    public static Label newContactUsLabel(Document orderDocument) {
        Label contactUsLabel = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.ContactUsAboutThisBooking)));
        setupContactUsLabel(contactUsLabel, orderDocument);
        return contactUsLabel;
    }

    public static void setupContactUsLabel(Labeled contactUsLabel, Document orderDocument) {
        setupLabeled(contactUsLabel, true, e -> {
            ContactUsDialog contactUsWindow = new ContactUsDialog();
            contactUsWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(contactUsWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            contactUsWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());

            contactUsWindow.getSendButton().setOnAction(ae -> {
                Document d = orderDocument;
                UpdateStore updateStore = UpdateStore.createAbove(d.getStore());
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(d.getFirstName() + ' ' + d.getLastName());
                email.setFromEmail(d.getEmail());
                email.setSubject("[" + Entities.getPrimaryKey(d.getEvent()) + "-" + d.getRef() + "] " + contactUsWindow.getSubject());
                email.setOut(false);
                email.setDocument(d);
                String content = contactUsWindow.getMessage();
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                Person userPerson = FXUserPerson.getUserPerson();
                if (userPerson != null) {
                    history.setUserPerson(userPerson);
                } else
                    history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(d);
                history.setMail(email);

                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> contactUsWindow.displaySuccessMessage(5000, messageWindowCallback::closeDialog)),
                    contactUsWindow.getSendButton(), contactUsWindow.getCancelButton());
            });
        });
    }

    public static Button newModifyOrderButton(Object orderDocumentOrPrimaryKey) {
        Button modifyOrderButton = Bootstrap.secondaryButton(I18nControls.newButton(OrderI18nKeys.AddOrEditOption));
        setupModifyOrderButton(modifyOrderButton, orderDocumentOrPrimaryKey);
        return modifyOrderButton;
    }

    public static void setupModifyOrderButton(ButtonBase modifyOrderButton, Object orderDocumentOrPrimaryKey) {
        setupButton(modifyOrderButton, false, e ->
            WindowHistory.getProvider().push("/modify-order/" + Entities.getPrimaryKey(orderDocumentOrPrimaryKey)));
    }

    public static Button newMakePaymentButton(Object orderDocumentOrPrimaryKey) {
        Button makePaymentButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.MakePayment));
        setupMakePaymentButton(makePaymentButton, orderDocumentOrPrimaryKey);
        return makePaymentButton;
    }

    public static void setupMakePaymentButton(ButtonBase makePaymentButton, Object orderDocumentOrPrimaryKey) {
        setupButton(makePaymentButton, false, e ->
            WindowHistory.getProvider().push("/pay-order/" + Entities.getPrimaryKey(orderDocumentOrPrimaryKey)));
    }

    public static Button newLegacyCartButton(Document orderDocument) {
        Button legacyCartButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.LegacyCart));
        setupLegacyCartButton(legacyCartButton, orderDocument);
        return legacyCartButton;
    }

    public static void setupLegacyCartButton(ButtonBase legacyCartButton, Document orderDocument) {
        legacyCartButton.setGraphicTextGap(10);
        setupButton(legacyCartButton, false,e ->
            BrowserUtil.openExternalBrowser(EventLifeCycle.getKbs2BookingCartUrl(orderDocument)));
    }

    public static Button newAskRefundButton(Document orderDocument) {
        Button askRefundButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.AskARefund));
        setupAskRefundButton(askRefundButton, orderDocument);
        return askRefundButton;
    }

    public static void setupAskRefundButton(ButtonBase askRefundButton, Document orderDocument) {
        askRefundButton.setOnAction(e -> {
            Event event = orderDocument.getEvent();
            int totalPriceNet = orderDocument.getPriceNet();
            int deposit = orderDocument.getPriceDeposit();
            int remainingAmount = totalPriceNet - deposit;
            String formattedPrice = EventPriceFormatter.formatWithCurrency(remainingAmount, event);
            RefundDialog refundWindow = new RefundDialog(formattedPrice, String.valueOf(orderDocument.getRef()), event);
            refundWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(refundWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            refundWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());
            refundWindow.getRefundButton().setOnAction(m -> {
                Document d = orderDocument;
                UpdateStore updateStore = UpdateStore.createAbove(d.getStore());
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(d.getFirstName() + ' ' + d.getLastName());
                email.setFromEmail(d.getEmail());
                email.setSubject("[" + Entities.getPrimaryKey(d.getEvent()) + "-" + d.getRef() + "] Refund of " + formattedPrice + " requested");
                email.setOut(false);
                email.setDocument(d);
                String content = "The user has requested a refund for his canceled booking. Amount : " + formattedPrice;
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(d);
                history.setMail(email);

                //TODO: prevent the Refund to display if the refund as already been requested, and display somewhere in the interface that the refund has been requested
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayRefundSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton(), refundWindow.getCancelButton());

            });
            refundWindow.getDonateButton().setOnAction(ae -> {
                UpdateStore updateStore = UpdateStore.createAbove(orderDocument.getStore());
                //TODO implementation
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayDonationSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton());

            });
        });
    }

    public static Label newCancelOrderLabel(Document orderDocument, Supplier<Future<?>> loadFromDatabaseFunction) {
        Label cancelOrderLabel = Bootstrap.textDanger(I18nControls.newLabel(OrderI18nKeys.CancelBooking));
        setupCancelOrderButton(cancelOrderLabel, orderDocument, loadFromDatabaseFunction);
        return cancelOrderLabel;
    }

    public static void setupCancelOrderButton(Labeled cancelOrderLabel, Document orderDocument, Supplier<Future<?>> loadFromDatabaseFunction) {
        cancelOrderLabel.setCursor(Cursor.HAND);
        cancelOrderLabel.setWrapText(true);
        cancelOrderLabel.setOnMouseClicked(e -> {
            BorderPane errorDialog = new BorderPane();
            errorDialog.setMinWidth(500);
            ScalePane errorContainer = new ScalePane(errorDialog);

            errorDialog.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
            errorDialog.setPadding(new Insets(30));

            javafx.scene.control.Label title = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(I18nControls.newLabel(OrderI18nKeys.CancelBookingTitle))));
            title.setPadding(new Insets(20, 0, 0, 0));
            title.setWrapText(true);
            errorDialog.setTop(title);
            BorderPane.setAlignment(title, Pos.CENTER);
            javafx.scene.control.Label areYouSureLabel = Bootstrap.strong(Bootstrap.textSecondary(Bootstrap.h5(I18nControls.newLabel(OrderI18nKeys.CancelBookingAreYouSure))));
            areYouSureLabel.setWrapText(true);

            VBox content = new VBox(30, areYouSureLabel);
            content.setAlignment(Pos.CENTER);
            BorderPane.setAlignment(content, Pos.CENTER);
            BorderPane.setMargin(content, new Insets(30, 0, 30, 0));
            errorDialog.setCenter(content);

            Label cancelLabelText = Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(BaseI18nKeys.Cancel)));
            cancelLabelText.setCursor(Cursor.HAND);
            DialogCallback errorMessageCallback = DialogUtil.showModalNodeInGoldLayout(errorContainer, FXMainFrameDialogArea.getDialogArea());
            cancelLabelText.setOnMouseClicked(e2 -> errorMessageCallback.closeDialog());

            Button confirmButton = Bootstrap.largeDangerButton(I18nControls.newButton(BaseI18nKeys.Confirm));
            confirmButton.setOnAction(ae ->
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    WorkingBooking.loadWorkingBooking(orderDocument)
                        .compose(workingBooking -> {
                            workingBooking.cancelBooking();
                            return workingBooking.submitChanges("Booking canceled online by user")
                                .compose(result -> loadFromDatabaseFunction.get());
                        })
                        .onFailure(Console::log)
                        .onComplete(ar -> {
                            // Close the dialog only after the operation completes (success or failure)
                            errorMessageCallback.closeDialog();
                        }), confirmButton, cancelLabelText));
            HBox buttonsHBox = new HBox(70, cancelLabelText, confirmButton);
            buttonsHBox.setPadding(new Insets(30, 20, 20, 20));
            buttonsHBox.setAlignment(Pos.CENTER);
            errorDialog.setBottom(buttonsHBox);
        });
    }

    private static void setupLabeled(Labeled button, boolean wrap, EventHandler<MouseEvent> onMouseClicked) {
        button.setCursor(Cursor.HAND);
        if (wrap)
            button.setWrapText(true);
        else
            button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnMouseClicked(onMouseClicked);
    }

    private static void setupButton(ButtonBase button, boolean wrap, EventHandler<ActionEvent> onAction) {
        setupLabeled(button, wrap, null);
        button.setOnAction(onAction);
    }

}
