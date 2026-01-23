package one.modality.booking.frontoffice.bookingpage.sections.summary;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

/**
 * Interface for the "Existing Booking Summary" section of a booking modification form.
 * This section displays information about an existing booking that the user wants to modify.
 *
 * <p>Displays:</p>
 * <ul>
 *   <li>Event name with status badge (upcoming/in_progress/completed)</li>
 *   <li>Dates (arrival → departure, X nights)</li>
 *   <li>Package/programme name</li>
 *   <li>Booking reference number</li>
 *   <li>Attendee name</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasExistingBookingSummarySection extends BookingFormSection {

    /**
     * Booking status values for the status badge.
     */
    enum BookingStatus {
        /** Event has not started yet */
        UPCOMING,
        /** Event is currently running */
        IN_PROGRESS,
        /** Event has ended */
        COMPLETED
    }

    /**
     * Returns the color scheme property for theming.
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     */
    @Deprecated
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the event name to display.
     */
    void setEventName(String name);

    /**
     * Sets the booking status (for the status badge).
     */
    void setBookingStatus(BookingStatus status);

    /**
     * Sets the arrival date.
     */
    void setArrivalDate(LocalDate arrivalDate);

    /**
     * Sets the departure date.
     */
    void setDepartureDate(LocalDate departureDate);

    /**
     * Sets the package/programme name.
     */
    void setPackageName(String packageName);

    /**
     * Sets the booking reference number.
     */
    void setBookingReference(String reference);

    /**
     * Returns the booking reference.
     */
    String getBookingReference();

    /**
     * Resets the section to its initial state.
     */
    void reset();
}
