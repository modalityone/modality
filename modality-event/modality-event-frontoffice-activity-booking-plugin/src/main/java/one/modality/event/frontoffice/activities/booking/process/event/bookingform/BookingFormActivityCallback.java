package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.beans.value.ObservableBooleanValue;

/**
 * @author Bruno Salmon
 */
public interface BookingFormActivityCallback {

    void submitBooking();

    ObservableBooleanValue readyToSubmitBookingProperty();

}
