package one.modality.ecommerce.frontoffice.order;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;
import one.modality.base.shared.entities.Event;

/**
 * @author David Hello
 */
final class RefundDialog extends BaseDialog {

    private final String refundAmount;
    private final String orderReference;
    private Label cancelLabel;
    private Button refundButton;
    private Button donateButton;

    public RefundDialog(String refundAmount, String orderReference, Event event) {
        super();
        this.refundAmount = refundAmount;
        this.orderReference = orderReference;
    }

    @Override
    protected double getPrefHeight() {
        return 500; // Smaller height for refund dialog
    }

    @Override
    public void buildUI() {
        // Header
        VBox header = createHeader(OrderI18nKeys.RefundRequest, null);

        // Add amount info to header
        Label amountLabel = Bootstrap.strong(I18nControls.newLabel(OrderI18nKeys.RemainingAmountForRefund,refundAmount));
        amountLabel.setTextFill(Color.WHITE);
        header.getChildren().add(amountLabel);

        dialogPane.setTop(header);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(32, 24, 24, 24));
        content.setAlignment(Pos.CENTER);

        // Explanation text
        Label explanationLabel = I18nControls.newLabel(OrderI18nKeys.RefundExplanation);
        explanationLabel.setWrapText(true);
        explanationLabel.setTextAlignment(TextAlignment.CENTER);
        explanationLabel.getStyleClass().add("thank-you-message");
        explanationLabel.setPadding(new Insets(0, 0, 20, 0));

        // Temple project link
        Hyperlink templeLink = new Hyperlink();
        I18nControls.bindI18nProperties(templeLink, OrderI18nKeys.TempleProjectTitle);
        templeLink.setOnAction(e -> BrowserUtil.openExternalBrowser(I18n.getI18nText(OrderI18nKeys.TempleProjectLink)));

        content.getChildren().addAll(explanationLabel, templeLink);
        dialogPane.setCenter(content);

        refundButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.RequestRefund));
        refundButton.setMinWidth(Region.USE_PREF_SIZE);

        donateButton = ModalityStyle.whiteButton(I18nControls.newButton(OrderI18nKeys.DonateToTempleProject));
        donateButton.setMinWidth(Region.USE_PREF_SIZE);

        // Button layout - side by side for main actions
        ColumnsPane actionButtonsColumnPane = new ColumnsPane(20);
        actionButtonsColumnPane.setMaxColumnCount(2);
        actionButtonsColumnPane.setVgap(15);
        actionButtonsColumnPane.setMinColumnWidth(300);
        actionButtonsColumnPane.getChildren().addAll(refundButton,donateButton);

        VBox buttonContainer = new VBox(12, actionButtonsColumnPane);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(0, 24, 24, 24));

        dialogPane.setBottom(buttonContainer);
    }

    public void displayRefundSuccessMessage(int duration, Runnable onFinished) {
        String contactEmail = "treasurer@kadampa.org";
        //TODO: add the treasurer email to the organization
        // String contactEmail = event.getOrganization().getTreasurerEmail(); // This should be configurable
        String refundMessage = I18n.getI18nText(OrderI18nKeys.RefundMessage,contactEmail);

        displaySuccessMessage(
            OrderI18nKeys.RefundRequested,
            refundMessage,
            OrderI18nKeys.ThisWindowWillCloseAutomatically,
            duration,
            onFinished
        );
    }

    public void displayDonationSuccessMessage(int duration, Runnable onFinished) {
        displaySuccessMessage(
            OrderI18nKeys.ThankYou,
            OrderI18nKeys.DonationSuccessMessage,
            OrderI18nKeys.ThisWindowWillCloseAutomatically,
            duration,
            onFinished
        );
    }

    public Label getCancelButton() {
        return cancelLabel;
    }

    protected VBox createHeader(Object titleKey, Object subtitleKey, String backgroundColor) {
        VBox header = new VBox(5);
        header.setPadding(new Insets(24));
        header.setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), new CornerRadii(12, 12, 0, 0, false), Insets.EMPTY)));

        Label titleLabel = Bootstrap.strong(Bootstrap.h3(I18nControls.newLabel(titleKey)));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        cancelLabel = Bootstrap.h3(new Label("x"));
        cancelLabel.setTextFill(Color.WHITE);
        cancelLabel.setCursor(Cursor.HAND);
        Region spacer = new Region();
        HBox.setHgrow(spacer,Priority.ALWAYS);
        HBox topLine = new HBox(titleLabel,spacer,cancelLabel);
        if (subtitleKey != null) {
            Label subtitleLabel = Bootstrap.strong(I18nControls.newLabel(subtitleKey));
            subtitleLabel.setTextFill(Color.WHITE);
            subtitleLabel.setWrapText(true);
            header.getChildren().addAll(topLine, subtitleLabel);
        } else {
            header.getChildren().add(topLine);
        }

        return header;
    }
    public Button getRefundButton() {
        return refundButton;
    }

    public Button getDonateButton() {
        return donateButton;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public String getOrderReference() {
        return orderReference;
    }
}