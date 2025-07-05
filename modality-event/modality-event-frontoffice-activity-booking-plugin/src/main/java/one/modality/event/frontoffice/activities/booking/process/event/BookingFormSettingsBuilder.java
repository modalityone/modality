package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public final class BookingFormSettingsBuilder {

    private EventHeader eventHeader;
    private Background headerBackground;
    private boolean bookAsAGuestAllowed;
    private boolean partialEventAllowed;

    public BookingFormSettingsBuilder setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
        return this;
    }

    public BookingFormSettingsBuilder setHeaderBackground(Background headerBackground) {
        this.headerBackground = headerBackground;
        return this;
    }

    public BookingFormSettingsBuilder setBookAsAGuestAllowed(boolean bookAsAGuestAllowed) {
        this.bookAsAGuestAllowed = bookAsAGuestAllowed;
        return this;
    }

    public BookingFormSettingsBuilder setPartialEventAllowed(boolean partialEventAllowed) {
        this.partialEventAllowed = partialEventAllowed;
        return this;
    }

    public BookingFormSettings build() {
        return new BookingFormSettings(eventHeader, headerBackground, bookAsAGuestAllowed, partialEventAllowed);
    }
}
