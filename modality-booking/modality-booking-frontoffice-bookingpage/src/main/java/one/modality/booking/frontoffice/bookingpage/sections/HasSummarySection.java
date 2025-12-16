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
     */
    class PriceLine {
        private final String name;
        private final String description;
        private final int amount;

        public PriceLine(String name, String description, int amount) {
            this.name = name;
            this.description = description;
            this.amount = amount;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getAmount() { return amount; }
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
     * Adds a price line item to the breakdown.
     */
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
