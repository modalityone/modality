package one.modality.event.frontoffice.activities.book.event;

import javafx.scene.layout.Background;
import one.modality.base.shared.entities.Event;
import one.modality.booking.frontoffice.bookingform.BookingFormSettings;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
@SuppressWarnings("unusable-by-js")
public record EventBookingFormSettings(
        Event event,
        EventHeader eventHeader,
        Background headerBackground,
        double headerMaxTopBottomPadding,
        double extraSpaceBetweenHeaderAndBookingForm, // if between 0 and 1, then it's a percent factor of the booking
                                                      // form width
        boolean showNavigationBar,
        boolean autoLoadExistingBooking,
        boolean showPriceBar,
        boolean bookAsAGuestAllowed,
        boolean partialEventAllowed,
        boolean isNavigationClickable) implements BookingFormSettings {
}
