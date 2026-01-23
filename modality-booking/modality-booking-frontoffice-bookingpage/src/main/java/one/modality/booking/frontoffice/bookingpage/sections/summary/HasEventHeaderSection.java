package one.modality.booking.frontoffice.bookingpage.sections.summary;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Interface for the event header section of a booking form.
 * This section displays the event name, dates, location, description, and optional cover image.
 *
 * <p>The event header is typically displayed at the top of the booking form options page
 * to provide context about the event being booked.</p>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 * @see DefaultEventHeaderSection
 */
public interface HasEventHeaderSection extends BookingFormSection {

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Returns the current color scheme.
     */
    BookingFormColorScheme getColorScheme();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

}
