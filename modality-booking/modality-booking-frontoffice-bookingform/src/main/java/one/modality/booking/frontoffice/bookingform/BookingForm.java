package one.modality.booking.frontoffice.bookingform;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingForm {

    BookingFormSettings getSettings();

    Node buildUi();

    default String getEventFieldsToLoad() {
        return null;
    }

    void onWorkingBookingLoaded();

    default ObservableBooleanValue transitingProperty() {
        return new SimpleBooleanProperty(false);
    }

    void setActivityCallback(BookingFormActivityCallback activityCallback);

    BookingFormActivityCallback getActivityCallback();

    /**
     * Called when a guest has submitted their information.
     * Multi-page forms can override this to navigate to the review page.
     * Default implementation submits the booking directly.
     */
    default void onGuestSubmitted() {
        BookingFormActivityCallback callback = getActivityCallback();
        if (callback != null) {
            callback.submitBooking(0); // Default behavior: submit directly
        }
    }

}
