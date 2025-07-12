package one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages;

import dev.webfx.extras.panes.MonoPane;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public interface BookingFormPage {

    Object getTitleI18nKey();

    Node getView();

    default MonoPane getEmbeddedLoginContainer() {
        return null;
    }

    default boolean isShowingOwnSubmitButton() {
        return false;
    }

    void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties);

    default boolean isValid() {
        return validProperty().get();
    };

    default ObservableBooleanValue validProperty() {
        return new SimpleBooleanProperty(true);
    }

}
