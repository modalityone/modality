package one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;

/**
 * @author Bruno Salmon
 */
public interface BookingFormPage {

    Object getTitleI18nKey();

    Node getView();

    void setWorkingBooking(WorkingBooking workingBooking);

    default boolean isValid() {
        return validProperty().get();
    };

    default ObservableBooleanValue validProperty() {
        return new SimpleBooleanProperty(true);
    }

}
