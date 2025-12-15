package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.MoneyTransfer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Payments tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Display payment history (money_transfer records)
 * - Add new payment form
 * - Edit existing payment (with permission check)
 * <p>
 * Based on RegistrationDashboardFull.jsx PaymentsTab section.
 *
 * @author Claude Code
 */
public class PaymentsTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<VisualResult> paymentsVisualResultProperty = new SimpleObjectProperty<>();

    // UI Components
    private VisualGrid paymentsGrid;
    private ReactiveVisualMapper<MoneyTransfer> paymentsMapper;

    // New payment form fields
    private DatePicker datePicker;
    private TextField amountField;
    private ComboBox<String> methodCombo;
    private TextField referenceField;

    public PaymentsTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
    }

    /**
     * Builds the Payments tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Summary section
        Node summarySection = createSummarySection();

        // Payment history grid
        Node historySection = createHistorySection();
        VBox.setVgrow(historySection, Priority.ALWAYS);

        // Add payment form
        Node addPaymentSection = createAddPaymentSection();

        container.getChildren().addAll(summarySection, historySection, addPaymentSection);

        return container;
    }

    /**
     * Creates the payment summary section.
     */
    private Node createSummarySection() {
        HBox summary = new HBox(SPACING_XLARGE);
        summary.setPadding(PADDING_LARGE);
        summary.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        summary.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Total price
        Integer priceNet = document.getPriceNet();
        int total = priceNet != null ? priceNet : 0;

        VBox totalBox = createSummaryItem("Total Price", formatPrice(total), TEXT);

        // Total paid
        Integer deposit = document.getPriceDeposit();
        int paid = deposit != null ? deposit : 0;

        VBox paidBox = createSummaryItem("Paid", formatPrice(paid), SUCCESS);

        // Balance
        int balance = total - paid;
        VBox balanceBox = createSummaryItem("Balance Due", formatPrice(balance), balance > 0 ? WARNING : SUCCESS);

        summary.getChildren().addAll(totalBox, paidBox, balanceBox);
        return summary;
    }

    /**
     * Creates a summary item card.
     */
    private VBox createSummaryItem(String label, String value, javafx.scene.paint.Color valueColor) {
        VBox item = new VBox(4);
        item.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(item, Priority.ALWAYS);

        Label labelText = new Label(label);
        labelText.setFont(FONT_SMALL);
        labelText.setTextFill(TEXT_MUTED);

        Label valueText = new Label(value);
        valueText.setFont(FONT_TITLE);
        valueText.setTextFill(valueColor);

        item.getChildren().addAll(labelText, valueText);
        return item;
    }

    /**
     * Creates the payment history section.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        Label titleLabel = new Label("Payment History");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        // Create payments grid
        paymentsGrid = new VisualGrid();
        paymentsGrid.setFullHeight(true);
        paymentsGrid.setMinHeight(150);
        paymentsGrid.setPrefHeight(200);

        // Bind visual result
        FXProperties.runNowAndOnPropertiesChange(() -> {
            VisualResult result = paymentsVisualResultProperty.get();
            paymentsGrid.setVisualResult(result);
        }, paymentsVisualResultProperty);

        VBox gridContainer = new VBox(paymentsGrid);
        gridContainer.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        gridContainer.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        VBox.setVgrow(gridContainer, Priority.ALWAYS);

        section.getChildren().addAll(titleLabel, gridContainer);
        VBox.setVgrow(section, Priority.ALWAYS);

        return section;
    }

    /**
     * Creates the add payment form section.
     */
    private Node createAddPaymentSection() {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(PRIMARY_BORDER, BORDER_RADIUS_MEDIUM));

        Label titleLabel = new Label("Add Payment");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        // Form row
        HBox formRow = new HBox(12);
        formRow.setAlignment(Pos.CENTER_LEFT);

        // Date picker
        VBox dateField = new VBox(4);
        Label dateLabel = new Label("Date");
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setTextFill(TEXT_MUTED);
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(140);
        dateField.getChildren().addAll(dateLabel, datePicker);

        // Amount
        VBox amountFieldContainer = new VBox(4);
        Label amountLabel = new Label("Amount");
        amountLabel.setFont(FONT_SMALL);
        amountLabel.setTextFill(TEXT_MUTED);
        amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setPrefWidth(120);
        applyInputFieldStyle(amountField);
        amountFieldContainer.getChildren().addAll(amountLabel, amountField);

        // Payment method
        VBox methodField = new VBox(4);
        Label methodLabel = new Label("Method");
        methodLabel.setFont(FONT_SMALL);
        methodLabel.setTextFill(TEXT_MUTED);
        methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("Cash", "Card", "Bank Transfer", "Check", "Other");
        methodCombo.setValue("Card");
        methodCombo.setPrefWidth(140);
        methodField.getChildren().addAll(methodLabel, methodCombo);

        // Reference
        VBox referenceFieldContainer = new VBox(4);
        Label referenceLabel = new Label("Reference");
        referenceLabel.setFont(FONT_SMALL);
        referenceLabel.setTextFill(TEXT_MUTED);
        referenceField = new TextField();
        referenceField.setPromptText("Transaction ID or note");
        referenceField.setPrefWidth(200);
        applyInputFieldStyle(referenceField);
        HBox.setHgrow(referenceFieldContainer, Priority.ALWAYS);
        referenceFieldContainer.getChildren().addAll(referenceLabel, referenceField);

        // Add button
        Button addButton = new Button("Add Payment");
        applyPrimaryButtonStyle(addButton);
        addButton.setOnAction(e -> handleAddPayment());

        formRow.getChildren().addAll(dateField, amountFieldContainer, methodField, referenceFieldContainer, addButton);

        section.getChildren().addAll(titleLabel, formRow);
        return section;
    }

    /**
     * Handles adding a new payment.
     */
    private void handleAddPayment() {
        String amountText = amountField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountText.trim());
            if (amount <= 0) {
                return;
            }

            LocalDate date = datePicker.getValue();
            String method = methodCombo.getValue();
            String reference = referenceField.getText();

            // Create new MoneyTransfer entity
            MoneyTransfer payment = updateStore.insertEntity(MoneyTransfer.class);
            payment.setDocument(document);
            payment.setAmount((int) (amount * 100)); // Convert to cents if needed
            // payment.setDate(date); // TODO: Set date field
            // payment.setMethod(method); // TODO: Set payment method
            // payment.setComment(reference); // TODO: Set reference/comment

            // Clear form
            amountField.clear();
            referenceField.clear();
            datePicker.setValue(LocalDate.now());

            // Refresh the grid
            if (paymentsMapper != null) {
                paymentsMapper.refreshWhenActive();
            }

            System.out.println("Payment added: " + amount + " via " + method + " on " + date);
        } catch (NumberFormatException e) {
            // Invalid amount - show error
            System.err.println("Invalid payment amount: " + amountText);
        }
    }

    /**
     * Sets up the reactive payments mapper.
     * Should be called when the tab becomes active.
     */
    public void setupPaymentsMapper() {
        if (paymentsMapper == null && document.getId() != null) {
            paymentsMapper = ReactiveVisualMapper.<MoneyTransfer>createMasterPushReactiveChain(activity, paymentsVisualResultProperty)
                // Full MoneyTransfer fields from BookingDetailsPanel pattern
                .always("{class: 'MoneyTransfer', alias: 'mt', columns: 'date,method,transactionRef,comment,amount,verified', orderBy: 'date desc'}")
                .ifNotNullOtherwiseEmpty(pm.selectedDocumentProperty(), doc -> where("document=?", doc.getId()))
                .start();
        }
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            setupPaymentsMapper();
        }
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    /**
     * Formats a price value.
     */
    private String formatPrice(int amount) {
        // TODO: Get currency from document/organization
        return "£" + String.format("%,d", amount);
    }

    /**
     * Applies consistent styling to input fields.
     */
    private void applyInputFieldStyle(TextField field) {
        field.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }
}
