package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public final class EventBookingFormSettingsBuilder {

    private EventHeader eventHeader;
    private Background headerBackground;
    private double headerMaxTopBottomPadding = -1;
    private double extraSpaceBetweenHeaderAndBookingForm; // if between 0 and 1, then it's a percent factor of the booking form width
    private boolean showNavigationBar;
    private boolean autoLoadExistingBooking;
    private boolean showPriceBar;
    private boolean bookAsAGuestAllowed;
    private boolean partialEventAllowed;

    public EventBookingFormSettingsBuilder setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
        return this;
    }

    public EventBookingFormSettingsBuilder setHeaderBackground(Background headerBackground) {
        this.headerBackground = headerBackground;
        return this;
    }

    public EventBookingFormSettingsBuilder setHeaderMaxTopBottomPadding(double headerMaxTopBottomPadding) {
        this.headerMaxTopBottomPadding = headerMaxTopBottomPadding;
        return this;
    }

    public EventBookingFormSettingsBuilder setExtraSpaceBetweenHeaderAndBookingForm(double extraSpaceBetweenHeaderAndBookingForm) {
        this.extraSpaceBetweenHeaderAndBookingForm = extraSpaceBetweenHeaderAndBookingForm;
        return this;
    }

    public EventBookingFormSettingsBuilder setBookAsAGuestAllowed(boolean bookAsAGuestAllowed) {
        this.bookAsAGuestAllowed = bookAsAGuestAllowed;
        return this;
    }

    public EventBookingFormSettingsBuilder setPartialEventAllowed(boolean partialEventAllowed) {
        this.partialEventAllowed = partialEventAllowed;
        return this;
    }

    public EventBookingFormSettingsBuilder setShowNavigationBar(boolean showNavigationBar) {
        this.showNavigationBar = showNavigationBar;
        return this;
    }

    public EventBookingFormSettingsBuilder setAutoLoadExistingBooking(boolean autoLoadExistingBooking) {
        this.autoLoadExistingBooking = autoLoadExistingBooking;
        return this;
    }

    public EventBookingFormSettingsBuilder setShowPriceBar(boolean showPriceBar) {
        this.showPriceBar = showPriceBar;
        return this;
    }

    public EventBookingFormSettings build() {
        return new EventBookingFormSettings(eventHeader, headerBackground, headerMaxTopBottomPadding, extraSpaceBetweenHeaderAndBookingForm, showNavigationBar, autoLoadExistingBooking, showPriceBar, bookAsAGuestAllowed, partialEventAllowed);
    }
}
