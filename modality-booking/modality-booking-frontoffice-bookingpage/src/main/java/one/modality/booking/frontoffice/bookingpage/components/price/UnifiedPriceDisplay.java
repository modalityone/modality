package one.modality.booking.frontoffice.bookingpage.components.price;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;

import java.time.LocalDate;

/**
 * Unified utility for creating consistent price display components across the booking flow.
 * <p>
 * This class centralizes:
 * <ul>
 *   <li>Price formatting using event-aware currency ({@link EventPriceFormatter})</li>
 *   <li>Date range formatting using {@link ModalityDates}</li>
 *   <li>Flexible ItemFamily + Item name formatting</li>
 *   <li>Reusable UI components for price rows, line items, and summaries</li>
 * </ul>
 * <p>
 * All UI components have text wrapping enabled for mobile-friendly display.
 *
 * @author Claude
 */
public class UnifiedPriceDisplay {

    private final Event event;
    private LineItemDisplayFormat displayFormat = LineItemDisplayFormat.FAMILY_DASH_ITEM;

    /**
     * Creates a new UnifiedPriceDisplay for the given event.
     *
     * @param event the event used for currency detection (can be null, defaults to GBP)
     */
    public UnifiedPriceDisplay(Event event) {
        this.event = event;
    }

    // ========================================
    // CONFIGURATION
    // ========================================

    /**
     * Sets the display format for line item names.
     *
     * @param format the format to use for displaying ItemFamily + Item names
     * @return this instance for fluent configuration
     */
    public UnifiedPriceDisplay withDisplayFormat(LineItemDisplayFormat format) {
        this.displayFormat = format;
        return this;
    }

    /**
     * Gets the current display format.
     *
     * @return the current LineItemDisplayFormat
     */
    public LineItemDisplayFormat getDisplayFormat() {
        return displayFormat;
    }

    /**
     * Gets the event used for currency detection.
     *
     * @return the event, or null if not set
     */
    public Event getEvent() {
        return event;
    }

    // ========================================
    // FORMATTING (delegates to existing APIs)
    // ========================================

    /**
     * Formats a price amount using the event's currency.
     * Delegates to {@link EventPriceFormatter#formatWithCurrency(Object, Event)}.
     *
     * @param amountInCents the price in cents/pence
     * @return formatted price string (e.g., "£ 100.00", "€ 50.00", "$ 75.00")
     */
    public String formatPrice(int amountInCents) {
        return EventPriceFormatter.formatWithCurrency(amountInCents, event);
    }

    /**
     * Formats a date range for display.
     * Delegates to {@link ModalityDates#formatDateInterval(LocalDate, LocalDate)}.
     *
     * @param start the start date
     * @param end the end date
     * @return formatted date range (e.g., "15-20 January" or "15 January - 3 February")
     */
    public String formatDateRange(LocalDate start, LocalDate end) {
        return ModalityDates.formatDateInterval(start, end);
    }

    /**
     * Formats the display name for a line item based on the configured format.
     *
     * @param familyName ItemFamily name (can be null)
     * @param itemName Item name (should not be null, defaults to "Item")
     * @return formatted display name based on current {@link #displayFormat}
     */
    public String formatLineItemName(String familyName, String itemName) {
        String safeItemName = itemName != null ? itemName : "Item";

        switch (displayFormat) {
            case ITEM_ONLY:
                return safeItemName;

            case FAMILY_ONLY:
                return familyName != null ? familyName : safeItemName;

            case ITEM_PARENTHESIS_FAMILY:
                return familyName != null
                        ? safeItemName + " (" + familyName + ")"
                        : safeItemName;

            case FAMILY_DASH_ITEM:
            default:
                return familyName != null
                        ? familyName + " - " + safeItemName
                        : safeItemName;
        }
    }

    /**
     * Formats the display name for a line item using entity objects.
     *
     * @param family the ItemFamily entity (can be null)
     * @param item the Item entity (can be null)
     * @return formatted display name
     */
    public String formatLineItemName(ItemFamily family, Item item) {
        String familyName = family != null ? family.getName() : null;
        String itemName = item != null ? item.getName() : null;
        return formatLineItemName(familyName, itemName);
    }

    // ========================================
    // UI COMPONENTS
    // ========================================

    /**
     * Creates a line item row with ItemFamily and Item names.
     * Text wraps for mobile-friendly display.
     *
     * @param familyName ItemFamily name (can be null)
     * @param itemName Item name
     * @param dates date range string (can be null)
     * @param amount price in cents
     * @return HBox containing the line item row
     */
    public HBox createLineItem(String familyName, String itemName, String dates, int amount) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        VBox labelBox = new VBox(2);
        HBox.setHgrow(labelBox, Priority.ALWAYS);

        // Primary: formatted name (wraps for mobile)
        Label nameLabel = new Label(formatLineItemName(familyName, itemName));
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        labelBox.getChildren().add(nameLabel);

        // Secondary: dates (if provided)
        if (dates != null && !dates.isEmpty()) {
            Label datesLabel = new Label(dates);
            datesLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            datesLabel.setWrapText(true);
            labelBox.getChildren().add(datesLabel);
        }

        // Price (right-aligned, doesn't shrink)
        Label priceLabel = new Label(formatPrice(amount));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        priceLabel.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(labelBox, priceLabel);
        return row;
    }

    /**
     * Creates a line item row directly from entity objects.
     *
     * @param family the ItemFamily entity (can be null)
     * @param item the Item entity
     * @param dates date range string (can be null)
     * @param amount price in cents
     * @return HBox containing the line item row
     */
    public HBox createLineItem(ItemFamily family, Item item, String dates, int amount) {
        String familyName = family != null ? family.getName() : null;
        String itemName = item != null ? item.getName() : null;
        return createLineItem(familyName, itemName, dates, amount);
    }

    /**
     * Creates a line item row for an "Included" item (no price shown).
     *
     * @param familyName ItemFamily name (can be null)
     * @param itemName Item name
     * @param dates date range string (can be null)
     * @return HBox containing the line item row with "Included" label
     */
    public HBox createIncludedLineItem(String familyName, String itemName, String dates) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        VBox labelBox = new VBox(2);
        HBox.setHgrow(labelBox, Priority.ALWAYS);

        Label nameLabel = new Label(formatLineItemName(familyName, itemName));
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        labelBox.getChildren().add(nameLabel);

        if (dates != null && !dates.isEmpty()) {
            Label datesLabel = new Label(dates);
            datesLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            datesLabel.setWrapText(true);
            labelBox.getChildren().add(datesLabel);
        }

        // "Included" label instead of price
        Label includedLabel = I18nControls.newLabel(BookingPageI18nKeys.Included);
        includedLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-muted");
        includedLabel.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(labelBox, includedLabel);
        return row;
    }

    /**
     * Creates a standard price row with an i18n label and amount.
     *
     * @param labelI18nKey the i18n key for the label
     * @param amount price in cents
     * @return HBox containing the price row
     */
    public HBox createPriceRow(Object labelI18nKey, int amount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");
        label.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(formatPrice(amount));
        value.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        value.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Creates a muted price row (secondary styling, e.g., for "Already Paid").
     *
     * @param labelI18nKey the i18n key for the label
     * @param amount price in cents
     * @return HBox containing the price row with muted styling
     */
    public HBox createMutedPriceRow(Object labelI18nKey, int amount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-muted");
        label.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(formatPrice(amount));
        value.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-muted");
        value.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Creates a success-styled price row (green, e.g., for positive "Already Paid").
     *
     * @param labelI18nKey the i18n key for the label
     * @param amount price in cents
     * @return HBox containing the price row with success styling
     */
    public HBox createSuccessPriceRow(Object labelI18nKey, int amount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-muted");
        label.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(formatPrice(amount));
        value.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-success");
        value.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Creates an emphasized total row (larger, bold, primary color).
     *
     * @param labelI18nKey the i18n key for the label
     * @param amount price in cents
     * @return HBox containing the total row
     */
    public HBox createTotalRow(Object labelI18nKey, int amount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, 0, 0));

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(formatPrice(amount));
        value.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-font-bold", "bookingpage-text-primary");

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Creates a large total row (extra large, for prominent display).
     *
     * @param labelI18nKey the i18n key for the label
     * @param amount price in cents
     * @return HBox containing the large total row
     */
    public HBox createLargeTotalRow(Object labelI18nKey, int amount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, 0, 0));

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(formatPrice(amount));
        value.getStyleClass().addAll("bookingpage-price-large", "bookingpage-text-primary");

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Creates a complete payment summary card with total, paid, and balance.
     *
     * @param total total amount in cents
     * @param paid amount already paid in cents
     * @param balance remaining balance in cents
     * @return VBox containing the payment summary card
     */
    public VBox createPaymentSummaryCard(int total, int paid, int balance) {
        VBox card = new VBox(0);
        card.setPadding(new Insets(32));
        card.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded-lg");
        card.setEffect(BookingPageUIBuilder.SHADOW_CARD);

        // Title
        Label title = I18nControls.newLabel(BookingPageI18nKeys.PaymentSummary);
        title.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-dark");
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // Total cost row
        HBox totalCostRow = createPriceRow(BookingPageI18nKeys.TotalCost, total);
        totalCostRow.getStyleClass().add("bookingpage-divider-thin-bottom");
        totalCostRow.setPadding(new Insets(12, 0, 12, 0));

        // Already paid row (green if > 0, muted if 0)
        HBox paidRow = paid > 0
                ? createSuccessPriceRow(BookingPageI18nKeys.AlreadyPaid, paid)
                : createMutedPriceRow(BookingPageI18nKeys.AlreadyPaid, paid);
        paidRow.getStyleClass().add("bookingpage-divider-thin-bottom");
        paidRow.setPadding(new Insets(12, 0, 12, 0));

        // Balance due row (emphasized)
        HBox balanceRow = createTotalRow(BookingPageI18nKeys.BalanceDue, balance);
        balanceRow.getStyleClass().add("bookingpage-divider-top-primary");
        VBox.setMargin(balanceRow, new Insets(16, 0, 0, 0));

        card.getChildren().addAll(title, totalCostRow, paidRow, balanceRow);
        return card;
    }
}
