package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.crm.frontoffice.order.OrderCardView;

import java.util.List;

/**
 * @author David Hello
 */
final class OrdersView {

    private final VBox container = new VBox(50);

    OrdersView(List<Document> orders, ObservableValue<Object> selectedOrderIdProperty) {
        List<OrderCardView> cards = Collections.map(orders, OrderCardView::new);
        container.getChildren().setAll(Collections.map(cards, OrderCardView::getView));
        container.setPadding(new Insets(0,30,0,30));
        FXProperties.runNowAndOnPropertyChange(orderId -> {
            if (orderId != null)
                cards.forEach(card -> card.autoScrollToExpandedDetailsIfOrderId(orderId));
        }, selectedOrderIdProperty);
    }

    public Node getView() {
        return container;
    }

}
