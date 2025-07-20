package one.modality.ecommerce.frontoffice.bookingform;

import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

/**
 * @author Bruno Salmon
 */
public interface BookingFormActivityCallback {

    void setPersonToBookRequired(boolean required);

    void showDefaultSubmitButton(boolean show);

    void disableSubmitButton(boolean disable);

    void submitBooking(int paymentDeposit, Button... bookingFormSubmitButtons);

    Region getEmbeddedLoginNode();

    BooleanExpression readyToSubmitBookingProperty();

}
