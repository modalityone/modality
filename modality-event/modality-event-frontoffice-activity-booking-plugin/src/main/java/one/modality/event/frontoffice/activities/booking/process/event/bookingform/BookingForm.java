package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingForm {

    BookingFormSettings getSettings();

    Node buildUi();

    void onWorkingBookingLoaded();

    default ObservableBooleanValue transitingProperty() {
        return new SimpleBooleanProperty(false);
    }

    void setActivityCallback(BookingFormActivityCallback activityCallback);

    BookingFormActivityCallback getActivityCallback();

}
