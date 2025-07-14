package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public record BookingFormSettings(
    EventHeader eventHeader,
    Background headerBackground,
    double headerMaxTopBottomPadding,
    boolean showNavigationBar,
    boolean showPriceBar,
    boolean bookAsAGuestAllowed,
    boolean partialEventAllowed
) {}
