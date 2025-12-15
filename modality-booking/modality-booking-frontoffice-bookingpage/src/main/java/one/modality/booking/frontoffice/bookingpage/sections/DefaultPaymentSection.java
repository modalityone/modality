package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.async.Future;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    protected final IntegerProperty customAmountProperty = new SimpleIntegerProperty(0);
    protected final BooleanProperty termsAcceptedProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty processingProperty = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty payButtonDisabled = new SimpleBooleanProperty(true);
    protected final SimpleStringProperty payButtonText = new SimpleStringProperty();

    // === ALLOCATION STATE ===
    protected final Map<Object, IntegerProperty> allocationProperties = new HashMap<>();

    // === BOOKING DATA ===
    protected final ObservableList<PaymentBookingItem> bookingItems = FXCollections.observableArrayList();
    protected int totalAmount = 0;
    protected int depositAmount = 0;
    protected int depositPercentage = 0; // Calculated from depositAmount/totalAmount
    protected Set<PaymentOption> availablePaymentOptions = EnumSet.allOf(PaymentOption.class);
    protected Set<PaymentMethod> availablePaymentMethods = EnumSet.of(PaymentMethod.CARD, PaymentMethod.BANK); // PAYPAL not implemented

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox bookingSummaryContainer;
    protected VBox paymentAmountSection;
    protected VBox paymentMethodsSection;
    protected HBox paymentOptionsContainer;
    protected VBox customAmountSection;
    protected VBox allocationSection;
    protected VBox allocationItemsContainer;
    protected Label allocationTotalLabel;
    protected Label allocationWarningLabel;
    protected HBox paymentMethodsContainer;
    protected Label totalAmountLabel;
    protected Slider customAmountSlider;
    protected Label customAmountRangeLabel;
    protected TextField customAmountTextField;

    // === CALLBACKS ===
    protected AsyncFunction<PaymentResult, Void> onPaymentSubmit;
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
        // Stored as field so it can be hidden when only one payment option exists
        paymentAmountSection = buildPaymentAmountSection();

        // Payment methods section
        // Stored as field so it can be hidden when only one payment method exists
        paymentMethodsSection = buildPaymentMethodsSection();

        // Terms and conditions
        HBox termsSection = buildTermsSection();

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

        // Note: Color scheme listener removed - CSS handles theme changes via CSS variables

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
        if (payButtonText == null) return;
        int amount = getPaymentAmount();
        Event event = workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null
                ? workingBookingProperties.getWorkingBooking().getEvent() : null;
        String formattedAmount = EventPriceFormatter.formatWithCurrency(amount, event);
        I18n.bindI18nTextProperty(payButtonText, BookingPageI18nKeys.PayAmountNow, formattedAmount);
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
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.YourBookingSummary);
        titleLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-semibold", "bookingpage-text-dark", "bookingpage-divider-bottom-gray");
        titleLabel.setPadding(new Insets(0, 0, 12, 0));
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Booking items container
        bookingSummaryContainer = new VBox(12);

        // Total row
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(16, 0, 0, 0));
        totalRow.getStyleClass().add("bookingpage-divider-top");
        VBox.setMargin(totalRow, new Insets(16, 0, 0, 0));

        Label totalTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalAmountDue);
        totalTextLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-dark");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalAmountLabel = new Label(EventPriceFormatter.formatWithCurrency(totalAmount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
        totalAmountLabel.getStyleClass().addAll("bookingpage-price-large", "bookingpage-text-primary");

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

        Label priceLabel = new Label(EventPriceFormatter.formatWithCurrency(item.getAmount(), workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
        priceLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");

        row.getChildren().addAll(infoBox, priceLabel);
        return row;
    }

    protected VBox buildPaymentAmountSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(24));
        section.getStyleClass().addAll("bookingpage-card-static", "bookingpage-rounded-lg", "bookingpage-card-bordered");

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
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.EnterAmount);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Input row with currency symbol
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label currencyLabel = new Label(PriceFormatter.getCurrencySymbol());
        currencyLabel.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-semibold", "bookingpage-text-dark");

        customAmountTextField = new TextField();
        customAmountTextField.setText(PriceFormatter.formatPriceNoCurrencyNoDecimals(customAmountProperty.get()));
        customAmountTextField.getStyleClass().addAll("bookingpage-input", "bookingpage-text-2xl", "bookingpage-font-semibold");
        customAmountTextField.setPadding(new Insets(12, 16, 12, 16));
        customAmountTextField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customAmountTextField, Priority.ALWAYS);

        // Update property when text changes
        customAmountTextField.textProperty().addListener((obs, old, newVal) -> {
            try {
                int value = PriceFormatter.parsePrice(newVal);
                handleCustomAmountChange(value);
            } catch (NumberFormatException ignored) {
                // Ignore invalid input
            }
        });

        // Update text field to show rounded value when focus is lost
        customAmountTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                // Update to show the rounded/bounded value
                customAmountTextField.setText(PriceFormatter.formatPriceNoCurrencyNoDecimals(customAmountProperty.get()));
            }
        });

        inputRow.getChildren().addAll(currencyLabel, customAmountTextField);

        // Range helper text
        customAmountRangeLabel = I18nControls.newLabel(BookingPageI18nKeys.CustomAmountRange);
        I18nControls.bindI18nProperties(customAmountRangeLabel, BookingPageI18nKeys.CustomAmountRange,
            EventPriceFormatter.formatWithCurrency(depositAmount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null),
            EventPriceFormatter.formatWithCurrency(totalAmount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
        customAmountRangeLabel.getStyleClass().add("bookingpage-label-small");

        // Slider
        customAmountSlider = new Slider(depositAmount, totalAmount, customAmountProperty.get());
        // Note: setShowTickMarks and setShowTickLabels are not supported in WebFX
        // Slider track color requires CSS styling in JavaFX

        // Bind slider to property bidirectionally
        customAmountSlider.valueProperty().addListener((obs, old, newVal) -> {
            int value = newVal.intValue();
            handleCustomAmountChange(value);
            // Use the actual stored value (rounded and bounded) for display
            customAmountTextField.setText(PriceFormatter.formatPriceNoCurrencyNoDecimals(customAmountProperty.get()));
        });

        // Update slider when property changes (e.g., from text field input)
        customAmountProperty.addListener((obs, old, newVal) -> {
            if (customAmountSlider.getValue() != newVal.intValue()) {
                customAmountSlider.setValue(newVal.intValue());
            }
        });

        section.getChildren().addAll(titleLabel, inputRow, customAmountRangeLabel, customAmountSlider);
        return section;
    }

    protected VBox buildAllocationSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("bookingpage-warning-box");
        VBox.setMargin(section, new Insets(20, 0, 0, 0));

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.AllocatePaymentAcrossBookings);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Subtitle
        Label subtitleLabel = I18nControls.newLabel(BookingPageI18nKeys.AllocatePaymentSubtitle);
        subtitleLabel.getStyleClass().add("bookingpage-label-small");
        subtitleLabel.setWrapText(true);

        // Allocation items container
        allocationItemsContainer = new VBox(12);

        // Footer with total
        HBox footerRow = new HBox();
        footerRow.setAlignment(Pos.CENTER_LEFT);
        footerRow.setPadding(new Insets(12, 0, 0, 0));
        footerRow.getStyleClass().add("bookingpage-divider-thin-top");

        Label allocatedTextLabel = I18nControls.newLabel(BookingPageI18nKeys.AllocatedTotal);
        allocatedTextLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        allocationTotalLabel = new Label(EventPriceFormatter.formatWithCurrency(0, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
        allocationTotalLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");

        footerRow.getChildren().addAll(allocatedTextLabel, footerSpacer, allocationTotalLabel);

        // Warning label (initially hidden)
        allocationWarningLabel = new Label();
        allocationWarningLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-danger");
        allocationWarningLabel.setVisible(false);
        allocationWarningLabel.setManaged(false);

        // Auto-allocate button - use themed hyperlink helper
        Hyperlink autoAllocateLink = BookingPageUIBuilder.createThemedHyperlink(BookingPageI18nKeys.AutoAllocateProportionally);
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
        Label nameLabel = new Label(index + ". " + item.getPersonName() + " (" + EventPriceFormatter.formatWithCurrency(item.getAmount(), workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null) + ")");
        nameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-dark");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Input for allocation
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_RIGHT);

        Label currencyLabel = new Label(PriceFormatter.getCurrencySymbol());
        currencyLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-muted");

        TextField allocationField = new TextField();
        IntegerProperty allocationProp = allocationProperties.computeIfAbsent(item.getDocumentPrimaryKey(), k -> new SimpleIntegerProperty(0));
        allocationField.setText(PriceFormatter.formatPriceNoCurrencyNoDecimals(allocationProp.get()));
        allocationField.setPrefWidth(100);
        allocationField.getStyleClass().addAll("bookingpage-input", "bookingpage-font-semibold");
        allocationField.setPadding(new Insets(8, 12, 8, 12));
        allocationField.setAlignment(Pos.CENTER_RIGHT);

        // Update property when text changes
        allocationField.textProperty().addListener((obs, old, newVal) -> {
            try {
                int value = Math.max(0, Math.min(item.getAmount(), PriceFormatter.parsePrice(newVal)));
                allocationProp.set(value);
                updateAllocationTotal();
            } catch (NumberFormatException ignored) {
                // Ignore invalid input
            }
        });

        // Update text when property changes
        allocationProp.addListener((obs, old, newVal) -> {
            String newText = PriceFormatter.formatPriceNoCurrencyNoDecimals(newVal.intValue());
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

                // Update range label text
                if (customAmountRangeLabel != null) {
                    Event event = workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null
                        ? workingBookingProperties.getWorkingBooking().getEvent() : null;
                    I18nControls.bindI18nProperties(customAmountRangeLabel, BookingPageI18nKeys.CustomAmountRange,
                        EventPriceFormatter.formatWithCurrency(depositAmount, event),
                        EventPriceFormatter.formatWithCurrency(totalAmount, event));
                }
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

    protected int getAllocationTotal() {
        return allocationProperties.values().stream()
                .mapToInt(IntegerProperty::get)
                .sum();
    }

    protected void autoAllocate() {
        double paymentAmount = PriceFormatter.centsPriceToDoublePrice(getPaymentAmount());

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
            double proportion = PriceFormatter.centsPriceToDoublePrice(item.getAmount()) / PriceFormatter.centsPriceToDoublePrice(totalAmount);
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
            IntegerProperty prop = allocationProperties.computeIfAbsent(item.getDocumentPrimaryKey(), k -> new SimpleIntegerProperty(0));
            int allocatedAmount = PriceFormatter.doublePriceToCentsPrice(flooredAllocations[i]);
            prop.setValue(allocatedAmount);
        }

        updateAllocationTotal();
    }

    /**
     * Rounds cents to the nearest whole currency unit (100 cents).
     * E.g., 5047 → 5000 (£50.47 → £50), 5050 → 5100 (£50.50 → £51)
     */
    protected int roundToWholeCurrencyUnit(int cents) {
        return ((cents + 50) / 100) * 100;
    }

    protected void handleCustomAmountChange(int value) {
        int rounded = roundToWholeCurrencyUnit(value);
        int bounded = Math.max(depositAmount, Math.min(totalAmount, rounded));
        customAmountProperty.set(bounded);
        autoAllocate();
        rebuildPaymentOptions(); // To update displayed amount
    }

    protected void updateAllocationTotal() {
        if (allocationTotalLabel == null || allocationWarningLabel == null) return;

        int allocatedTotal = getAllocationTotal();
        int paymentAmount = getPaymentAmount();

        // Compare rounded values since allocations are whole numbers
        boolean matches = allocatedTotal == paymentAmount;

        allocationTotalLabel.setText(EventPriceFormatter.formatWithCurrency(allocatedTotal, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
        // Update CSS classes for match/mismatch state
        allocationTotalLabel.getStyleClass().removeAll("bookingpage-text-primary", "bookingpage-text-danger");
        allocationTotalLabel.getStyleClass().add(matches ? "bookingpage-text-primary" : "bookingpage-text-danger");

        if (!matches) {
            I18nControls.bindI18nProperties(allocationWarningLabel, BookingPageI18nKeys.AllocationMismatchWarning,
                EventPriceFormatter.formatWithCurrency(paymentAmount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
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

        // Only add cards for available payment options
        if (availablePaymentOptions.contains(PaymentOption.DEPOSIT)) {
            // Pass the deposit percentage to the description
            String depositDescription = I18n.getI18nText(BookingPageI18nKeys.DepositRequiredPercent, depositPercentage);
            VBox depositCard = createPaymentOptionCard(PaymentOption.DEPOSIT, BookingPageI18nKeys.MinimumDeposit, depositDescription);
            HBox.setHgrow(depositCard, Priority.ALWAYS);
            paymentOptionsContainer.getChildren().add(depositCard);
        }

        if (availablePaymentOptions.contains(PaymentOption.CUSTOM)) {
            VBox customCard = createPaymentOptionCard(PaymentOption.CUSTOM, BookingPageI18nKeys.CustomAmount, I18n.getI18nText(BookingPageI18nKeys.ChooseYourAmount));
            HBox.setHgrow(customCard, Priority.ALWAYS);
            paymentOptionsContainer.getChildren().add(customCard);
        }

        if (availablePaymentOptions.contains(PaymentOption.FULL)) {
            VBox fullCard = createPaymentOptionCard(PaymentOption.FULL, BookingPageI18nKeys.PayInFullTitle, I18n.getI18nText(BookingPageI18nKeys.CompletePayment));
            HBox.setHgrow(fullCard, Priority.ALWAYS);
            paymentOptionsContainer.getChildren().add(fullCard);
        }
    }

    protected VBox createPaymentOptionCard(PaymentOption option, Object titleKey, String description) {
        boolean selected = paymentOptionProperty.get() == option;
        double amount = getAmountForOption(option);
        String amountText = EventPriceFormatter.formatWithCurrency((int) amount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null);

        // Use helper to create the card (CSS-based styling)
        return BookingPageUIBuilder.createPaymentOptionCard(
                I18n.getI18nText(titleKey),
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

    protected int getAmountForOption(PaymentOption option) {
        return switch (option) {
            case DEPOSIT -> depositAmount;
            case CUSTOM -> customAmountProperty.get() > 0 ? customAmountProperty.get() : totalAmount / 2;
            case FULL -> totalAmount;
        };
    }

    protected VBox buildPaymentMethodsSection() {
        VBox section = new VBox(16);

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.SelectPaymentMethod);
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

        // Card payment
        if (availablePaymentMethods.contains(PaymentMethod.CARD)) {
            VBox cardOption = createPaymentMethodCard(PaymentMethod.CARD, BookingPageI18nKeys.CreditDebitCard,
                    "M3 5a2 2 0 012-2h14a2 2 0 012 2v14a2 2 0 01-2 2H5a2 2 0 01-2-2V5z M3 10h18");
            HBox.setHgrow(cardOption, Priority.ALWAYS);
            paymentMethodsContainer.getChildren().add(cardOption);
        }

        // PayPal (not currently implemented, but kept for future use)
        if (availablePaymentMethods.contains(PaymentMethod.PAYPAL)) {
            VBox paypalOption = createPaymentMethodCard(PaymentMethod.PAYPAL, BookingPageI18nKeys.PayPal,
                    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z");
            HBox.setHgrow(paypalOption, Priority.ALWAYS);
            paymentMethodsContainer.getChildren().add(paypalOption);
        }

        // Bank Transfer
        if (availablePaymentMethods.contains(PaymentMethod.BANK)) {
            VBox bankOption = createPaymentMethodCard(PaymentMethod.BANK, BookingPageI18nKeys.BankTransfer,
                    "M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3");
            HBox.setHgrow(bankOption, Priority.ALWAYS);
            paymentMethodsContainer.getChildren().add(bankOption);
        }
    }

    protected VBox createPaymentMethodCard(PaymentMethod method, Object titleKey, String iconPath) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 16, 24, 16));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);

        boolean selected = paymentMethodProperty.get() == method;
        updatePaymentMethodCardStyle(card, selected);

        // Icon - use themed or muted icon based on selection
        SVGPath icon = selected
            ? BookingPageUIBuilder.createThemedIcon(iconPath, 1.2)
            : BookingPageUIBuilder.createMutedIcon(iconPath, 1.2);

        // Title
        Label titleLabel = I18nControls.newLabel(titleKey);
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

    protected void updatePaymentMethodCardStyle(VBox card, boolean selected) {
        // Use CSS classes for styling - hover effects handled by CSS
        card.getStyleClass().removeAll("bookingpage-selectable-card", "selected");
        card.getStyleClass().add("bookingpage-selectable-card");
        if (selected) {
            card.getStyleClass().add("selected");
        }
    }

    protected HBox buildTermsSection() {
        // Create HtmlText with hyperlink for Terms & Conditions
        HtmlText termsText = new HtmlText();
        I18n.bindI18nTextProperty(termsText.textProperty(), BookingPageI18nKeys.AcceptTermsHtml);
        termsText.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-secondary");
        HBox.setHgrow(termsText, Priority.ALWAYS);

        // Use checkbox card helper - handles indicator, click, and styling
        HBox card = BookingPageUIBuilder.createCheckboxCard(termsText, termsAcceptedProperty, colorScheme);
        card.setPadding(new Insets(20));

        return card;
    }

    protected void updateTotalDisplay() {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(EventPriceFormatter.formatWithCurrency(totalAmount, workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null ? workingBookingProperties.getWorkingBooking().getEvent() : null));
            // Note: Color is handled via CSS class "bookingpage-text-primary" set in buildBookingSummarySection
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
        Label label = I18nControls.newLabel(BookingPageI18nKeys.ReviewRegistrationsSubtitle);
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
    public void setTotalAmount(int amount) {
        this.totalAmount = amount;
        // Note: depositAmount should be set separately via setDepositAmount()
        // Only set default deposit here if not yet set
        if (this.depositAmount == 0) {
            this.depositAmount = roundToWholeCurrencyUnit(amount / 10); // Default 10% deposit, rounded
        }
        updateDepositPercentageAndOptions();
        this.customAmountProperty.set(roundToWholeCurrencyUnit(amount / 2)); // Default 50%, rounded
        updateTotalDisplay();
        rebuildPaymentOptions();
        updateCustomAmountSectionVisibility();
        updateAllocationSectionVisibility();
        updatePayButtonText();
    }

    @Override
    public int getTotalAmount() {
        return totalAmount;
    }

    @Override
    public void setDepositAmount(int amount) {
        this.depositAmount = roundToWholeCurrencyUnit(amount);
        updateDepositPercentageAndOptions();
        rebuildPaymentOptions();
        updateCustomAmountSectionVisibility();
        updatePayButtonText();
    }

    /**
     * Updates the deposit percentage and adjusts available payment options.
     * If deposit equals total, only "Pay in Full" option is available.
     */
    protected void updateDepositPercentageAndOptions() {
        if (totalAmount > 0) {
            // Calculate percentage, rounding to nearest integer
            this.depositPercentage = Math.round((float) depositAmount * 100 / totalAmount);
        } else {
            this.depositPercentage = 0;
        }

        // If deposit equals total (100%), only "Pay in Full" is available
        if (depositAmount >= totalAmount) {
            this.availablePaymentOptions = EnumSet.of(PaymentOption.FULL);
            paymentOptionProperty.set(PaymentOption.FULL);
        } else {
            // Reset to all options if previously restricted
            if (this.availablePaymentOptions.size() == 1 && this.availablePaymentOptions.contains(PaymentOption.FULL)) {
                this.availablePaymentOptions = EnumSet.allOf(PaymentOption.class);
            }
        }

        // Update payment amount section visibility
        updatePaymentAmountSectionVisibility();
    }

    /**
     * Shows or hides the "How much would you like to pay" section.
     * If there's only one payment option (e.g., only Pay in Full), hide the entire section.
     */
    protected void updatePaymentAmountSectionVisibility() {
        if (paymentAmountSection == null) return;

        boolean hasMultipleOptions = availablePaymentOptions.size() > 1;
        paymentAmountSection.setVisible(hasMultipleOptions);
        paymentAmountSection.setManaged(hasMultipleOptions);
    }

    @Override
    public void setAvailablePaymentOptions(Set<PaymentOption> options) {
        this.availablePaymentOptions = options != null ? options : EnumSet.allOf(PaymentOption.class);

        // If current selection is no longer available, switch to first available option
        if (!availablePaymentOptions.contains(paymentOptionProperty.get())) {
            paymentOptionProperty.set(availablePaymentOptions.iterator().next());
        }

        rebuildPaymentOptions();
        updatePaymentAmountSectionVisibility();
        updateCustomAmountSectionVisibility();
        updatePayButtonText();
    }

    @Override
    public Set<PaymentOption> getAvailablePaymentOptions() {
        return Collections.unmodifiableSet(availablePaymentOptions);
    }

    @Override
    public void setAvailablePaymentMethods(Set<PaymentMethod> methods) {
        this.availablePaymentMethods = methods != null ? methods : EnumSet.of(PaymentMethod.CARD, PaymentMethod.BANK);

        // If current selection is no longer available, switch to first available method
        if (!availablePaymentMethods.contains(paymentMethodProperty.get())) {
            paymentMethodProperty.set(availablePaymentMethods.iterator().next());
        }

        rebuildPaymentMethods();
        updatePaymentMethodsSectionVisibility();
    }

    /**
     * Shows or hides the "Select Payment Method" section.
     * If there's only one payment method available, hide the entire section.
     */
    protected void updatePaymentMethodsSectionVisibility() {
        if (paymentMethodsSection == null) return;

        boolean hasMultipleMethods = availablePaymentMethods.size() > 1;
        paymentMethodsSection.setVisible(hasMultipleMethods);
        paymentMethodsSection.setManaged(hasMultipleMethods);
    }

    @Override
    public Set<PaymentMethod> getAvailablePaymentMethods() {
        return Collections.unmodifiableSet(availablePaymentMethods);
    }

    @Override
    public void addBookingItem(PaymentBookingItem item) {
        bookingItems.add(item);
        // Initialize allocation for this item
        allocationProperties.computeIfAbsent(item.getDocumentPrimaryKey(), k -> new SimpleIntegerProperty(0));
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
            allocationProperties.computeIfAbsent(item.getDocumentPrimaryKey(), k -> new SimpleIntegerProperty(0));
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
    public int getPaymentAmount() {
        return getAmountForOption(paymentOptionProperty.get());
    }

    @Override
    public boolean isTermsAccepted() {
        return termsAcceptedProperty.get();
    }

    @Override
    public void setOnPaymentSubmit(AsyncFunction<PaymentResult, Void> callback) {
        this.onPaymentSubmit = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    @Override
    public Future<PaymentResult> submitPaymentAsync() {

        PaymentResult result = new PaymentResult(
                paymentOptionProperty.get(),
                paymentMethodProperty.get(),
                getPaymentAmount(),
                getAllocations()
        );

        // Notify callback if set
        if (onPaymentSubmit != null) {
            return onPaymentSubmit.apply(result)
                .map(ignored -> result);
        }

        // Should be for testing only (skipping actual payment)
        return Future.succeededFuture(result);
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
    public Map<Object, Integer> getAllocations() {
        Map<Object, Integer> result = new HashMap<>();
        for (Map.Entry<Object, IntegerProperty> entry : allocationProperties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    @Override
    public void setAllocation(Object itemId, int amount) {
        IntegerProperty prop = allocationProperties.get(itemId);
        if (prop != null) {
            prop.set(amount);
            updateAllocationTotal();
        }
    }

    @Override
    public void setAllocation(String itemId, double amount) {
        setAllocation((Object) itemId, (int) (amount * 100));
    }
}
