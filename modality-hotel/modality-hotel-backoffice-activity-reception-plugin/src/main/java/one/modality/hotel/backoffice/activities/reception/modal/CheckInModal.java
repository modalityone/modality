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
 * Modal dialog for checking in a guest.
 * Displays guest details including room, meals, diet, and payment info.
 * Allows collecting payment during check-in if there's a balance.
 *
 * @author David Hello
 * @author Claude Code
 */
public class CheckInModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Document document;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(true);
    private Runnable onSuccessCallback;

    // Guest info derived from document
    private final String guestName;
    private final String roomInfo;
    private final String roomType;
    private final String mealsInfo;
    private final String dietInfo;
    private final Integer balance;
    private final Integer totalAmount;
    private final Integer paidAmount;
    private final boolean isOffsite;

    // UI components for payment
    private ToggleButton cashButton;
    private ToggleButton cardButton;
    private TextField amountField;
    private Label partialWarningLabel;
    private boolean isSubmitting = false;

    public CheckInModal(DataSourceModel dataSourceModel, Document document, String roomName, String roomType,
                        Integer paidAmount, String mealsInfo, String dietInfo, boolean isOffsite) {
        this.dataSourceModel = dataSourceModel;
        this.document = document;
        this.roomType = roomType;
        this.mealsInfo = mealsInfo;
        this.dietInfo = dietInfo;
        this.isOffsite = isOffsite;

        // Build guest name
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        this.guestName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();

        // Build room info
        if (roomName != null && roomType != null) {
            this.roomInfo = roomName + " (" + roomType + ")";
        } else if (roomName != null) {
            this.roomInfo = roomName;
        } else if (roomType != null) {
            this.roomInfo = roomType;
        } else {
            this.roomInfo = isOffsite ? "Offsite" : "—";
        }

        // Calculate balance
        Integer priceNet = document.getPriceNet();
        this.totalAmount = priceNet != null ? priceNet : 0;
        this.paidAmount = paidAmount != null ? paidAmount : 0;
        this.balance = this.totalAmount - this.paidAmount;
    }

    // Simplified constructor for backward compatibility
    public CheckInModal(DataSourceModel dataSourceModel, Document document, String roomName, String roomType, Integer paidAmount) {
        this(dataSourceModel, document, roomName, roomType, paidAmount, null, null, false);
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(420);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        container.getChildren().add(header);

        // Offsite indicator (if applicable)
        if (isOffsite) {
            HBox offsiteIndicator = buildOffsiteIndicator();
            container.getChildren().add(offsiteIndicator);
        }

        // Guest details
        VBox details = buildDetails();
        container.getChildren().add(details);

        // Payment section (if balance > 0)
        if (balance > 0) {
            VBox paymentSection = buildPaymentSection();
            container.getChildren().add(paymentSection);
        }

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\u2713"); // Checkmark
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #198754;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.CheckInGuest);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label guestLabel = new Label(guestName);
        guestLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, guestLabel);

        return header;
    }

    private HBox buildOffsiteIndicator() {
        HBox indicator = new HBox(ReceptionStyles.SPACING_SM);
        indicator.setAlignment(Pos.CENTER_LEFT);
        indicator.setPadding(new Insets(10, 14, 10, 14));
        indicator.setStyle("-fx-background-color: #e2d4f0; -fx-background-radius: 8;");

        Label sunIcon = new Label("☀"); // Sun icon for offsite
        sunIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #6f42c1;");

        Label text = new Label("Offsite participant — not staying on site");
        text.setStyle("-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: #6f42c1;");

        indicator.getChildren().addAll(sunIcon, text);

        return indicator;
    }

    private VBox buildDetails() {
        VBox details = new VBox(ReceptionStyles.SPACING_SM);
        details.setPadding(new Insets(16));
        details.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Room (if not offsite)
        if (!isOffsite) {
            HBox roomRow = buildDetailRow("Room", roomInfo);
            details.getChildren().add(roomRow);
        }

        // Meals
        if (mealsInfo != null && !mealsInfo.isEmpty()) {
            HBox mealsRow = buildDetailRow("Meals", mealsInfo);
            details.getChildren().add(mealsRow);
        }

        // Diet (if specified)
        if (dietInfo != null && !dietInfo.isEmpty() && !dietInfo.equalsIgnoreCase("standard")) {
            HBox dietRow = buildDetailRow("Diet", dietInfo);
            Label dietValue = (Label) dietRow.getChildren().get(2);
            dietValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0d6efd;");
            details.getChildren().add(dietRow);
        }

        // Separator
        Region separator1 = new Region();
        separator1.setPrefHeight(1);
        separator1.setStyle("-fx-background-color: #dee2e6;");
        separator1.setPadding(new Insets(8, 0, 8, 0));
        details.getChildren().add(separator1);

        // Total
        HBox totalRow = buildDetailRow("Total", "€" + totalAmount);
        details.getChildren().add(totalRow);

        // Paid
        HBox paidRow = buildDetailRow("Paid", "€" + paidAmount);
        details.getChildren().add(paidRow);

        // Separator
        Region separator2 = new Region();
        separator2.setPrefHeight(1);
        separator2.setStyle("-fx-background-color: #dee2e6;");
        separator2.setPadding(new Insets(8, 0, 8, 0));
        details.getChildren().add(separator2);

        // Balance
        HBox balanceRow = buildDetailRow("Balance", "€" + balance);
        Label balanceLabel = (Label) balanceRow.getChildren().get(0);
        Label balanceValue = (Label) balanceRow.getChildren().get(2);
        balanceLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");
        if (balance > 0) {
            balanceValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0d6efd;");
        } else {
            balanceValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #198754;");
        }
        details.getChildren().add(balanceRow);

        return details;
    }

    private HBox buildDetailRow(String label, String value) {
        HBox row = new HBox(ReceptionStyles.SPACING_SM);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        labelNode.setMinWidth(60);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 11px;");

        row.getChildren().addAll(labelNode, spacer, valueLabel);

        return row;
    }

    private VBox buildPaymentSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(16, 0, 0, 0));

        // Payment method label
        Label methodLabel = new Label("Payment method");
        methodLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Payment method buttons
        HBox methodButtons = new HBox(ReceptionStyles.SPACING_SM);

        cashButton = new ToggleButton("💵 Cash");
        cashButton.setPrefWidth(100);
        cashButton.setPadding(new Insets(10, 16, 10, 16));
        cashButton.setOnAction(e -> {
            cashButton.setSelected(true);
            cardButton.setSelected(false);
            updatePaymentButtonStyles();
        });

        cardButton = new ToggleButton("💳 Card");
        cardButton.setPrefWidth(100);
        cardButton.setPadding(new Insets(10, 16, 10, 16));
        cardButton.setSelected(true);
        cardButton.setOnAction(e -> {
            cardButton.setSelected(true);
            cashButton.setSelected(false);
            updatePaymentButtonStyles();
        });

        updatePaymentButtonStyles();

        methodButtons.getChildren().addAll(cashButton, cardButton);

        // Amount field
        VBox amountSection = new VBox(4);
        amountSection.setPadding(new Insets(8, 0, 0, 0));

        Label amountLabel = new Label("Amount");
        amountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        amountField = new TextField(String.valueOf(balance));
        amountField.setStyle("-fx-font-size: 13px; -fx-padding: 10 14;");
        amountField.textProperty().addListener((obs, old, newVal) -> {
            updatePartialWarning();
        });

        amountSection.getChildren().addAll(amountLabel, amountField);

        // Partial payment warning
        partialWarningLabel = new Label();
        partialWarningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #fd7e14;");
        partialWarningLabel.setVisible(false);
        partialWarningLabel.setManaged(false);

        section.getChildren().addAll(methodLabel, methodButtons, amountSection, partialWarningLabel);

        return section;
    }

    private void updatePaymentButtonStyles() {
        if (cardButton.isSelected()) {
            cardButton.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-background-radius: 8; -fx-border-color: #0d6efd; -fx-border-radius: 8;");
            cashButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        } else {
            cashButton.setStyle("-fx-background-color: #d1e7dd; -fx-text-fill: #198754; -fx-background-radius: 8; -fx-border-color: #198754; -fx-border-radius: 8;");
            cardButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        }
    }

    private void updatePartialWarning() {
        try {
            int amount = Integer.parseInt(amountField.getText().trim());
            if (amount > 0 && amount < balance) {
                int remaining = balance - amount;
                partialWarningLabel.setText("Partial — €" + remaining + " remaining");
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
        if (isSubmitting) return;
        isSubmitting = true;

        UpdateStore updateStore = UpdateStore.create(dataSourceModel);

        // Update document to mark as arrived
        Document updatedDoc = updateStore.updateEntity(document);
        updatedDoc.setArrived(true);

        // Create payment if amount > 0 and balance > 0
        if (balance > 0 && amountField != null) {
            try {
                int paymentAmount = Integer.parseInt(amountField.getText().trim());
                if (paymentAmount > 0) {
                    boolean isCash = cashButton != null && cashButton.isSelected();
                    MoneyTransfer transfer = updateStore.insertEntity(MoneyTransfer.class);
                    transfer.setDocument(document);
                    transfer.setAmount(paymentAmount);
                    transfer.setMethod(isCash ? 0 : 1); // 0=cash, 1=card
                    transfer.setDate(LocalDateTime.now());
                }
            } catch (NumberFormatException e) {
                // No payment if invalid amount
            }
        }

        updateStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error checking in guest: " + error.getMessage());
                    isSubmitting = false;
                    dialogCallback.closeDialog();
                })
                .onSuccess(result -> {
                    Console.log("Guest checked in successfully: " + guestName);
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dialogCallback.closeDialog();
                });
    }

    @Override
    public String getPrimaryButtonText() {
        return isSubmitting ? "Processing..." : "Complete";
    }
}
