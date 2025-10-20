package one.modality.booking.frontoffice.bookingform;

import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public interface BookingFormActivityCallback {

    void setPersonToBookRequired(boolean required);

    void showDefaultSubmitButton(boolean show);

    void disableSubmitButton(boolean disable);

    default void submitBooking(int paymentDeposit, Button... submitButtons) {
        submitBooking(paymentDeposit, null, submitButtons);
    }

    void submitBooking(int paymentDeposit, Consumer<GatewayPaymentForm> gatewayPaymentFormDisplayer, Button... submitButtons);

    Region getEmbeddedLoginNode();

    BooleanExpression readyToSubmitBookingProperty();

    void onEndReached();

}
