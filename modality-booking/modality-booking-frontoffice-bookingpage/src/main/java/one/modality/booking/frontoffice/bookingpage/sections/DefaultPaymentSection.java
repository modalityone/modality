package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.formatAmountNoDecimals;
import static one.modality.booking.frontoffice.bookingpage.theme.BookingFormStyles.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Default implementation of the Payment section.
 * Displays payment options, payment method selection, and terms acceptance.
 * <p>
 * Design based on JSX mockup Step6Payment.
 *
 * @author Bruno Salmon
 */
public class DefaultPaymentSection implements HasPaymentSection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === PAYMENT STATE ===
    protected final ObjectProperty<PaymentOption> paymentOptionProperty = new SimpleObjectProperty<>(PaymentOption.DEPOSIT);
    protected final ObjectProperty<PaymentMethod> paymentMethodProperty = new SimpleObjectProperty<>(PaymentMethod.CARD);
    protected final DoubleProperty customAmountProperty = new SimpleDoubleProperty(0);
    protected final BooleanProperty termsAcceptedProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty processingProperty = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty payButtonDisabled = new SimpleBooleanProperty(true);
    protected final SimpleStringProperty payButtonText = new SimpleStringProperty("Pay Now →");

    // === ALLOCATION STATE ===
    protected final Map<String, DoubleProperty> allocationProperties = new HashMap<>();

    // === BOOKING DATA ===
    protected final ObservableList<PaymentBookingItem> bookingItems = FXCollections.observableArrayList();
    protected double totalAmount = 0;
    protected double depositAmount = 0;
    protected String currencySymbol = "£";

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox bookingSummaryContainer;
    protected HBox paymentOptionsContainer;
    protected VBox customAmountSection;
    protected VBox allocationSection;
    protected VBox allocationItemsContainer;
    protected Label allocationTotalLabel;
    protected Label allocationWarningLabel;
    protected HBox paymentMethodsContainer;
    protected Label totalAmountLabel;
    protected Slider customAmountSlider;
    protected TextField customAmountTextField;

    // === CALLBACKS ===
    protected Consumer<PaymentResult> onPaymentSubmit;
    protected Runnable onBackPressed;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultPaymentSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-payment-section");

        // Page title
        Label title = createPageTitle();

        // Page subtitle
        Label subtitle = createPageSubtitle();

        // Booking summary section
        VBox bookingSummarySection = buildBookingSummarySection();

        // Payment amount section (includes custom amount slider)
        VBox paymentAmountSection = buildPaymentAmountSection();

        // Payment methods section
        VBox paymentMethodsSection = buildPaymentMethodsSection();

        // Terms and conditions
        VBox termsSection = buildTermsSection();

        // Note: Pay button is managed via composite API (ButtonNavigation)
        // not created inside this section

        container.getChildren().addAll(
                title, subtitle, bookingSummarySection,
                paymentAmountSection, paymentMethodsSection,
                termsSection
        );
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(bookingSummarySection, new Insets(0, 0, 32, 0));
        VBox.setMargin(paymentAmountSection, new Insets(0, 0, 32, 0));
        VBox.setMargin(paymentMethodsSection, new Insets(0, 0, 32, 0));
        VBox.setMargin(termsSection, new Insets(0, 0, 32, 0));
    }

    protected void setupBindings() {
        // Pay button is disabled until terms accepted and not processing
        payButtonDisabled.bind(termsAcceptedProperty.not().or(processingProperty));

        // Validity depends on terms accepted and not processing
        termsAcceptedProperty.addListener((obs, old, val) -> updateValidity());
        processingProperty.addListener((obs, old, val) -> updateValidity());

        // Update on color scheme change
        colorScheme.addListener((obs, old, newScheme) -> rebuildUI());

        // Update custom amount section and allocation visibility when payment option changes
        paymentOptionProperty.addListener((obs, old, newOption) -> {
            updateCustomAmountSectionVisibility();
            updateAllocationSectionVisibility();
            autoAllocate(); // Re-allocate when option changes
            updatePayButtonText(); // Update button text with new amount
        });

        // Update pay button text when custom amount changes
        customAmountProperty.addListener((obs, old, newVal) -> updatePayButtonText());
    }

    /**
     * Updates the pay button text with the current payment amount.
     * Format: "Pay £{amount} Now →"
     */
    protected void updatePayButtonText() {
        double amount = getPaymentAmount();
        String formattedAmount = formatAmountNoDecimals(amount);
        payButtonText.set("Pay " + currencySymbol + formattedAmount + " Now →");
    }

    protected void updateValidity() {
        validProperty.set(termsAcceptedProperty.get() && !processingProperty.get());
    }

    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
        rebuildBookingSummary();
        rebuildPaymentOptions();
        rebuildPaymentMethods();
        updateCustomAmountSectionVisibility();
        updateAllocationSectionVisibility();
    }

    protected VBox buildBookingSummarySection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(24));
        section.getStyleClass().add("bookingpage-card-lighter");

        // Title
        Label titleLabel = new Label("Your Booking Summary");
        titleLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-semibold", "bookingpage-text-dark");
        titleLabel.setPadding(new Insets(0, 0, 12, 0));
        titleLabel.setBorder(borderBottom(BORDER_GRAY, 2));
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Booking items container
        bookingSummaryContainer = new VBox(12);

        // Total row
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(16, 0, 0, 0));
        totalRow.getStyleClass().add("bookingpage-divider-top");
        VBox.setMargin(totalRow, new Insets(16, 0, 0, 0));

        Label totalTextLabel = new Label("Total Amount Due");
        totalTextLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-dark");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalAmountLabel = new Label(currencySymbol + formatAmountNoDecimals(totalAmount));
        totalAmountLabel.setFont(fontBold(24));
        totalAmountLabel.setTextFill(colorScheme.get().getPrimary());

        totalRow.getChildren().addAll(totalTextLabel, spacer, totalAmountLabel);

        section.getChildren().addAll(titleLabel, bookingSummaryContainer, totalRow);
        return section;
    }

    protected void rebuildBookingSummary() {
        if (bookingSummaryContainer == null) return;

        bookingSummaryContainer.getChildren().clear();

        int index = 1;
        for (PaymentBookingItem item : bookingItems) {
            HBox row = createBookingItemRow(item, index);
            bookingSummaryContainer.getChildren().add(row);
            index++;
        }
    }

    protected HBox createBookingItemRow(PaymentBookingItem item, int index) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded");

        VBox infoBox = new VBox(8);
        Label nameLabel = new Label(index + ". " + item.getPersonName());
        nameLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label detailsLabel = new Label(item.getDetails());
        detailsLabel.getStyleClass().add("bookingpage-label-small");
        detailsLabel.setWrapText(true);

        infoBox.getChildren().addAll(nameLabel, detailsLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label priceLabel = new Label(currencySymbol + formatAmountNoDecimals(item.getAmount()));
        priceLabel.setFont(fontBold(18));
        priceLabel.setTextFill(colorScheme.get().getPrimary());

        row.getChildren().addAll(infoBox, priceLabel);
        return row;
    }

    protected VBox buildPaymentAmountSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(24));
        section.getStyleClass().addAll("bookingpage-card-static", "bookingpage-rounded-lg");
        section.setBorder(border(BORDER_GRAY, 2, RADII_12));

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.HowMuchToPay);
        titleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Payment options container
        paymentOptionsContainer = new HBox(12);
        paymentOptionsContainer.setAlignment(Pos.CENTER);

        // Custom amount section (initially hidden)
        customAmountSection = buildCustomAmountSection();
        customAmountSection.setVisible(false);
        customAmountSection.setManaged(false);

        // Allocation section (initially hidden)
        allocationSection = buildAllocationSection();
        allocationSection.setVisible(false);
        allocationSection.setManaged(false);

        section.getChildren().addAll(titleLabel, paymentOptionsContainer, customAmountSection, allocationSection);
        rebuildPaymentOptions();

        return section;
    }

    protected VBox buildCustomAmountSection() {

        VBox section = new VBox(16);
        section.setPadding(new Insets(20));
        section.getStyleClass().addAll("bookingpage-bg-light", "bookingpage-rounded");
        VBox.setMargin(section, new Insets(20, 0, 0, 0));

        // Title
        Label titleLabel = new Label("Enter Amount");
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Input row with currency symbol
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label currencyLabel = new Label(currencySymbol);
        currencyLabel.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-semibold", "bookingpage-text-dark");

        customAmountTextField = new TextField();
        customAmountTextField.setText(formatAmountNoDecimals(customAmountProperty.get()));
        customAmountTextField.setFont(fontSemiBold(24));
        customAmountTextField.setPadding(new Insets(12, 16, 12, 16));
        customAmountTextField.setBorder(border(BORDER_GRAY, 2, RADII_8));
        customAmountTextField.setBackground(bg(Color.WHITE, RADII_8));
        customAmountTextField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customAmountTextField, Priority.ALWAYS);

        // Update property when text changes
        customAmountTextField.textProperty().addListener((obs, old, newVal) -> {
            try {
                double value = Double.parseDouble(newVal);
                handleCustomAmountChange(value);
            } catch (NumberFormatException ignored) {
                // Ignore invalid input
            }
        });

        inputRow.getChildren().addAll(currencyLabel, customAmountTextField);

        // Range helper text
        Label rangeLabel = new Label("Between " + currencySymbol + formatAmountNoDecimals(depositAmount) +
                " (minimum) and " + currencySymbol + formatAmountNoDecimals(totalAmount) + " (full amount)");
        rangeLabel.getStyleClass().add("bookingpage-label-small");

        // Slider
        customAmountSlider = new Slider(depositAmount, totalAmount, customAmountProperty.get());
        // Note: setShowTickMarks and setShowTickLabels are not supported in WebFX
        // Slider track color requires CSS styling in JavaFX

        // Bind slider to property bidirectionally
        customAmountSlider.valueProperty().addListener((obs, old, newVal) -> {
            handleCustomAmountChange(newVal.doubleValue());
            customAmountTextField.setText(formatAmountNoDecimals(newVal.doubleValue()));
        });

        section.getChildren().addAll(titleLabel, inputRow, rangeLabel, customAmountSlider);
        return section;
    }

    protected VBox buildAllocationSection() {
        BookingFormColorScheme colors = colorScheme.get();

        VBox section = new VBox(12);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("bookingpage-warning-box");
        VBox.setMargin(section, new Insets(20, 0, 0, 0));

        // Title
        Label titleLabel = new Label("Allocate Payment Across Bookings");
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Subtitle
        Label subtitleLabel = new Label("Specify how much to pay for each booking (optional - will auto-allocate proportionally)");
        subtitleLabel.getStyleClass().add("bookingpage-label-small");
        subtitleLabel.setWrapText(true);

        // Allocation items container
        allocationItemsContainer = new VBox(12);

        // Footer with total
        HBox footerRow = new HBox();
        footerRow.setAlignment(Pos.CENTER_LEFT);
        footerRow.setPadding(new Insets(12, 0, 0, 0));
        footerRow.getStyleClass().add("bookingpage-divider-thin-top");

        Label allocatedTextLabel = new Label("Allocated Total");
        allocatedTextLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        allocationTotalLabel = new Label(currencySymbol + "0");
        allocationTotalLabel.setFont(fontBold(18));
        allocationTotalLabel.setTextFill(colors.getPrimary());

        footerRow.getChildren().addAll(allocatedTextLabel, footerSpacer, allocationTotalLabel);

        // Warning label (initially hidden)
        allocationWarningLabel = new Label();
        allocationWarningLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-danger");
        allocationWarningLabel.setVisible(false);
        allocationWarningLabel.setManaged(false);

        // Auto-allocate button
        Hyperlink autoAllocateLink = new Hyperlink("Auto-allocate proportionally");
        autoAllocateLink.setFont(fontSemiBold(12));
        autoAllocateLink.setTextFill(colors.getPrimary());
        autoAllocateLink.setOnAction(e -> autoAllocate());

        section.getChildren().addAll(titleLabel, subtitleLabel, allocationItemsContainer, footerRow, allocationWarningLabel, autoAllocateLink);
        return section;
    }

    protected void rebuildAllocationItems() {
        if (allocationItemsContainer == null) return;

        allocationItemsContainer.getChildren().clear();

        int index = 1;
        for (PaymentBookingItem item : bookingItems) {
            HBox row = createAllocationItemRow(item, index);
            allocationItemsContainer.getChildren().add(row);
            index++;
        }
    }

    protected HBox createAllocationItemRow(PaymentBookingItem item, int index) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded");

        // Person name and total
        Label nameLabel = new Label(index + ". " + item.getPersonName() + " (" + currencySymbol + formatAmountNoDecimals(item.getAmount()) + ")");
        nameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-dark");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Input for allocation
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_RIGHT);

        Label currencyLabel = new Label(currencySymbol);
        currencyLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-muted");

        TextField allocationField = new TextField();
        DoubleProperty allocationProp = allocationProperties.computeIfAbsent(item.getId(), k -> new SimpleDoubleProperty(0));
        allocationField.setText(formatAmountNoDecimals(allocationProp.get()));
        allocationField.setPrefWidth(100);
        allocationField.setFont(fontSemiBold(14));
        allocationField.setPadding(new Insets(8, 12, 8, 12));
        allocationField.setBorder(border(BORDER_GRAY, 2, RADII_6));
        allocationField.setBackground(bg(Color.WHITE, RADII_6));
        allocationField.setAlignment(Pos.CENTER_RIGHT);

        // Update property when text changes
        allocationField.textProperty().addListener((obs, old, newVal) -> {
            try {
                double value = Math.max(0, Math.min(item.getAmount(), Double.parseDouble(newVal)));
                allocationProp.set(value);
                updateAllocationTotal();
            } catch (NumberFormatException ignored) {
                // Ignore invalid input
            }
        });

        // Update text when property changes
        allocationProp.addListener((obs, old, newVal) -> {
            String newText = formatAmountNoDecimals(newVal.doubleValue());
            if (!allocationField.getText().equals(newText)) {
                allocationField.setText(newText);
            }
        });

        inputBox.getChildren().addAll(currencyLabel, allocationField);
        row.getChildren().addAll(nameLabel, inputBox);
        return row;
    }

    protected void updateCustomAmountSectionVisibility() {
        if (customAmountSection != null) {
            boolean showCustom = paymentOptionProperty.get() == PaymentOption.CUSTOM;
            customAmountSection.setVisible(showCustom);
            customAmountSection.setManaged(showCustom);

            if (showCustom) {
                // Update slider range
                customAmountSlider.setMin(depositAmount);
                customAmountSlider.setMax(totalAmount);
                customAmountSlider.setValue(customAmountProperty.get());
            }
        }
    }

    protected void updateAllocationSectionVisibility() {
        if (allocationSection != null) {
            boolean showAllocation = bookingItems.size() > 1 && paymentOptionProperty.get() != PaymentOption.FULL;
            allocationSection.setVisible(showAllocation);
            allocationSection.setManaged(showAllocation);

            if (showAllocation) {
                rebuildAllocationItems();
                autoAllocate();
            }
        }
    }

    // === ALLOCATION HELPER METHODS ===

    protected double getAllocationTotal() {
        return allocationProperties.values().stream()
                .mapToDouble(DoubleProperty::get)
                .sum();
    }

    protected void autoAllocate() {
        double paymentAmount = getPaymentAmount();

        if (bookingItems.isEmpty() || totalAmount <= 0) {
            updateAllocationTotal();
            return;
        }

        // Step 1: Calculate initial allocations using floor to avoid exceeding payment amount
        double[] exactAllocations = new double[bookingItems.size()];
        double[] flooredAllocations = new double[bookingItems.size()];
        double sumFloored = 0;

        for (int i = 0; i < bookingItems.size(); i++) {
            PaymentBookingItem item = bookingItems.get(i);
            double proportion = item.getAmount() / totalAmount;
            exactAllocations[i] = paymentAmount * proportion;
            flooredAllocations[i] = Math.floor(exactAllocations[i]);
            sumFloored += flooredAllocations[i];
        }

        // Step 2: Calculate remainder to distribute (due to rounding)
        double remainder = Math.round(paymentAmount - sumFloored);

        // Step 3: Distribute remainder to items with largest fractional parts
        // Create array of indices sorted by fractional part (descending)
        Integer[] indices = new Integer[bookingItems.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        // Sort by fractional part (largest first)
        final double[] exactAlloc = exactAllocations;
        final double[] floorAlloc = flooredAllocations;
        java.util.Arrays.sort(indices, (a, b) -> {
            double fracA = exactAlloc[a] - floorAlloc[a];
            double fracB = exactAlloc[b] - floorAlloc[b];
            return Double.compare(fracB, fracA); // Descending order
        });

        // Distribute remainder (1 unit at a time to items with largest fractional parts)
        for (int i = 0; i < remainder && i < indices.length; i++) {
            flooredAllocations[indices[i]] += 1;
        }

        // Step 4: Apply allocations to properties
        for (int i = 0; i < bookingItems.size(); i++) {
            PaymentBookingItem item = bookingItems.get(i);
            DoubleProperty prop = allocationProperties.computeIfAbsent(item.getId(), k -> new SimpleDoubleProperty(0));
            prop.set(flooredAllocations[i]);
        }

        updateAllocationTotal();
    }

    protected void handleCustomAmountChange(double value) {
        double bounded = Math.max(depositAmount, Math.min(totalAmount, value));
        customAmountProperty.set(bounded);
        autoAllocate();
        rebuildPaymentOptions(); // To update displayed amount
    }

    protected void updateAllocationTotal() {
        if (allocationTotalLabel == null || allocationWarningLabel == null) return;

        double allocatedTotal = getAllocationTotal();
        double paymentAmount = getPaymentAmount();
        BookingFormColorScheme colors = colorScheme.get();

        // Compare rounded values since allocations are whole numbers
        long roundedAllocated = Math.round(allocatedTotal);
        long roundedPayment = Math.round(paymentAmount);
        boolean matches = roundedAllocated == roundedPayment;

        allocationTotalLabel.setText(currencySymbol + formatAmountNoDecimals(allocatedTotal));
        allocationTotalLabel.setTextFill(matches ? colors.getPrimary() : DANGER);

        if (!matches) {
            allocationWarningLabel.setText("⚠️ Allocated amount doesn't match payment amount (" + currencySymbol + formatAmountNoDecimals(paymentAmount) + ")");
            allocationWarningLabel.setVisible(true);
            allocationWarningLabel.setManaged(true);
        } else {
            allocationWarningLabel.setVisible(false);
            allocationWarningLabel.setManaged(false);
        }
    }

    protected void rebuildPaymentOptions() {
        if (paymentOptionsContainer == null) return;

        paymentOptionsContainer.getChildren().clear();

        // Deposit option
        VBox depositCard = createPaymentOptionCard(PaymentOption.DEPOSIT, "Minimum Deposit", "10% deposit required");
        HBox.setHgrow(depositCard, Priority.ALWAYS);

        // Custom option
        VBox customCard = createPaymentOptionCard(PaymentOption.CUSTOM, "Custom Amount", "Choose your amount");
        HBox.setHgrow(customCard, Priority.ALWAYS);

        // Full option
        VBox fullCard = createPaymentOptionCard(PaymentOption.FULL, "Pay in Full", "Complete payment");
        HBox.setHgrow(fullCard, Priority.ALWAYS);

        paymentOptionsContainer.getChildren().addAll(depositCard, customCard, fullCard);
    }

    protected VBox createPaymentOptionCard(PaymentOption option, String title, String description) {
        boolean selected = paymentOptionProperty.get() == option;
        double amount = getAmountForOption(option);
        String amountText = currencySymbol + formatAmountNoDecimals(amount);

        // Use helper to create the card (CSS-based styling)
        return BookingPageUIBuilder.createPaymentOptionCard(
                title,
                amountText,
                description,
                selected,
                () -> {
                    paymentOptionProperty.set(option);
                    autoAllocate();
                    rebuildPaymentOptions();
                }
        );
    }

    protected double getAmountForOption(PaymentOption option) {
        return switch (option) {
            case DEPOSIT -> depositAmount;
            case CUSTOM -> customAmountProperty.get() > 0 ? customAmountProperty.get() : totalAmount * 0.5;
            case FULL -> totalAmount;
        };
    }

    protected VBox buildPaymentMethodsSection() {
        VBox section = new VBox(16);

        // Title
        Label titleLabel = new Label("Select Payment Method");
        titleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Payment methods container
        paymentMethodsContainer = new HBox(16);
        paymentMethodsContainer.setAlignment(Pos.CENTER);

        section.getChildren().addAll(titleLabel, paymentMethodsContainer);
        rebuildPaymentMethods();

        return section;
    }

    protected void rebuildPaymentMethods() {
        if (paymentMethodsContainer == null) return;

        paymentMethodsContainer.getChildren().clear();
        BookingFormColorScheme colors = colorScheme.get();

        // Card payment
        VBox cardOption = createPaymentMethodCard(PaymentMethod.CARD, "Credit/Debit Card",
                "M3 5a2 2 0 012-2h14a2 2 0 012 2v14a2 2 0 01-2 2H5a2 2 0 01-2-2V5z M3 10h18", colors);
        HBox.setHgrow(cardOption, Priority.ALWAYS);

        // PayPal
        VBox paypalOption = createPaymentMethodCard(PaymentMethod.PAYPAL, "PayPal",
                "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z", colors);
        HBox.setHgrow(paypalOption, Priority.ALWAYS);

        // Bank Transfer
        VBox bankOption = createPaymentMethodCard(PaymentMethod.BANK, "Bank Transfer",
                "M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3", colors);
        HBox.setHgrow(bankOption, Priority.ALWAYS);

        paymentMethodsContainer.getChildren().addAll(cardOption, paypalOption, bankOption);
    }

    protected VBox createPaymentMethodCard(PaymentMethod method, String title, String iconPath,
                                         BookingFormColorScheme colors) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 16, 24, 16));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);

        boolean selected = paymentMethodProperty.get() == method;
        updatePaymentMethodCardStyle(card, selected, colors);

        // Icon
        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.setStroke(selected ? colors.getPrimary() : Color.web("#6c757d"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Content container
        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(icon, titleLabel);

        // Add checkmark badge for selected state
        if (selected) {
            StackPane checkmarkBadge = BookingPageUIBuilder.createCheckmarkBadgeCss(24);
            StackPane wrapper = new StackPane(content, checkmarkBadge);
            StackPane.setAlignment(checkmarkBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(checkmarkBadge, new Insets(-8, -8, 0, 0));
            card.getChildren().add(wrapper);
        } else {
            card.getChildren().add(content);
        }

        // Click handler
        card.setOnMouseClicked(e -> {
            paymentMethodProperty.set(method);
            rebuildPaymentMethods();
        });

        // Hover effects managed via CSS classes

        return card;
    }

    protected void updatePaymentMethodCardStyle(VBox card, boolean selected, BookingFormColorScheme colors) {
        // Use CSS classes for styling - hover effects handled by CSS
        card.getStyleClass().removeAll("bookingpage-card", "selected");
        card.getStyleClass().add("bookingpage-card");
        if (selected) {
            card.getStyleClass().add("selected");
        }
    }

    protected VBox buildTermsSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(20));
        section.getStyleClass().addAll("bookingpage-bg-light", "bookingpage-rounded");

        HBox checkboxRow = new HBox(12);
        checkboxRow.setAlignment(Pos.TOP_LEFT);

        // Use helper for checkbox indicator (CSS-based styling)
        StackPane checkboxIndicator = BookingPageUIBuilder.createCheckboxIndicator(termsAcceptedProperty);

        // Create HtmlText with hyperlink for Terms & Conditions
        HtmlText termsText = new HtmlText();
        termsText.setText("I accept the <a href=\"#terms\">Terms & Conditions</a> and understand the cancellation policy.");
        termsText.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-secondary");
        HBox.setHgrow(termsText, Priority.ALWAYS);

        checkboxRow.getChildren().addAll(checkboxIndicator, termsText);
        checkboxRow.setCursor(Cursor.HAND);
        checkboxRow.setOnMouseClicked(e -> {
            // Toggle checkbox when clicking anywhere on the row
            // Note: HtmlText handles link clicks internally
            termsAcceptedProperty.set(!termsAcceptedProperty.get());
        });

        section.getChildren().add(checkboxRow);
        return section;
    }

    protected void updateTotalDisplay() {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(currencySymbol + formatAmountNoDecimals(totalAmount));
            totalAmountLabel.setTextFill(colorScheme.get().getPrimary());
        }
    }

    // ========================================
    // STYLING HELPERS
    // ========================================

    protected Label createPageTitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.Payment);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(label, new Insets(0, 0, 10, 0));
        return label;
    }

    protected Label createPageSubtitle() {
        Label label = new Label("Review your registrations and choose how much to pay");
        label.getStyleClass().add("bookingpage-label-caption");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    // === BookingFormSection INTERFACE ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Payment;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasPaymentSection INTERFACE ===

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     * This property is kept for dynamic element coloring which requires Java.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        updateTotalDisplay();
    }

    @Override
    public void setTotalAmount(double amount) {
        this.totalAmount = amount;
        this.depositAmount = amount * 0.1; // 10% deposit
        this.customAmountProperty.set(amount * 0.5); // Default 50%
        updateTotalDisplay();
        rebuildPaymentOptions();
        updateCustomAmountSectionVisibility();
        updateAllocationSectionVisibility();
        updatePayButtonText();
    }

    @Override
    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public void setDepositAmount(double amount) {
        this.depositAmount = amount;
        rebuildPaymentOptions();
        updateCustomAmountSectionVisibility();
        updatePayButtonText();
    }

    @Override
    public void addBookingItem(PaymentBookingItem item) {
        bookingItems.add(item);
        // Initialize allocation for this item
        allocationProperties.computeIfAbsent(item.getId(), k -> new SimpleDoubleProperty(0));
        rebuildBookingSummary();
        updateAllocationSectionVisibility();
    }

    @Override
    public void setBookingItems(List<PaymentBookingItem> items) {
        bookingItems.clear();
        allocationProperties.clear();
        bookingItems.addAll(items);
        // Initialize allocations for all items
        for (PaymentBookingItem item : items) {
            allocationProperties.computeIfAbsent(item.getId(), k -> new SimpleDoubleProperty(0));
        }
        rebuildBookingSummary();
        updateAllocationSectionVisibility();
    }

    @Override
    public void clearBookingItems() {
        bookingItems.clear();
        allocationProperties.clear();
        rebuildBookingSummary();
        updateAllocationSectionVisibility();
    }

    @Override
    public PaymentOption getSelectedPaymentOption() {
        return paymentOptionProperty.get();
    }

    @Override
    public PaymentMethod getSelectedPaymentMethod() {
        return paymentMethodProperty.get();
    }

    @Override
    public double getPaymentAmount() {
        return getAmountForOption(paymentOptionProperty.get());
    }

    @Override
    public boolean isTermsAccepted() {
        return termsAcceptedProperty.get();
    }

    @Override
    public void setOnPaymentSubmit(Consumer<PaymentResult> callback) {
        this.onPaymentSubmit = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    @Override
    public void submitPayment() {
        if (onPaymentSubmit != null) {
            PaymentResult result = new PaymentResult(
                    paymentOptionProperty.get(),
                    paymentMethodProperty.get(),
                    getPaymentAmount(),
                    getAllocations()
            );
            onPaymentSubmit.accept(result);
        }
    }

    @Override
    public Future<PaymentResult> submitPaymentAsync() {
        Promise<PaymentResult> promise = Promise.promise();

        PaymentResult result = new PaymentResult(
                paymentOptionProperty.get(),
                paymentMethodProperty.get(),
                getPaymentAmount(),
                getAllocations()
        );

        // Notify callback if set
        if (onPaymentSubmit != null) {
            onPaymentSubmit.accept(result);
        }

        // Complete the promise with the result
        promise.complete(result);

        return promise.future();
    }

    @Override
    public ObservableBooleanValue payButtonDisabledProperty() {
        return payButtonDisabled;
    }

    @Override
    public StringProperty payButtonTextProperty() {
        return payButtonText;
    }

    @Override
    public void setProcessing(boolean isProcessing) {
        processingProperty.set(isProcessing);
    }

    @Override
    public Map<String, Double> getAllocations() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, DoubleProperty> entry : allocationProperties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    @Override
    public void setAllocation(String itemId, double amount) {
        DoubleProperty prop = allocationProperties.get(itemId);
        if (prop != null) {
            prop.set(amount);
            updateAllocationTotal();
        }
    }
}
