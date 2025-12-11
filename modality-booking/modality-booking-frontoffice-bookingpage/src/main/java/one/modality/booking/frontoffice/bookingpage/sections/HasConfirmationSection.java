package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

/**
 * Interface for the "Confirmation" section of a booking form.
 * This section displays payment confirmation, booking references,
 * and next steps after successful payment.
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasConfirmationSection extends BookingFormSection {

    /**
     * Represents a confirmed booking with its reference number.
     */
    class ConfirmedBooking {
        private final String name;
        private final String email;
        private final String reference;

        public ConfirmedBooking(String name, String email, String reference) {
            this.name = name;
            this.email = email;
            this.reference = reference;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getReference() { return reference; }
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
     * Sets the currency symbol for price display.
     */
    void setCurrencySymbol(String symbol);

    /**
     * Sets the event name to display.
     */
    void setEventName(String name);

    /**
     * Sets the event date range.
     */
    void setEventDates(LocalDate start, LocalDate end);

    /**
     * Sets the payment amounts (total and paid).
     */
    void setPaymentAmounts(double total, double paid);

    /**
     * Adds a confirmed booking to display.
     */
    void addConfirmedBooking(ConfirmedBooking booking);

    /**
     * Clears all confirmed bookings.
     */
    void clearConfirmedBookings();

    /**
     * Sets the callback for when "Make Another Booking" is clicked.
     */
    void setOnMakeAnotherBooking(Runnable callback);
}
