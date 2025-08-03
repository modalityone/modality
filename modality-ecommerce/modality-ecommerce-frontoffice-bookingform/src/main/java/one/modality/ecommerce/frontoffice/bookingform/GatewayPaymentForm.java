package one.modality.ecommerce.frontoffice.bookingform;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface GatewayPaymentForm {

    String getGatewayName();

    Node getView();

}
