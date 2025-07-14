package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public final class BookingFormSettings {

    private final EventHeader eventHeader;
    private final Background headerBackground;
    private final double headerMaxTopBottomPadding;
    private final boolean showNavigationBar;
    private final boolean showPriceBar;
    private final boolean bookAsAGuestAllowed;
    private final boolean partialEventAllowed;

    public BookingFormSettings(EventHeader eventHeader, Background headerBackground, double headerMaxTopBottomPadding, boolean showNavigationBar, boolean showPriceBar, boolean bookAsAGuestAllowed, boolean partialEventAllowed) {
        this.eventHeader = eventHeader;
        this.headerBackground = headerBackground;
        this.headerMaxTopBottomPadding = headerMaxTopBottomPadding;
        this.showNavigationBar = showNavigationBar;
        this.showPriceBar = showPriceBar;
        this.bookAsAGuestAllowed = bookAsAGuestAllowed;
        this.partialEventAllowed = partialEventAllowed;
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }

    public Background getHeaderBackground() {
        return headerBackground;
    }

    public double getHeaderMaxTopBottomPadding() {
        return headerMaxTopBottomPadding;
    }

    public boolean showNavigationBar() {
        return showNavigationBar;
    }

    public boolean showPriceBar() {
        return showPriceBar;
    }

    public boolean isBookAsAGuestAllowed() {
        return bookAsAGuestAllowed;
    }

    public boolean isPartialEventAllowed() {
        return partialEventAllowed;
    }
}
