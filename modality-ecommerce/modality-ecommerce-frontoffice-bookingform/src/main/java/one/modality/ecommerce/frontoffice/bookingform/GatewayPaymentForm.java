package one.modality.ecommerce.frontoffice.bookingform;

import dev.webfx.platform.async.AsyncResult;
import javafx.scene.Node;
import one.modality.ecommerce.payment.CancelPaymentResult;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public interface GatewayPaymentForm {

    String getGatewayName();

    void setCancelPaymentResultHandler(Consumer<AsyncResult<CancelPaymentResult>> cancelPaymentHandler);

    Node getView();

}
