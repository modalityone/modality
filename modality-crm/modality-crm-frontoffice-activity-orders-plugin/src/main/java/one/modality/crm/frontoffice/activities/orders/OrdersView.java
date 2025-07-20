package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.platform.util.collection.Collections;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.crm.frontoffice.order.OrderView;

import java.util.List;

/**
 * @author David Hello
 */
final class OrdersView {

    private final Event event;
    private final VBox container = new VBox(50);

    OrdersView(Event event, List<Document> orders, ObservableValue<Object> selectedOrderIdProperty) {
        this.event = event;
        container.setPadding(new Insets(0,30,0,30));
        container.getChildren().setAll(
            Collections.map(orders, order -> new OrderView(order, selectedOrderIdProperty).getView())
        );
    }

    public Event getEvent() {
        return event;
    }

    public Node getView() {
        return container;
    }

}
