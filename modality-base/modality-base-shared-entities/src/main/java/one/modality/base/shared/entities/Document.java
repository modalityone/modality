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

    default void setArrived(Boolean arrived) {
        setFieldValue("arrived", arrived);
    }

    default Boolean isArrived() {
        return getBooleanFieldValue("arrived");
    }

    default void setRead(Boolean read) {
        setFieldValue("read", read);
    }

    default Boolean isRead() {
        return getBooleanFieldValue("read");
    }

    default void setWillPay(Boolean willPay) {
        setFieldValue("willPay", willPay);
    }

    default Boolean isWillPay() {
        return getBooleanFieldValue("willPay");
    }

    default void setPassReady(Boolean passReady) {
        setFieldValue("passReady", passReady);
    }

    default Boolean isPassReady() {
        return getBooleanFieldValue("passReady");
    }

    default void setFlagged(Boolean flagged) {
        setFieldValue("flagged", flagged);
    }

    default Boolean isFlagged() {
        return getBooleanFieldValue("flagged");
    }

    default void setPersonFacilityFee(Boolean personFacilityFee) {
        setFieldValue("person_facilityFee", personFacilityFee);
    }

    default Boolean isPersonFacilityFee() {
        return getBooleanFieldValue("person_facilityFee");
    }

}
