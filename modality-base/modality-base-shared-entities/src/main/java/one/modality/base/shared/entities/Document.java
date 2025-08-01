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
    String ref = "ref";
    String person_lang = "person_lang";
    String cart = "cart";
    String priceNet = "price_net";
    String priceDeposit = "price_deposit";
    String priceMinDeposit = "price_minDeposit";
    String confirmed = "confirmed";
    String arrived = "arrived";
    String read = "read";
    String willPay = "willPay";
    String passReady = "passReady";
    String flagged = "flagged";
    String personFacilityFee = "person_facilityFee";
    String request = "request";

    default void setRef(Integer value) {
        setFieldValue(ref, value);
    }

    default Integer getRef() {
        return getIntegerFieldValue(ref);
    }

    default void setPersonLang(String value) {
        setFieldValue(person_lang, value);
    }

    default String getPersonLang() {
        return getStringFieldValue(person_lang);
    }

    default void setCart(Object value) {
        setForeignField(cart, value);
    }

    default EntityId getCartId() {
        return getForeignEntityId(cart);
    }

    default Cart getCart() {
        return getForeignEntity(cart);
    }

    default void setPriceNet(Integer value) {
        setFieldValue(priceNet, value);
    }

    default Integer getPriceNet() {
        return getIntegerFieldValue(priceNet);
    }

    default void setPriceDeposit(Integer value) {
        setFieldValue(priceDeposit, value);
    }

    default Integer getPriceDeposit() {
        return getIntegerFieldValue(priceDeposit);
    }

    default void setPriceMinDeposit(Integer value) {
        setFieldValue(priceMinDeposit, value);
    }

    default Integer getPriceMinDeposit() {
        return getIntegerFieldValue(priceMinDeposit);
    }

    default void setConfirmed(Boolean value) {
        setFieldValue(confirmed, value);
    }

    default Boolean isConfirmed() {
        return getBooleanFieldValue(confirmed);
    }

    default void setArrived(Boolean value) {
        setFieldValue(arrived, value);
    }

    default Boolean isArrived() {
        return getBooleanFieldValue(arrived);
    }

    default void setRead(Boolean value) {
        setFieldValue(read, value);
    }

    default Boolean isRead() {
        return getBooleanFieldValue(read);
    }

    default void setWillPay(Boolean value) {
        setFieldValue(willPay, value);
    }

    default Boolean isWillPay() {
        return getBooleanFieldValue(willPay);
    }

    default void setPassReady(Boolean value) {
        setFieldValue(passReady, value);
    }

    default Boolean isPassReady() {
        return getBooleanFieldValue(passReady);
    }

    default void setFlagged(Boolean value) {
        setFieldValue(flagged, value);
    }

    default Boolean isFlagged() {
        return getBooleanFieldValue(flagged);
    }

    default void setPersonFacilityFee(Boolean value) {
        setFieldValue(personFacilityFee, value);
    }

    default Boolean isPersonFacilityFee() {
        return getBooleanFieldValue(personFacilityFee);
    }

    default void setRequest(String value) {
        setFieldValue(request, value);
    }

    default String getRequest() {
        return getStringFieldValue(request);
    }

}