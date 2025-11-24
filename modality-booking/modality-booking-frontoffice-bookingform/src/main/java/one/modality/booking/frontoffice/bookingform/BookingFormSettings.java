package one.modality.booking.frontoffice.bookingform;

/**
 * @author Bruno Salmon
 */
public interface BookingFormSettings {

    boolean showNavigationBar();

    boolean autoLoadExistingBooking();

    default boolean isNavigationClickable() {
        return true;
    }

    default boolean showPriceBar() {
        return true;
    }
}
