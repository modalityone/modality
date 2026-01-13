package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.History;
import one.modality.base.shared.entities.Method;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserName;

import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Payments tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Display payment history (money_transfer records) as styled list
 * - Add new payment form
 * - Edit existing payment (with permission check)
 * <p>
 * Based on RegistrationDashboardFull.jsx PaymentsTab section (lines 9086-9327).
 *
 * @author Claude Code
 */
public class PaymentsTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);

    // Document ID property for reactive binding (stores primary key value, not entity)
    private final ObjectProperty<Object> documentIdProperty = new SimpleObjectProperty<>();

    // Loaded payments from database
    private final ObservableList<MoneyTransfer> loadedPayments = FXCollections.observableArrayList();
    private ReactiveEntitiesMapper<MoneyTransfer> paymentsMapper;

    // UI Components
    private VBox paymentsListContainer;
    private Label emptyStateLabel;

    // Summary card labels (for updating after payment)
    private Label paidAmountLabel;
    private Label outstandingLabel;
    private VBox outstandingCard;
    private int totalAmount; // Cache total for recalculation

    // Cache for payment methods
    private final Map<String, Method> methodsByName = new HashMap<>();

    // New payment form fields
    private javafx.scene.control.DatePicker datePicker;
    private TextField amountField;
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

        // Store the document's primary key value for reactive binding
        if (document.getId() != null) {
            this.documentIdProperty.set(document.getId().getPrimaryKey());
        }
    }

    /**
     * Builds the Payments tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Summary section
        Node summarySection = createSummarySection();

        // Payment history section (styled list)
        Node historySection = createHistorySection();

        // Add payment form
        Node addPaymentSection = createAddPaymentSection();

        container.getChildren().addAll(summarySection, historySection, addPaymentSection);

        // Wrap entire content in ScrollPane (like GuestDetailsTab)
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    /**
     * Creates the payment summary section with 3 cards.
     */
    private Node createSummarySection() {
        HBox summary = new HBox(SPACING_XLARGE);
        summary.setPadding(PADDING_LARGE);

        // Total price card
        Integer priceNet = document.getPriceNet();
        totalAmount = priceNet != null ? priceNet : 0;
        VBox totalBox = createSummaryCard("Total Amount", formatPrice(totalAmount), CREAM, WARM_BROWN, CREAM_BORDER, null);

        // Total paid card - store reference to label for updating
        Integer deposit = document.getPriceDeposit();
        int paid = deposit != null ? deposit : 0;
        paidAmountLabel = new Label(formatPrice(paid));
        VBox paidBox = createSummaryCard("Paid Amount", null, SUCCESS_BG, SUCCESS, SUCCESS_BORDER, paidAmountLabel);

        // Balance card - store reference to label and card for updating
        int balance = totalAmount - paid;
        outstandingLabel = new Label(formatPrice(balance));
        javafx.scene.paint.Color balanceBg = balance > 0 ? RED_LIGHT : SUCCESS_BG;
        javafx.scene.paint.Color balanceColor = balance > 0 ? DANGER : SUCCESS;
        javafx.scene.paint.Color balanceBorder = balance > 0 ? DANGER_BORDER : SUCCESS_BORDER;
        outstandingCard = createSummaryCard("Outstanding", null, balanceBg, balanceColor, balanceBorder, outstandingLabel);

        summary.getChildren().addAll(totalBox, paidBox, outstandingCard);
        return summary;
    }

    /**
     * Creates a summary card with styled background and values.
     * @param existingValueLabel if not null, use this label instead of creating a new one
     */
    private VBox createSummaryCard(String label, String value, javafx.scene.paint.Color bg, javafx.scene.paint.Color textColor, javafx.scene.paint.Color borderColor, Label existingValueLabel) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setBackground(createBackground(bg, BORDER_RADIUS_MEDIUM));
        card.setBorder(createBorder(borderColor, BORDER_RADIUS_MEDIUM));
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);

        // Convert Color to CSS hex for WebFX compatibility
        String textColorHex = toHexColor(textColor);
        String labelColorHex = toHexColor(textColor.deriveColor(0, 1, 0.8, 1));

        Label labelText = new Label(label.toUpperCase());
        labelText.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        labelText.setStyle("-fx-text-fill: " + labelColorHex + ";");

        Label valueText = existingValueLabel != null ? existingValueLabel : new Label(value);
        valueText.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueText.setStyle("-fx-text-fill: " + textColorHex + ";");

        card.getChildren().addAll(labelText, valueText);
        return card;
    }

    /**
     * Converts a Color to CSS hex string for WebFX compatibility.
     * Uses manual hex conversion since String.format is not available in GWT.
     */
    private String toHexColor(javafx.scene.paint.Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "#" + toHex(r) + toHex(g) + toHex(b);
    }

    /**
     * Converts an integer (0-255) to a two-digit hex string.
     */
    private String toHex(int value) {
        String hex = Integer.toHexString(value);
        return hex.length() == 1 ? "0" + hex : hex;
    }

    /**
     * Creates the payment history section with styled list.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        // Header with icon badge
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        CornerRadii topRoundedCorners = new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false);
        header.setBackground(new Background(new BackgroundFill(CREAM, topRoundedCorners, null)));
        header.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            topRoundedCorners, new BorderWidths(1, 1, 1, 1))));
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon badge
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(28, 28);
        iconBadge.setMaxSize(28, 28);
        iconBadge.setBackground(createBackground(WARM_BROWN, 14));
        Label iconLabel = new Label("\uD83D\uDCB3"); // Credit card emoji
        iconLabel.setFont(Font.font(14));
        iconBadge.getChildren().add(iconLabel);

        Label titleLabel = new Label("Payment History");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #5c4033;"); // WARM_TEXT color

        header.getChildren().addAll(iconBadge, titleLabel);

        // Payments list container
        paymentsListContainer = new VBox();
        paymentsListContainer.setBackground(createBackground(BG_CARD, 0));

        // Empty state
        emptyStateLabel = new Label("No payments recorded yet");
        emptyStateLabel.setFont(FONT_SMALL);
        emptyStateLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        emptyStateLabel.setAlignment(Pos.CENTER);
        emptyStateLabel.setMaxWidth(Double.MAX_VALUE);
        emptyStateLabel.setPadding(new Insets(30));

        // Wrap list in container with rounded bottom corners
        VBox listWrapper = new VBox(paymentsListContainer);
        CornerRadii bottomRoundedCorners = new CornerRadii(0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, false);
        listWrapper.setBackground(new Background(new BackgroundFill(BG_CARD, bottomRoundedCorners, null)));
        listWrapper.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            bottomRoundedCorners, new BorderWidths(0, 1, 1, 1))));

        // Outer container
        VBox container = new VBox();
        container.getChildren().addAll(header, listWrapper);

        section.getChildren().add(container);

        return section;
    }

    /**
     * Refreshes the payment list from loaded data.
     */
    private void refreshPaymentsList() {
        paymentsListContainer.getChildren().clear();

        if (loadedPayments.isEmpty()) {
            paymentsListContainer.getChildren().add(emptyStateLabel);
        } else {
            for (int i = 0; i < loadedPayments.size(); i++) {
                MoneyTransfer payment = loadedPayments.get(i);
                Node row = createPaymentRow(payment, i < loadedPayments.size() - 1);
                paymentsListContainer.getChildren().add(row);
            }
        }

        // Update summary cards with new totals
        updateSummaryCards();
    }

    /**
     * Updates the summary cards based on the current payments list.
     */
    private void updateSummaryCards() {
        // Calculate total paid from all payments in the list
        int paidAmount = 0;
        for (MoneyTransfer payment : loadedPayments) {
            Integer amount = payment.getAmount();
            if (amount != null) {
                paidAmount += amount;
            }
        }

        // Update paid amount label
        if (paidAmountLabel != null) {
            paidAmountLabel.setText(formatPrice(paidAmount));
        }

        // Update outstanding label and card styling
        int outstanding = totalAmount - paidAmount;
        if (outstandingLabel != null) {
            outstandingLabel.setText(formatPrice(outstanding));
        }

        // Update outstanding card background/border color based on balance
        if (outstandingCard != null) {
            javafx.scene.paint.Color balanceBg = outstanding > 0 ? RED_LIGHT : SUCCESS_BG;
            javafx.scene.paint.Color balanceBorder = outstanding > 0 ? DANGER_BORDER : SUCCESS_BORDER;
            javafx.scene.paint.Color textColor = outstanding > 0 ? DANGER : SUCCESS;
            outstandingCard.setBackground(createBackground(balanceBg, BORDER_RADIUS_MEDIUM));
            outstandingCard.setBorder(createBorder(balanceBorder, BORDER_RADIUS_MEDIUM));
            outstandingLabel.setStyle("-fx-text-fill: " + toHexColor(textColor) + ";");
        }
    }

    /**
     * Creates a styled payment row matching JSX design.
     */
    private Node createPaymentRow(MoneyTransfer payment, boolean showBorder) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        if (showBorder) {
            row.setBorder(new Border(new BorderStroke(
                BORDER_LIGHT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 1, 0))));
        }

        // Left section: Date and method info
        VBox leftSection = new VBox(4);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        // Date (bold, 14px)
        String dateStr = formatPaymentDate(payment.getDate());
        Label dateLabel = new Label(dateStr);
        dateLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        dateLabel.setStyle("-fx-text-fill: #3d3530;"); // TEXT color

        // Method and reference (13px, muted)
        String methodName = payment.getMethod() != null ? payment.getMethod().getName() : "Unknown";
        String reference = payment.getTransactionRef();
        String methodText = reference != null && !reference.isEmpty()
            ? methodName + " \u2022 " + reference
            : methodName;
        Label methodLabel = new Label(methodText);
        methodLabel.setFont(Font.font("System", 13));
        methodLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

        leftSection.getChildren().addAll(dateLabel, methodLabel);

        // Right section: Amount and status badge
        VBox rightSection = new VBox(4);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        // Amount (large, green, bold)
        Integer amount = payment.getAmount();
        String amountStr = formatPrice(amount != null ? amount : 0);
        Label amountLabel = new Label(amountStr);
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        amountLabel.setStyle("-fx-text-fill: #16a34a;"); // SUCCESS color

        // Status badge
        Label statusBadge = new Label("PAID");
        statusBadge.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        statusBadge.setStyle("-fx-text-fill: #16a34a;"); // SUCCESS color
        statusBadge.setBackground(createBackground(SUCCESS_BG, BORDER_RADIUS_SMALL));
        statusBadge.setPadding(new Insets(3, 10, 3, 10));

        rightSection.getChildren().addAll(amountLabel, statusBadge);

        row.getChildren().addAll(leftSection, rightSection);

        return row;
    }

    /**
     * Formats a payment date in a friendly format.
     */
    private String formatPaymentDate(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown date";
        // Format: "Mon, 15 February 2025"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMMM yyyy", Locale.ENGLISH);
        return dateTime.format(formatter);
    }

    /**
     * Creates the add payment form section.
     */
    private Node createAddPaymentSection() {
        VBox section = new VBox(12);

        // Header with icon badge
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        CornerRadii topCorners = new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false);
        header.setBackground(new Background(new BackgroundFill(SAND, topCorners, null)));
        header.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            topCorners, new BorderWidths(1, 1, 0, 1))));
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon badge (orange plus)
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(28, 28);
        iconBadge.setMaxSize(28, 28);
        iconBadge.setBackground(createBackground(WARM_ORANGE, 14));
        Label iconLabel = new Label("+");
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        iconLabel.setStyle("-fx-text-fill: white;");
        iconBadge.getChildren().add(iconLabel);

        Label titleLabel = new Label("Record New Payment");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #5c4033;"); // WARM_TEXT color

        header.getChildren().addAll(iconBadge, titleLabel);

        // Form content
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20));
        CornerRadii bottomCorners = new CornerRadii(0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, false);
        formContent.setBackground(new Background(new BackgroundFill(BG_CARD, bottomCorners, null)));
        formContent.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            bottomCorners, new BorderWidths(0, 1, 1, 1))));

        // Form row (4 columns)
        HBox formRow = new HBox(16);
        formRow.setAlignment(Pos.CENTER_LEFT);

        // Date picker (compact JavaFX DatePicker with dropdown)
        VBox dateField = new VBox(6);
        Label dateLabel = new Label("Date");
        dateLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        dateLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        datePicker = new javafx.scene.control.DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(140);
        datePicker.setStyle("-fx-font-size: 14px;");
        dateField.getChildren().addAll(dateLabel, datePicker);

        // Amount
        VBox amountFieldContainer = new VBox(6);
        Label amountLabel = new Label("Amount (\u00A3)");
        amountLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        amountLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setPrefWidth(120);
        applyInputFieldStyle(amountField);
        amountFieldContainer.getChildren().addAll(amountLabel, amountField);

        // Payment method (ToggleButtons - GWT-compatible)
        VBox methodField = new VBox(6);
        Label methodLabel = new Label("Method");
        methodLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        methodLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

        methodToggleGroup = new ToggleGroup();
        cashToggle = createMethodToggleButton("Cash");
        cardToggle = createMethodToggleButton("Card");
        cardToggle.setSelected(true);
        // Apply selected style to Card button initially
        cardToggle.setBackground(createBackground(WARM_BROWN, BORDER_RADIUS_SMALL));
        cardToggle.setStyle("-fx-text-fill: white;");
        bankToggle = createMethodToggleButton("Bank");
        checkToggle = createMethodToggleButton("Check");
        otherToggle = createMethodToggleButton("Other");

        HBox methodButtons = new HBox(4);
        methodButtons.getChildren().addAll(cashToggle, cardToggle, bankToggle, checkToggle, otherToggle);
        methodField.getChildren().addAll(methodLabel, methodButtons);

        // Reference
        VBox referenceFieldContainer = new VBox(6);
        Label referenceLabel = new Label("Comment");
        referenceLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        referenceLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        referenceField = new TextField();
        referenceField.setPromptText("Optional note...");
        referenceField.setPrefWidth(200);
        applyInputFieldStyle(referenceField);
        HBox.setHgrow(referenceFieldContainer, Priority.ALWAYS);
        referenceFieldContainer.getChildren().addAll(referenceLabel, referenceField);

        formRow.getChildren().addAll(dateField, amountFieldContainer, methodField, referenceFieldContainer);

        // Add button - styled per JSX design (warmBrown background)
        Button addButton = new Button();
        HBox buttonContent = new HBox(8);
        buttonContent.setAlignment(Pos.CENTER);
        Label plusIcon = new Label("+");
        plusIcon.setFont(Font.font("System", FontWeight.BOLD, 16));
        plusIcon.setStyle("-fx-text-fill: white;");
        Label buttonText = new Label("Add Payment");
        buttonText.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        buttonText.setStyle("-fx-text-fill: white;");
        buttonContent.getChildren().addAll(plusIcon, buttonText);
        addButton.setGraphic(buttonContent);
        addButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // Apply JSX-matching style: warmBrown background, 10px 20px padding, 8px border radius
        addButton.setBackground(createBackground(WARM_BROWN, BORDER_RADIUS_MEDIUM));
        addButton.setPadding(new Insets(10, 20, 10, 20));
        addButton.setCursor(javafx.scene.Cursor.HAND);
        addButton.setOnAction(e -> handleAddPayment());

        formContent.getChildren().addAll(formRow, addButton);

        section.getChildren().addAll(header, formContent);
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
            String methodName = getSelectedPaymentMethod();
            String reference = referenceField.getText();

            // Create a new UpdateStore for this payment
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
                method = methodsByName.get("card");
                if (method != null) {
                    payment.setMethod(method);
                }
            }

            // Create History record for this payment
            History history = paymentStore.insertEntity(History.class);
            history.setDocument(document);
            history.setMoneyTransfer(payment);
            history.setUsername(FXUserName.getUserName());
            history.setComment("Payment recorded: " + formatPrice((int)(amount * 100)) + " via " + methodName);

            // Submit changes to database
            paymentStore.submitChanges()
                .onSuccess(batch -> {
                    Console.log("Payment added successfully: " + amount + " via " + methodName);
                    // UI updates must run on FX application thread
                    Platform.runLater(() -> {
                        // Clear form
                        amountField.clear();
                        referenceField.clear();
                        datePicker.setValue(LocalDate.now());
                        // Refresh the mapper to load new data
                        if (paymentsMapper != null) {
                            paymentsMapper.refreshWhenActive();
                        }
                    });
                })
                .onFailure(e -> {
                    Console.log("Failed to add payment: " + e.getMessage());
                });

        } catch (NumberFormatException e) {
            Console.log("Invalid payment amount: " + amountText);
        }
    }

    /**
     * Sets up the reactive payments mapper.
     * Uses ReactiveEntitiesMapper pattern from BookingTab.
     */
    public void setupPaymentsMapper() {
        if (paymentsMapper == null && documentIdProperty.get() != null) {
            Console.log("PaymentsTab: Setting up payments mapper for document " + documentIdProperty.get());

            paymentsMapper = ReactiveEntitiesMapper.<MoneyTransfer>createPushReactiveChain(activity)
                .always("{class: 'MoneyTransfer', fields: 'date,method.name,transactionRef,comment,amount,pending,successful', orderBy: 'date desc'}")
                .always(documentIdProperty, docId -> where("document=?", docId))
                .storeEntitiesInto(loadedPayments)
                .start();

            // Listen for changes and refresh the list
            ObservableLists.runNowAndOnListChange(change -> {
                Console.log("PaymentsTab: Loaded " + loadedPayments.size() + " payments");
                refreshPaymentsList();
            }, loadedPayments);

            // Load payment methods for the add payment form
            loadPaymentMethods();
        }
    }

    /**
     * Loads payment methods from the database.
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
     * Formats a price value (amount in cents) with 2 decimal places.
     */
    private String formatPrice(int amountInCents) {
        Event event = document.getEvent();
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        // Use PriceFormatter with show00cents=true to always show 2 decimal places
        return PriceFormatter.formatWithCurrency(amountInCents, currencySymbol, true);
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
        return "Card";
    }

    /**
     * Applies consistent styling to input fields.
     */
    private void applyInputFieldStyle(TextField field) {
        field.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }

    /**
     * Creates a styled toggle button for payment method selection.
     */
    private ToggleButton createMethodToggleButton(String text) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(methodToggleGroup);
        button.setMinWidth(55);
        button.setPrefWidth(55);
        button.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        // Style with rounded corners and warm colors
        button.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
        button.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
        button.setPadding(new Insets(8, 10, 8, 10));
        // Update style when selected changes
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setBackground(createBackground(WARM_BROWN, BORDER_RADIUS_SMALL));
                button.setStyle("-fx-text-fill: white;");
            } else {
                button.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
                button.setStyle("-fx-text-fill: #5c4033;");
            }
        });
        // Set initial style
        button.setStyle("-fx-text-fill: #5c4033;");
        return button;
    }
}
