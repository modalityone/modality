package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public final class BookingFormSettings {

    private final EventHeader eventHeader;
    private final Background headerBackground;
    private final boolean bookAsAGuestAllowed;
    private final boolean partialEventAllowed;

    public BookingFormSettings(EventHeader eventHeader, Background headerBackground, boolean bookAsAGuestAllowed, boolean partialEventAllowed) {
        this.eventHeader = eventHeader;
        this.headerBackground = headerBackground;
        this.bookAsAGuestAllowed = bookAsAGuestAllowed;
        this.partialEventAllowed = partialEventAllowed;
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }

    public Background getHeaderBackground() {
        return headerBackground;
    }

    public boolean isBookAsAGuestAllowed() {
        return bookAsAGuestAllowed;
    }

    public boolean isPartialEventAllowed() {
        return partialEventAllowed;
    }
}
