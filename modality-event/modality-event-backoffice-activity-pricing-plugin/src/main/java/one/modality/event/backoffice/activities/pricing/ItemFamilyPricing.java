package one.modality.event.backoffice.activities.pricing;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
interface ItemFamilyPricing {

    boolean hasChanges();

    Node getHeaderNode();

    Node getContentNode();

}
