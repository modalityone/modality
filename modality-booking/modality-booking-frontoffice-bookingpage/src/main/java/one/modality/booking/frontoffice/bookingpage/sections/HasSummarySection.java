package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

/**
 * Interface for the "Summary" section of a booking form.
 * This section displays a review of the booking before submission,
 * including attendee info, event details, and price breakdown.
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasSummarySection extends BookingFormSection {

    /**
     * Represents a line item in the price breakdown.
     * Supports flexible display of ItemFamily + Item names.
     */
    class PriceLine {
        private final String familyName;
        private final String itemName;
        private final String dates;
        private final int amount;

        /**
         * Creates a price line with separate family and item names.
         *
         * @param familyName ItemFamily name (can be null)
         * @param itemName Item name
         * @param dates date range string (can be null)
         * @param amount price in cents
         */
        public PriceLine(String familyName, String itemName, String dates, int amount) {
            this.familyName = familyName;
            this.itemName = itemName;
            this.dates = dates;
            this.amount = amount;
        }

        /**
         * Creates a price line with a combined name (backward compatibility).
         *
         * @param name combined display name (treated as itemName, familyName is null)
         * @param description date range or description
         * @param amount price in cents
         * @deprecated Use {@link #PriceLine(String, String, String, int)} with separate family/item names
         */
        @Deprecated
        public PriceLine(String name, String description, int amount) {
            this(null, name, description, amount);
        }

        public String getFamilyName() { return familyName; }
        public String getItemName() { return itemName; }
        public String getDates() { return dates; }
        public int getAmount() { return amount; }

        /**
         * @deprecated Use {@link #getItemName()} instead
         */
        @Deprecated
        public String getName() { return itemName; }

        /**
         * @deprecated Use {@link #getDates()} instead
         */
        @Deprecated
        public String getDescription() { return dates; }
    }

    /**
     * Types of additional options.
     */
    enum AdditionalOptionType {
        AUDIO_RECORDING,
        MEAL,
        PARKING,
        OTHER
    }

    /**
     * Represents an additional option selected for the booking.
     */
    class AdditionalOption {
        private final AdditionalOptionType type;
        private final String name;
        private final String description;

        public AdditionalOption(AdditionalOptionType type, String name, String description) {
            this.type = type;
            this.name = name;
            this.description = description;
        }

        public AdditionalOptionType getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the attendee name to display.
     */
    void setAttendeeName(String name);

    /**
     * Sets the attendee email to display.
     */
    void setAttendeeEmail(String email);

    /**
     * Sets the event name to display.
     */
    void setEventName(String name);

    /**
     * Sets the event date range to display.
     */
    void setEventDates(LocalDate start, LocalDate end);

    /**
     * Sets the rate type label (e.g., "Member", "Standard").
     */
    void setRateType(String rateType);

    /**
     * Adds a price line item to the breakdown with separate family and item names.
     *
     * @param familyName ItemFamily name (can be null)
     * @param itemName Item name
     * @param dates date range string (can be null)
     * @param amount price in cents
     */
    default void addPriceLine(String familyName, String itemName, String dates, int amount) {
        // Default implementation falls back to the simple method for backward compatibility
        // Implementations should override this to support family/item separation
        String displayName = familyName != null ? familyName + " - " + itemName : itemName;
        addPriceLine(displayName, dates, amount);
    }

    /**
     * Adds a price line item to the breakdown.
     *
     * @param name combined display name
     * @param description date range or description
     * @param amount price in cents
     * @deprecated Use {@link #addPriceLine(String, String, String, int)} with separate family/item names
     */
    @Deprecated
    void addPriceLine(String name, String description, int amount);

    /**
     * Clears all price lines.
     */
    void clearPriceLines();

    /**
     * Adds a generic additional option.
     */
    void addAdditionalOption(AdditionalOptionType type, String name, String description);

    /**
     * Clears all additional options.
     */
    void clearAdditionalOptions();

    /**
     * Refreshes the price breakdown display.
     */
    void refreshPriceBreakdown();
}
