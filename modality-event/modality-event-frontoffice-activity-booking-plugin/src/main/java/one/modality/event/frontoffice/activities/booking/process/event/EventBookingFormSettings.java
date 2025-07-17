package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.layout.Background;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormSettings;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public record EventBookingFormSettings(
    EventHeader eventHeader,
    Background headerBackground,
    double headerMaxTopBottomPadding,
    double extraSpaceBetweenHeaderAndBookingForm, // if between 0 and 1, then it's a percent factor of the booking form width
    boolean showNavigationBar,
    boolean autoLoadExistingBooking,
    boolean showPriceBar,
    boolean bookAsAGuestAllowed,
    boolean partialEventAllowed
) implements BookingFormSettings {}
