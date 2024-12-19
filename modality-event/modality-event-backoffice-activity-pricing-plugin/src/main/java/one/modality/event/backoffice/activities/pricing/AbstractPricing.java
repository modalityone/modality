package one.modality.event.backoffice.activities.pricing;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
interface AbstractPricing {

    Node getHeaderNode();

    Node getContentNode();

    void onChangesCancelled();

}
