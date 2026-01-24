package one.modality.booking.frontoffice.bookingpage.sections.generalprogram;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasClassDateSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasRateTypeSection;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of a class date selection section for General Program booking forms.
 * Displays a grid of date cards that users can select/deselect.
 * Shows total price with discount when all classes are selected.
 *
 * <p>Uses CSS-based theming. Styling is handled via CSS classes that inherit
 * theme colors from CSS variables set on the parent container.</p>
 *
 * @author Claude
 * @see HasClassDateSelectionSection
 */
public class DefaultClassDateSelectionSection implements BookingFormSection, ResettableSection, HasClassDateSelectionSection {

    // UI Components
    private final VBox container = new VBox(16);
    private final FlowPane dateCardsContainer = new FlowPane();
    private final HBox selectAllBar = new HBox(12);
    private final VBox priceSummaryBox = new VBox(12);

    // Select All Bar components (need field references for updates after data loads)
    private Label selectAllMessageLabel;
    private Button selectAllActionButton;

    // State
    private final ObservableList<ScheduledItem> availableItems = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> selectedItems = FXCollections.observableArrayList();
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);

    // Already booked items (from existing booking - locked, cannot be deselected)
    private final Set<Object> alreadyBookedItemIds = new HashSet<>();

    // Working booking reference
    private WorkingBookingProperties workingBookingProperties;

    // Price tracking
    private int pricePerClass = 0;
    private int allClassesPrice = 0;
    private int allClassesDiscount = 0;

    public DefaultClassDateSelectionSection() {
        buildUI();
        setupBindings();
    }

    private void buildUI() {
        // Section header
        StyledSectionHeader header = new StyledSectionHeader(
                BookingPageI18nKeys.ClassSelectionYourClasses,
                StyledSectionHeader.ICON_CALENDAR
        );

        // Date cards grid
        dateCardsContainer.setHgap(12);
        dateCardsContainer.setVgap(12);
        dateCardsContainer.setPadding(new Insets(0));
        dateCardsContainer.setAlignment(Pos.TOP_LEFT);
        dateCardsContainer.getStyleClass().add("gpclass-date-cards-container");

        // Select all bar
        buildSelectAllBar();

        // Price summary
        priceSummaryBox.setPadding(new Insets(24));
        priceSummaryBox.getStyleClass().addAll("bookingpage-card-static", "gpclass-price-summary");
        priceSummaryBox.setVisible(false);
        priceSummaryBox.setManaged(false);

        container.getChildren().addAll(header, dateCardsContainer, selectAllBar, priceSummaryBox);
        container.getStyleClass().add("gpclass-date-selection-section");
        container.setMinWidth(0);
    }

    private void buildSelectAllBar() {
        selectAllBar.setAlignment(Pos.CENTER_LEFT);
        selectAllBar.setPadding(new Insets(12, 16, 12, 16));
        selectAllBar.getStyleClass().add("bookingpage-select-all-bar");
        HBox.setHgrow(selectAllBar, Priority.ALWAYS);

        // Message label (stored as field for updates after data loads)
        selectAllMessageLabel = new Label();
        selectAllMessageLabel.getStyleClass().add("bookingpage-select-all-message");

        // Spacer to push button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Select/Clear all button (stored as field for updates after data loads)
        selectAllActionButton = new Button();
        selectAllActionButton.setCursor(Cursor.HAND);
        // Initial button style (primary - "Select All")
        selectAllActionButton.getStyleClass().add("bookingpage-btn-small-primary");

        // Update message and button based on selection changes
        selectedItems.addListener((ListChangeListener<ScheduledItem>) change -> {
            // Keeping the working booking updated when the user selects/deselects classes
            if (workingBookingProperties != null) {
                workingBookingProperties.getWorkingBooking().bookScheduledItems(selectedItems, false);
            }
            updateSelectAllBar();
        });

        selectAllActionButton.setOnAction(e -> {
            boolean allSelected = selectedItems.size() == availableItems.size();
            if (allSelected) {
                clearAllDates();
            } else {
                selectAllDates();
            }
        });

        selectAllBar.getChildren().addAll(selectAllMessageLabel, spacer, selectAllActionButton);

        // Set initial text (will show placeholder values until data loads)
        updateSelectAllBar();
    }

    /**
     * Updates the select all bar message and button based on current state.
     * Called after data loads and whenever selection changes.
     */
    private void updateSelectAllBar() {
        if (selectAllMessageLabel == null || selectAllActionButton == null) {
            return;
        }

        boolean allSelected = selectedItems.size() == availableItems.size() && !availableItems.isEmpty();

        if (allSelected) {
            // All selected state - green message, outline button
            if (!selectAllMessageLabel.getStyleClass().contains("all-selected")) {
                selectAllMessageLabel.getStyleClass().add("all-selected");
            }
            // Only show "Save" text if there's actually a discount
            String messageText = "✓ " + I18n.getI18nText(BookingPageI18nKeys.ClassSelectionAllSelected, availableItems.size());
            if (allClassesDiscount > 0) {
                messageText += " — " + I18n.getI18nText(BookingPageI18nKeys.ClassSelectionSave, formatPrice(allClassesDiscount));
            }
            selectAllMessageLabel.setText(messageText);
            selectAllActionButton.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionClearAll));
            // Switch to outline button style
            selectAllActionButton.getStyleClass().remove("bookingpage-btn-small-primary");
            if (!selectAllActionButton.getStyleClass().contains("bookingpage-btn-small-outline")) {
                selectAllActionButton.getStyleClass().add("bookingpage-btn-small-outline");
            }
        } else {
            // Not all selected state - muted message, primary button
            selectAllMessageLabel.getStyleClass().remove("all-selected");
            // Only show discount info if there's actually a discount
            if (allClassesDiscount > 0) {
                selectAllMessageLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionSaveBySelectingAll,
                        availableItems.size(), formatPrice(allClassesPrice), formatPrice(allClassesDiscount)));
            } else {
                selectAllMessageLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionSelectAllFor,
                        availableItems.size(), formatPrice(allClassesPrice)));
            }
            selectAllActionButton.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionSelectAll));
            // Switch to primary button style
            selectAllActionButton.getStyleClass().remove("bookingpage-btn-small-outline");
            if (!selectAllActionButton.getStyleClass().contains("bookingpage-btn-small-primary")) {
                selectAllActionButton.getStyleClass().add("bookingpage-btn-small-primary");
            }
        }
    }

    private void setupBindings() {
        // Valid when at least one date is selected (for new bookings)
        // This binding will be updated in updateValidityBinding() for modifications
        validProperty.bind(Bindings.isNotEmpty(selectedItems));

        // Update price summary when selection changes
        selectedItems.addListener((ListChangeListener<ScheduledItem>) change -> {
            updatePriceSummary();
            updateWorkingBooking();
            updateValidityBinding();
        });
    }

    /**
     * Updates the validity binding based on whether this is a modification or new booking.
     * For new bookings: valid when at least one date is selected.
     * For modifications: valid when at least one NEW date is selected (beyond already-booked).
     */
    private void updateValidityBinding() {
        boolean hasExistingBooking = !alreadyBookedItemIds.isEmpty();
        if (hasExistingBooking) {
            // Unbind first, then rebind with new condition for modifications
            validProperty.unbind();
            validProperty.bind(Bindings.createBooleanBinding(
                () -> getNewlySelectedCount() > 0,
                selectedItems
            ));
        }
        // For new bookings, keep the original binding (isNotEmpty)
    }

    /**
     * Checks if a scheduled item is already booked in an existing booking.
     * Already booked items are locked and cannot be deselected.
     */
    private boolean isAlreadyBooked(ScheduledItem item) {
        return alreadyBookedItemIds.contains(item.getPrimaryKey());
    }

    /**
     * Public accessor for checking if a scheduled item is already booked.
     * Used by summary sections for differential pricing display.
     */
    public boolean isItemAlreadyBooked(ScheduledItem item) {
        return isAlreadyBooked(item);
    }

    /**
     * Returns the count of newly selected items (excluding already-booked items).
     * Used for calculating pricing in modification mode.
     */
    private int getNewlySelectedCount() {
        return (int) selectedItems.stream()
            .filter(item -> !isAlreadyBooked(item))
            .count();
    }

    /**
     * Checks if a scheduled item has passed and is no longer bookable.
     * A class is considered past if current time is more than 1 hour after its start time.
     */
    private boolean isClassPast(ScheduledItem item) {
        LocalDate date = item.getDate();
        if (date == null) return true;

        LocalTime startTime = item.getStartTime();
        LocalDateTime now = LocalDateTime.now();

        if (startTime != null) {
            // Class is past if current time is 1+ hour after start time
            LocalDateTime classStart = LocalDateTime.of(date, startTime);
            return now.isAfter(classStart.plusHours(1));
        } else {
            // No time info - just check if date is before today
            return date.isBefore(LocalDate.now());
        }
    }

    /**
     * Creates a date card for a ScheduledItem.
     */
    private Node createDateCard(ScheduledItem item) {
        LocalDate date = item.getDate();
        if (date == null) return null;

        // Detect if this class has passed (1 hour after start time) - not selectable
        boolean isPastDate = isClassPast(item);

        // Detect if this class is already booked (locked - cannot be deselected)
        boolean isLocked = isAlreadyBooked(item);

        // Use AnchorPane for absolute positioning of indicator in top-right corner
        AnchorPane card = new AnchorPane();
        card.setMinWidth(180);
        card.setPrefWidth(180);
        card.setMaxWidth(200);
        card.getStyleClass().addAll("bookingpage-card", "gpclass-date-card");

        // Apply past date styling using CSS helper (grayed out, not clickable)
        BookingPageUIBuilder.applyPastDateStyle(card, isPastDate);

        // Apply locked styling for already booked items
        if (isLocked) {
            card.getStyleClass().addAll("selected", "locked");
        }

        // Only set hand cursor for selectable (future, non-locked) dates
        if (!isPastDate && !isLocked) {
            card.setCursor(Cursor.HAND);
        }

        // Check if selected (locked items are always selected)
        BooleanProperty isSelected = new SimpleBooleanProperty(isLocked || selectedItems.contains(item));
        if (!isLocked) {
            selectedItems.addListener((ListChangeListener<ScheduledItem>) change -> {
                isSelected.set(selectedItems.contains(item));
            });
        }

        // Empty circle indicator (top-right corner) - shown when NOT selected and not locked
        StackPane emptyCircle = BookingPageUIBuilder.createEmptyCircleIndicator(20);
        emptyCircle.setVisible(!isSelected.get() && !isPastDate && !isLocked);

        // Selection checkmark (top-right corner) - shown when selected (including locked)
        StackPane checkmark = BookingPageUIBuilder.createCheckmarkBadgeCss(20);
        checkmark.setVisible(isSelected.get());

        // Update indicator visibility and card styling based on selection (only for non-locked)
        if (!isLocked) {
            isSelected.addListener((obs, old, selected) -> {
                emptyCircle.setVisible(!selected && !isPastDate);
                checkmark.setVisible(selected);
                if (selected) {
                    if (!card.getStyleClass().contains("selected")) {
                        card.getStyleClass().add("selected");
                    }
                } else {
                    card.getStyleClass().remove("selected");
                }
            });
            // Apply initial selected state
            if (isSelected.get()) {
                card.getStyleClass().add("selected");
            }
        }

        // Date info row
        HBox dateRow = new HBox(12);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(14, 16, 14, 16));  // Padding on content, not card

        // Date box (day number + month) - styling via CSS class
        VBox dateBox = new VBox(0);
        dateBox.setMinSize(44, 44);
        dateBox.setMaxSize(44, 44);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.getStyleClass().add("gpclass-date-box");

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("gpclass-date-day");

        Label monthLabel = new Label(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase());
        monthLabel.getStyleClass().add("gpclass-date-month");

        dateBox.getChildren().addAll(dayLabel, monthLabel);

        // Day name and time - styling via CSS classes
        VBox dayTimeBox = new VBox(2);
        dayTimeBox.setAlignment(Pos.CENTER_LEFT);

        Label dayNameLabel = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        dayNameLabel.getStyleClass().add("gpclass-date-dayname");

        // Get time from ScheduledItem if available
        String timeText = item.getStartTime() != null ?
                item.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("gpclass-date-time");

        dayTimeBox.getChildren().addAll(dayNameLabel, timeLabel);

        dateRow.getChildren().addAll(dateBox, dayTimeBox);

        // Indicator container - positioned absolutely 8px from top-right corner
        StackPane indicatorContainer = new StackPane(emptyCircle, checkmark);
        indicatorContainer.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(indicatorContainer, 8.0);
        AnchorPane.setRightAnchor(indicatorContainer, 8.0);

        // Date row fills the card (with bottom margin to make room for badge if locked)
        AnchorPane.setTopAnchor(dateRow, 0.0);
        AnchorPane.setLeftAnchor(dateRow, 0.0);
        AnchorPane.setBottomAnchor(dateRow, isLocked ? 24.0 : 0.0);
        AnchorPane.setRightAnchor(dateRow, 0.0);

        // Add content and indicator to card
        card.getChildren().addAll(dateRow, indicatorContainer);

        // Add "BOOKED" badge at bottom for locked (already booked) items
        if (isLocked) {
            Label bookedBadge = I18nControls.newLabel(BookingPageI18nKeys.ClassSelectionBooked);
            bookedBadge.getStyleClass().add("gpclass-booked-badge");
            bookedBadge.setPadding(new Insets(2, 6, 2, 6));
            HBox badgeContainer = new HBox(bookedBadge);
            badgeContainer.setAlignment(Pos.CENTER);
            badgeContainer.setPadding(new Insets(0, 0, 6, 0));
            AnchorPane.setBottomAnchor(badgeContainer, 0.0);
            AnchorPane.setLeftAnchor(badgeContainer, 0.0);
            AnchorPane.setRightAnchor(badgeContainer, 0.0);
            card.getChildren().add(badgeContainer);
        }

        // Click handler and hover effects only for selectable (future, non-locked) dates
        if (!isPastDate && !isLocked) {
            card.setOnMouseClicked(e -> {
                toggleDateSelection(item);
                e.consume();
            });

            // Hover effects via CSS class (same visual as selected)
            card.setOnMouseEntered(e -> {
                if (!isSelected.get()) {
                    card.getStyleClass().add("hovered");
                }
            });
            card.setOnMouseExited(e -> {
                card.getStyleClass().remove("hovered");
            });
        }

        return card;
    }

    private void toggleDateSelection(ScheduledItem item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
    }

    private void selectAllDates() {
        selectedItems.setAll(availableItems);
    }

    private void clearAllDates() {
        // Clear only non-locked items (keep already booked items selected)
        List<ScheduledItem> itemsToKeep = selectedItems.stream()
                .filter(this::isAlreadyBooked)
                .collect(Collectors.toList());
        selectedItems.setAll(itemsToKeep);
    }

    private void updatePriceSummary() {
        priceSummaryBox.getChildren().clear();

        if (selectedItems.isEmpty()) {
            priceSummaryBox.setVisible(false);
            priceSummaryBox.setManaged(false);
            return;
        }

        priceSummaryBox.setVisible(true);
        priceSummaryBox.setManaged(true);

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.ClassSelectionYourSelection);
        titleLabel.getStyleClass().add("gpclass-price-summary-title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 4, 0)); // 4px extra to achieve 16px total (12px VBox spacing + 4px)

        boolean allSelected = selectedItems.size() == availableItems.size();
        int newlySelectedCount = getNewlySelectedCount();
        boolean hasExistingBooking = !alreadyBookedItemIds.isEmpty();

        // Calculate prices
        workingBookingProperties.updateAll();
        int subtotal = getSubtotal();
        int total = getTotalPrice();
        int discount = subtotal - total;

        // Subtotal row
        HBox subtotalRow = new HBox();
        subtotalRow.setAlignment(Pos.CENTER_LEFT);
        subtotalRow.setPadding(new Insets(0, 12, 0, 12));

        Label subtotalLabel = new Label();
        if (hasExistingBooking) {
            // Modification: show new selections only
            if (newlySelectedCount == 0) {
                subtotalLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionNoNewClassesSelected));
            } else {
                subtotalLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionNewClassesSelected, newlySelectedCount, formatPrice(pricePerClass)));
            }
        } else if (allSelected) {
            subtotalLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionFullTermPrice, availableItems.size()));
        } else {
            subtotalLabel.setText(I18n.getI18nText(BookingPageI18nKeys.ClassSelectionSingleClassPrice, selectedItems.size(), formatPrice(pricePerClass)));
        }
        subtotalLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");

        Region subtotalSpacer = new Region();
        HBox.setHgrow(subtotalSpacer, Priority.ALWAYS);

        Label subtotalValue = new Label(formatPrice(subtotal));
        subtotalValue.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");

        subtotalRow.getChildren().addAll(subtotalLabel, subtotalSpacer, subtotalValue);

        priceSummaryBox.getChildren().addAll(titleLabel, subtotalRow);

        // Discount row (if all selected) - uses CSS classes for theme-compliant colors
        if (discount > 0) {
            HBox discountRow = new HBox();
            discountRow.setAlignment(Pos.CENTER_LEFT);
            discountRow.getStyleClass().add("bookingpage-discount-row");

            Label discountLabel = I18nControls.newLabel(BookingPageI18nKeys.ClassSelectionFullTermDiscount);
            discountLabel.getStyleClass().add("bookingpage-discount-label");

            Region discountSpacer = new Region();
            HBox.setHgrow(discountSpacer, Priority.ALWAYS);

            Label discountValue = new Label("-" + formatPrice(discount));
            discountValue.getStyleClass().add("bookingpage-discount-value");

            discountRow.getChildren().addAll(discountLabel, discountSpacer, discountValue);
            priceSummaryBox.getChildren().add(discountRow);
        }

        // Divider
        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.getStyleClass().add("gpclass-price-summary-divider");
        VBox.setMargin(divider, new Insets(4, 0, 4, 0));

        // Total row
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(0, 12, 0, 12)); // Match subtotal row padding for value alignment

        Label totalLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.Total));
        totalLabel.getStyleClass().add("gpclass-price-summary-total-label");

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        Label totalValue = new Label(formatPrice(total));
        totalValue.getStyleClass().addAll("gpclass-price-summary-total-value", "bookingpage-text-primary");

        totalRow.getChildren().addAll(totalLabel, totalSpacer, totalValue);

        priceSummaryBox.getChildren().addAll(divider, totalRow);
    }

    private void updateWorkingBooking() {
        if (workingBookingProperties == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        if (workingBooking == null) return;

        // Book selected items into WorkingBooking (replaces existing selections)
        List<ScheduledItem> itemsToBook = new ArrayList<>(selectedItems);
        if (!itemsToBook.isEmpty()) {
            workingBooking.bookScheduledItems(itemsToBook, false);
        } else {
            // Clear any existing bookings for this event by unbooking all available items
            workingBooking.unbookScheduledItems(new ArrayList<>(availableItems));
        }
    }

    private String formatPrice(int priceInCents) {
        Event event = getEvent();
        return EventPriceFormatter.formatWithCurrency(priceInCents, event);
    }

    private void loadData() {
        if (workingBookingProperties == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();

        if (policyAggregate == null) return;

        // Load already booked items from existing booking (for modification flow)
        alreadyBookedItemIds.clear();
        List<ScheduledItem> alreadyBookedItems = workingBooking.getAlreadyBookedScheduledItems();
        for (ScheduledItem bookedItem : alreadyBookedItems) {
            alreadyBookedItemIds.add(bookedItem.getPrimaryKey());
        }

        // PolicyAggregate.getScheduledItems() already returns only bookable items
        // (server-side filtered by bookableScheduledItem=id in ServerDocumentServiceProvider)
        List<ScheduledItem> scheduledItems = policyAggregate.getScheduledItems().stream()
                .sorted((a, b) -> {
                    if (a.getDate() == null || b.getDate() == null) return 0;
                    return a.getDate().compareTo(b.getDate());
                })
                .collect(Collectors.toList());

        availableItems.setAll(scheduledItems);

        // Pre-select already booked items (they are locked and cannot be deselected)
        for (ScheduledItem item : scheduledItems) {
            if (isAlreadyBooked(item) && !selectedItems.contains(item)) {
                selectedItems.add(item);
            }
        }

        // Get pricing info - use the rate for the first scheduled item (more accurate than generic daily rate)
        if (!scheduledItems.isEmpty()) {
            Rate scheduledItemRate = policyAggregate.getScheduledItemDailyRate(scheduledItems.get(0));
            pricePerClass = scheduledItemRate != null ? scheduledItemRate.getPrice() : 0;
        } else {
            pricePerClass = policyAggregate.getDailyRatePrice();
        }

        // Calculate all classes price and discount using the pricing API
        // The API applies fixed rate discounts from database if cheaper than daily rate total
        int wholeEventPrice = workingBooking.getWholeEventPrice();
        int wholeEventNoDiscountPrice = workingBooking.getWholeEventNoDiscountPrice();
        allClassesDiscount = wholeEventNoDiscountPrice - wholeEventPrice;
        allClassesPrice = wholeEventPrice;

        // Build date cards
        dateCardsContainer.getChildren().clear();
        for (ScheduledItem item : availableItems) {
            Node card = createDateCard(item);
            if (card != null) {
                dateCardsContainer.getChildren().add(card);
            }
        }

        // Only show "Select All" bar if the first class hasn't started yet
        // (i.e., all classes can still be booked - using 1 hour grace period)
        boolean firstClassBookable = !availableItems.isEmpty() && !isClassPast(availableItems.get(0));
        selectAllBar.setVisible(firstClassBookable);
        selectAllBar.setManaged(firstClassBookable);

        // Update select all bar with correct values now that data is loaded
        updateSelectAllBar();

        // Update validity binding (for modifications, need new selections to continue)
        updateValidityBinding();
    }

    // === BookingFormSection Implementation ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.ClassSelectionYourClasses;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        loadData();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === ResettableSection Implementation ===

    @Override
    public void reset() {
        // Clear only non-locked items (keep already booked items selected)
        List<ScheduledItem> itemsToKeep = selectedItems.stream()
                .filter(this::isAlreadyBooked)
                .collect(Collectors.toList());
        selectedItems.setAll(itemsToKeep);
    }

    // === Public Accessors ===

    /**
     * Returns the list of selected ScheduledItems.
     */
    @Override
    public ObservableList<ScheduledItem> getSelectedItems() {
        return selectedItems;
    }

    /**
     * Returns whether all available dates are selected.
     */
    @Override
    public boolean isAllDatesSelected() {
        return selectedItems.size() == availableItems.size() && !availableItems.isEmpty();
    }

    /**
     * Returns the total price in cents for the current selection.
     */
    @Override
    public int getTotalPrice() {
        return workingBookingProperties == null ? 0 : workingBookingProperties.getTotal();
    }

    /**
     * Returns the subtotal (before discount) in cents for the current selection.
     */
    @Override
    public int getSubtotal() {
        return workingBookingProperties == null ? 0 : workingBookingProperties.getNoDiscountTotal();
    }

    /**
     * Returns the discount amount in cents (only applies when all classes selected).
     */
    @Override
    public int getDiscount() {
        return getSubtotal() - getTotalPrice();
    }

    /**
     * Returns the price per individual class in cents.
     */
    @Override
    public int getPricePerClass() {
        return pricePerClass;
    }

    @Override
    public void setRateType(HasRateTypeSection.RateType rateType) {
        // Indicating the working booking if it should apply the facility fee rate
        if (workingBookingProperties != null) {
            workingBookingProperties.getWorkingBooking().applyFacilityFeeRate(rateType == HasRateTypeSection.RateType.MEMBER);
            updatePriceSummary();
        }
    }

    /**
     * Returns the event for price formatting.
     */
    public Event getEvent() {
        if (workingBookingProperties != null) {
            return workingBookingProperties.getEvent();
        }
        return null;
    }
}
