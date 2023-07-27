package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasCancelled;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPerson;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetailsCopy;

/**
 * @author Bruno Salmon
 */
public interface Document extends
        EntityHasEvent,
        EntityHasCancelled,
        EntityHasPerson,
        EntityHasPersonalDetailsCopy {

    default void setRef(Integer ref) {
        setFieldValue("ref", ref);
    }

    default Integer getRef() {
        return getIntegerFieldValue("ref");
    }

    default void setCart(Object cart) {
        setForeignField("cart", cart);
    }

    default EntityId getCartId() {
        return getForeignEntityId("cart");
    }

    default Cart getCart() {
        return getForeignEntity("cart");
    }

    default void setPriceNet(Integer priceNet) {
        setFieldValue("price_net", priceNet);
    }

    default Integer getPriceNet() {
        return getIntegerFieldValue("price_net");
    }

    default void setPriceDeposit(Integer priceDeposit) {
        setFieldValue("price_deposit", priceDeposit);
    }

    default Integer getPriceDeposit() {
        return getIntegerFieldValue("price_deposit");
    }

    default void setPriceMinDeposit(Integer priceMinDeposit) {
        setFieldValue("price_minDeposit", priceMinDeposit);
    }

    default Integer getPriceMinDeposit() {
        return getIntegerFieldValue("price_minDeposit");
    }

    default void setConfirmed(Boolean confirmed) {
        setFieldValue("confirmed", confirmed);
    }

    default Boolean isConfirmed() {
        return getBooleanFieldValue("confirmed");
    }

    default void setArrived(Boolean confirmed) {
        setFieldValue("arrived", confirmed);
    }

    default Boolean isArrived() {
        return getBooleanFieldValue("arrived");
    }

}
