package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDateTime;

/**
 * Modal dialog for collecting payment from a guest.
 * Features orange-themed summary box and partial payment warning.
 * Allows specifying amount and payment method (cash/card).
 *
 * @author David Hello
 * @author Claude Code
 */
public class PaymentModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Document document;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(false);
    private Runnable onSuccessCallback;

    // Guest info derived from document
    private final String guestName;
    private final String roomInfo;
    private final String eventName;
    private final Integer balance;
    private final Integer totalAmount;
    private final Integer paidAmount;

    // UI components
    private TextField amountField;
    private ToggleButton cashButton;
    private ToggleButton cardButton;
    private Label partialWarningLabel;

    public PaymentModal(DataSourceModel dataSourceModel, Document document, Integer paidAmount) {
        this(dataSourceModel, document, paidAmount, null, null);
    }

    public PaymentModal(DataSourceModel dataSourceModel, Document document, Integer paidAmount,
                        String roomInfo, String eventName) {
        this.dataSourceModel = dataSourceModel;
        this.document = document;
        this.paidAmount = paidAmount != null ? paidAmount : 0;
        this.roomInfo = roomInfo;
        this.eventName = eventName;

        // Build guest name
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        this.guestName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();

        // Calculate balance
        Integer priceNet = document.getPriceNet();
        this.totalAmount = priceNet != null ? priceNet : 0;
        this.balance = this.totalAmount - this.paidAmount;
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(420);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Orange-themed balance summary
        VBox summaryBox = buildSummaryBox();

        // Payment method selector
        VBox methodSection = buildMethodSection();

        // Amount field
        VBox amountSection = buildAmountSection();

        container.getChildren().addAll(header, summaryBox, methodSection, amountSection);

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\uD83D\uDCB3"); // Credit card icon
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.CollectPayment);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label guestLabel = new Label(guestName);
        guestLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, guestLabel);

        return header;
    }

    private VBox buildSummaryBox() {
        // Orange-themed summary box (like the JSX mockup)
        VBox summaryBox = new VBox(ReceptionStyles.SPACING_SM);
        summaryBox.setPadding(new Insets(16));
        summaryBox.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 8;"); // Orange/warning theme

        // Balance due (large)
        VBox balanceSection = new VBox(2);

        Label balanceLabel = new Label("BALANCE DUE");
        balanceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");

        Label balanceValue = new Label("€" + balance);
        balanceValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #856404;");

        balanceSection.getChildren().addAll(balanceLabel, balanceValue);

        // Additional info row
        HBox infoRow = new HBox(ReceptionStyles.SPACING_MD);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        if (roomInfo != null && !roomInfo.isEmpty()) {
            Label roomLabel = new Label("Room: " + roomInfo);
            roomLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");
            infoRow.getChildren().add(roomLabel);
        }

        if (eventName != null && !eventName.isEmpty()) {
            Label eventLabel = new Label("Event: " + eventName);
            eventLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");
            infoRow.getChildren().add(eventLabel);
        }

        summaryBox.getChildren().add(balanceSection);
        if (!infoRow.getChildren().isEmpty()) {
            summaryBox.getChildren().add(infoRow);
        }

        // Total/Paid breakdown
        HBox breakdownRow = new HBox(ReceptionStyles.SPACING_LG);
        breakdownRow.setPadding(new Insets(8, 0, 0, 0));

        Label totalText = new Label("Total: €" + totalAmount);
        totalText.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");

        Label paidText = new Label("Paid: €" + paidAmount);
        paidText.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");

        breakdownRow.getChildren().addAll(totalText, paidText);
        summaryBox.getChildren().add(breakdownRow);

        return summaryBox;
    }

    private VBox buildMethodSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(8, 0, 0, 0));

        Label methodLabel = new Label("Payment method");
        methodLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Payment method toggle buttons
        HBox methodButtons = new HBox(ReceptionStyles.SPACING_SM);

        cardButton = new ToggleButton("💳 Card");
        cardButton.setPrefWidth(100);
        cardButton.setPadding(new Insets(10, 16, 10, 16));
        cardButton.setSelected(true);
        cardButton.setOnAction(e -> {
            cardButton.setSelected(true);
            cashButton.setSelected(false);
            updateButtonStyles();
        });

        cashButton = new ToggleButton("💵 Cash");
        cashButton.setPrefWidth(100);
        cashButton.setPadding(new Insets(10, 16, 10, 16));
        cashButton.setOnAction(e -> {
            cashButton.setSelected(true);
            cardButton.setSelected(false);
            updateButtonStyles();
        });

        updateButtonStyles();

        methodButtons.getChildren().addAll(cardButton, cashButton);

        section.getChildren().addAll(methodLabel, methodButtons);

        return section;
    }

    private void updateButtonStyles() {
        if (cardButton.isSelected()) {
            cardButton.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-background-radius: 8; -fx-border-color: #0d6efd; -fx-border-radius: 8;");
            cashButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        } else {
            cashButton.setStyle("-fx-background-color: #d1e7dd; -fx-text-fill: #198754; -fx-background-radius: 8; -fx-border-color: #198754; -fx-border-radius: 8;");
            cardButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        }
    }

    private VBox buildAmountSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);
        section.setPadding(new Insets(8, 0, 0, 0));

        Label amountLabel = new Label("Amount");
        amountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        amountField = new TextField();
        amountField.setPromptText("€" + (balance > 0 ? balance : 0));
        amountField.setStyle("-fx-font-size: 16px; -fx-padding: 12 14;");
        if (balance > 0) {
            amountField.setText(String.valueOf(balance));
        }

        // Validate amount input and show partial warning
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateForm();
            updatePartialWarning();
        });

        section.getChildren().addAll(amountLabel, amountField);

        // Partial payment warning
        partialWarningLabel = new Label();
        partialWarningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #fd7e14;");
        partialWarningLabel.setVisible(false);
        partialWarningLabel.setManaged(false);
        section.getChildren().add(partialWarningLabel);

        // Initial validation
        validateForm();

        return section;
    }

    private void updatePartialWarning() {
        try {
            int amount = Integer.parseInt(amountField.getText().trim());
            if (amount > 0 && amount < balance) {
                int remaining = balance - amount;
                partialWarningLabel.setText("Partial payment — €" + remaining + " will remain unpaid");
                partialWarningLabel.setVisible(true);
                partialWarningLabel.setManaged(true);
            } else if (amount > balance) {
                partialWarningLabel.setText("Amount exceeds balance due");
                partialWarningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc3545;");
                partialWarningLabel.setVisible(true);
                partialWarningLabel.setManaged(true);
            } else {
                partialWarningLabel.setVisible(false);
                partialWarningLabel.setManaged(false);
            }
        } catch (NumberFormatException e) {
            partialWarningLabel.setVisible(false);
            partialWarningLabel.setManaged(false);
        }
    }

    private void validateForm() {
        boolean valid = false;
        try {
            String amountText = amountField.getText();
            if (amountText != null && !amountText.trim().isEmpty()) {
                int amount = Integer.parseInt(amountText.trim());
                valid = amount > 0;
            }
        } catch (NumberFormatException e) {
            valid = false;
        }
        canProceed.set(valid);
    }

    @Override
    public BooleanProperty canProceedProperty() {
        return canProceed;
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @Override
    public void performAction(DialogCallback dialogCallback) {
        try {
            int amount = Integer.parseInt(amountField.getText().trim());
            boolean isCash = cashButton.isSelected();

            UpdateStore updateStore = UpdateStore.create(dataSourceModel);
            MoneyTransfer transfer = updateStore.insertEntity(MoneyTransfer.class);
            transfer.setDocument(document);
            transfer.setAmount(amount);
            transfer.setMethod(isCash ? 0 : 1); // 0=cash, 1=card
            transfer.setDate(LocalDateTime.now());

            updateStore.submitChanges()
                    .onFailure(error -> {
                        Console.log("Error recording payment: " + error.getMessage());
                        dialogCallback.closeDialog();
                    })
                    .onSuccess(result -> {
                        Console.log("Payment recorded successfully: €" + amount + " (" + (isCash ? "cash" : "card") + ")");
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        dialogCallback.closeDialog();
                    });

        } catch (NumberFormatException e) {
            Console.log("Invalid amount: " + amountField.getText());
            dialogCallback.closeDialog();
        }
    }

    @Override
    public String getPrimaryButtonText() {
        try {
            int amount = Integer.parseInt(amountField.getText().trim());
            if (amount == balance) {
                return "Pay full €" + amount;
            }
            return "Pay €" + amount;
        } catch (Exception e) {
            return "Confirm Payment";
        }
    }
}
