package one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages;

import javafx.scene.Node;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;

/**
 * @author Bruno Salmon
 */
public interface BookingFormPage {

    Object getTitleI18nKey();

    Node getView();

    void setWorkingBooking(WorkingBooking workingBooking);

    boolean isValid();

}
