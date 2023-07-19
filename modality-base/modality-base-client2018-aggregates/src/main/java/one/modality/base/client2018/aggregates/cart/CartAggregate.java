package one.modality.base.client2018.aggregates.cart;

import one.modality.base.shared.entities.Cart;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.base.client2018.aggregates.event.EventAggregate;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.platform.async.Future;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public interface CartAggregate {

    static CartAggregate get(Object cartIdOrUuid) {
        return CartAggregateImpl.get(cartIdOrUuid);
    }

    static CartAggregate getOrCreate(Object cartIdOrUuid, DataSourceModel dataSourceModel) {
        return CartAggregateImpl.getOrCreate(cartIdOrUuid, dataSourceModel);
    }

    static CartAggregate getOrCreateFromCart(Cart cart) {
        return CartAggregateImpl.getOrCreateFromCart(cart);
    }

    static CartAggregate getOrCreateFromDocument(Document document) {
        return CartAggregateImpl.getOrCreateFromDocument(document);
    }

    Future<Cart> onCart();

    Cart getCart();

    Future<List<Document>> onCartDocuments();

    List<Document> getCartDocuments();

    Future<EntityList> onCartPayments();

    EntityList<MoneyTransfer> getCartPayments();

    void unload();

    boolean isLoading();

    boolean isLoaded();

    EventAggregate getEventAggregate();

}
