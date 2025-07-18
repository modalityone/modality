package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.stack.orm.entity.EntityStore;
import javafx.scene.Node;
import one.modality.base.shared.entities.Document;

/**
 * @author David Hello
 */
final class OrderView {

    private final BookingSummaryView bookingSummaryView;

    OrderView(Document booking, OrdersActivity ordersActivity) {
        EntityStore entityStore = EntityStore.create(ordersActivity.getDataSourceModel());
        bookingSummaryView = new BookingSummaryView(booking, entityStore,ordersActivity);
    }

    public Node getView() {
        return bookingSummaryView.getView();
    }

    public Document getBooking() {
        return bookingSummaryView.getBooking();
    }
}