package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingForm {

    BookingFormSettings getSettings();

    Node buildUi();

    void onWorkingBookingLoaded();

    ObservableBooleanValue showLoginProperty();

    ObservableBooleanValue showDefaultSubmitButtonProperty();

    ObservableBooleanValue disableSubmitButtonProperty();

    void setActivityCallback(BookingFormActivityCallback activityCallback);

    BookingFormActivityCallback getActivityCallback();

}
