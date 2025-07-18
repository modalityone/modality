package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;

import java.util.List;

/**
 * @author David Hello
 */
final class OrdersBookingsView {

    private Event event;

    private final VBox container = new VBox(50);

    OrdersBookingsView(Event event, List<Document> eventBookings, OrdersActivity ordersActivity) {
        setEvent(event);
        container.setPadding(new Insets(0,30,0,30));
        container.getChildren().addAll(
            Collections.map(eventBookings, b -> new OrderView(b,ordersActivity).getView())
        );
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public Node getView() {
        return container;
    }

}
