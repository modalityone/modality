package one.modality.booking.frontoffice.bookingpage;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public interface BookingFormSection {

    Object getTitleI18nKey();

    Node getView();

    default void onTransitionFinished() {
    }

    default boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return true;
    }

    void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties);

    default boolean isValid() {
        return validProperty().get();
    };

    default ObservableBooleanValue validProperty() {
        return new SimpleBooleanProperty(true);
    }
}
