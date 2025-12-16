package one.modality.booking.frontoffice.bookingpage;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingFormHeader {

    Node getView();

    void setBookingForm(MultiPageBookingForm bookingForm);

    void updateState();

    void setNavigationClickable(boolean clickable);
}
