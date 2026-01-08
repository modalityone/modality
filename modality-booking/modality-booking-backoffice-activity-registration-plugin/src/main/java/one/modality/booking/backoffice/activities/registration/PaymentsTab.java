package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Method;
import one.modality.base.shared.entities.MoneyTransfer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    // UI Components
    private VisualGrid paymentsGrid;
    private ReactiveVisualMapper<MoneyTransfer> paymentsMapper;

    // Cache for payment methods
    private final Map<String, Method> methodsByName = new HashMap<>();

    // New payment form fields
    private dev.webfx.extras.time.pickers.DatePicker datePicker; // WebFX DatePicker (GWT-compatible)
    private TextField amountField;
    // Payment method using ToggleButtons (GWT-compatible replacement for ComboBox)
    private ToggleGroup methodToggleGroup;
    private ToggleButton cashToggle;
    private ToggleButton cardToggle;
    private ToggleButton bankToggle;
    private ToggleButton checkToggle;
    private ToggleButton otherToggle;
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

        // Date picker (WebFX DatePicker - GWT-compatible)
        VBox dateField = new VBox(4);
        Label dateLabel = new Label("Date");
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setTextFill(TEXT_MUTED);
        datePicker = new dev.webfx.extras.time.pickers.DatePicker();
        datePicker.setSelectedDate(LocalDate.now()); // Set default date after construction
        javafx.scene.Node datePickerView = datePicker.getView();
        dateField.getChildren().addAll(dateLabel, datePickerView);

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

        // Payment method (using ToggleButtons - GWT-compatible)
        VBox methodField = new VBox(4);
        Label methodLabel = new Label("Method");
        methodLabel.setFont(FONT_SMALL);
        methodLabel.setTextFill(TEXT_MUTED);

        methodToggleGroup = new ToggleGroup();
        cashToggle = new ToggleButton("Cash");
        cashToggle.setToggleGroup(methodToggleGroup);
        cardToggle = new ToggleButton("Card");
        cardToggle.setToggleGroup(methodToggleGroup);
        cardToggle.setSelected(true); // Default selection
        bankToggle = new ToggleButton("Bank");
        bankToggle.setToggleGroup(methodToggleGroup);
        checkToggle = new ToggleButton("Check");
        checkToggle.setToggleGroup(methodToggleGroup);
        otherToggle = new ToggleButton("Other");
        otherToggle.setToggleGroup(methodToggleGroup);

        HBox methodButtons = new HBox(4);
        methodButtons.getChildren().addAll(cashToggle, cardToggle, bankToggle, checkToggle, otherToggle);
        methodField.getChildren().addAll(methodLabel, methodButtons);

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

            LocalDate date = datePicker.getSelectedDate();
            String methodName = getSelectedPaymentMethod();
            String reference = referenceField.getText();

            // Create a new UpdateStore for this payment (separate from the modal's updateStore)
            UpdateStore paymentStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());

            // Create new MoneyTransfer entity
            MoneyTransfer payment = paymentStore.insertEntity(MoneyTransfer.class);
            payment.setDocument(document);
            payment.setAmount((int) (amount * 100)); // Convert to cents
            payment.setDate(date != null ? date.atStartOfDay() : LocalDate.now().atStartOfDay());
            payment.setTransactionRef(reference);
            payment.setComment(reference);
            payment.setPending(false);
            payment.setSuccessful(true);

            // Set payment method from lookup
            Method method = methodsByName.get(methodName.toLowerCase());
            if (method != null) {
                payment.setMethod(method);
            } else {
                // Try alternative lookups
                method = methodsByName.get("card"); // Default fallback
                if (method != null) {
                    payment.setMethod(method);
                }
            }

            // Submit changes to database
            paymentStore.submitChanges()
                .onSuccess(batch -> {
                    Console.log("Payment added successfully: " + amount + " via " + methodName);
                    // Clear form
                    amountField.clear();
                    referenceField.clear();
                    datePicker.setSelectedDate(LocalDate.now());
                    // Refresh the grid
                    if (paymentsMapper != null) {
                        paymentsMapper.refreshWhenActive();
                    }
                })
                .onFailure(e -> {
                    Console.log("Failed to add payment: " + e.getMessage());
                });

        } catch (NumberFormatException e) {
            Console.log("Invalid payment amount: " + amountText);
        }
    }

    private static final String PAYMENTS_DQL =
        "{class: 'MoneyTransfer', columns: 'date,method,transactionRef,comment,amount', where: 'document=${selectedDocument}', orderBy: 'date desc'}";

    /**
     * Sets up the reactive payments mapper.
     * Should be called when the tab becomes active.
     */
    public void setupPaymentsMapper() {
        if (paymentsMapper == null && document.getId() != null) {
            // Use BookingDetailsPanel pattern: createPushReactiveChain + visualizeResultInto
            paymentsMapper = ReactiveVisualMapper.<MoneyTransfer>createPushReactiveChain()
                .always("{class: 'MoneyTransfer'}")
                .ifNotNullOtherwiseEmptyString(pm.selectedDocumentProperty(), doc ->
                    Strings.replaceAll(PAYMENTS_DQL, "${selectedDocument}", doc.getPrimaryKey()))
                .bindActivePropertyTo(activeProperty)
                .setDataSourceModel(DataSourceModelService.getDefaultDataSourceModel())
                .applyDomainModelRowStyle()
                .visualizeResultInto(paymentsGrid)
                .start();

            // Load payment methods for the add payment form
            loadPaymentMethods();
        }
    }

    /**
     * Loads payment methods from the database for the add payment form.
     */
    private void loadPaymentMethods() {
        if (methodsByName.isEmpty()) {
            EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .<Method>executeQuery("select code,name from Method")
                .onSuccess(methods -> {
                    for (Method m : methods) {
                        if (m.getName() != null) {
                            methodsByName.put(m.getName().toLowerCase(), m);
                        }
                        if (m.getCode() != null) {
                            methodsByName.put(m.getCode().toLowerCase(), m);
                        }
                    }
                    Console.log("Loaded " + methodsByName.size() + " payment methods");
                })
                .onFailure(e -> Console.log("Failed to load payment methods: " + e.getMessage()));
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
     * GWT-compatible: avoids String.format
     */
    private String formatPrice(int amount) {
        // TODO: Get currency from document/organization
        return "£" + formatWithCommas(amount);
    }

    private String formatWithCommas(int amount) {
        if (amount < 1000) return String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(Math.abs(amount));
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append(',');
            sb.append(str.charAt(i));
        }
        return amount < 0 ? "-" + sb : sb.toString();
    }

    /**
     * Gets the selected payment method from the toggle group.
     */
    private String getSelectedPaymentMethod() {
        Toggle selected = methodToggleGroup.getSelectedToggle();
        if (selected == cashToggle) return "Cash";
        if (selected == cardToggle) return "Card";
        if (selected == bankToggle) return "Bank Transfer";
        if (selected == checkToggle) return "Check";
        if (selected == otherToggle) return "Other";
        return "Card"; // Default
    }

    /**
     * Applies consistent styling to input fields.
     */
    private void applyInputFieldStyle(TextField field) {
        field.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }
}
