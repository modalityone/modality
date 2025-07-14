package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.scene.layout.Background;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
public final class BookingFormSettingsBuilder {

    private EventHeader eventHeader;
    private Background headerBackground;
    private double headerMaxTopBottomPadding = -1;
    private double extraSpaceBetweenHeaderAndBookingForm; // if between 0 and 1, then it's a percent factor of the booking form width
    private boolean showNavigationBar;
    private boolean showPriceBar;
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

    public BookingFormSettingsBuilder setHeaderMaxTopBottomPadding(double headerMaxTopBottomPadding) {
        this.headerMaxTopBottomPadding = headerMaxTopBottomPadding;
        return this;
    }

    public BookingFormSettingsBuilder setExtraSpaceBetweenHeaderAndBookingForm(double extraSpaceBetweenHeaderAndBookingForm) {
        this.extraSpaceBetweenHeaderAndBookingForm = extraSpaceBetweenHeaderAndBookingForm;
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

    public BookingFormSettingsBuilder setShowNavigationBar(boolean showNavigationBar) {
        this.showNavigationBar = showNavigationBar;
        return this;
    }

    public BookingFormSettingsBuilder setShowPriceBar(boolean showPriceBar) {
        this.showPriceBar = showPriceBar;
        return this;
    }

    public BookingFormSettings build() {
        return new BookingFormSettings(eventHeader, headerBackground, headerMaxTopBottomPadding, extraSpaceBetweenHeaderAndBookingForm, showNavigationBar, showPriceBar, bookAsAGuestAllowed, partialEventAllowed);
    }
}
