package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.beans.value.ObservableBooleanValue;

/**
 * @author Bruno Salmon
 */
public interface BookingFormActivityCallback {

    void setPersonToBookRequired(boolean required);

    void showDefaultSubmitButton(boolean show);

    void disableSubmitButton(boolean disable);

    void submitBooking(int paymentDeposit);

    ObservableBooleanValue readyToSubmitBookingProperty();

}
