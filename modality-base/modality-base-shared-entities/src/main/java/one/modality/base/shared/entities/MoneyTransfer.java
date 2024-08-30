package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasDocument;
import one.modality.base.shared.entities.markers.EntityHasIcon;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface MoneyTransfer extends
        EntityHasDocument,
        EntityHasIcon {

    default void setAmount(Integer amount) {
        setFieldValue("amount", amount);
    }

    default Integer getAmount() {
        return getIntegerFieldValue("amount");
    }

    default void setMethod(Object method) {
        setForeignField("method", method);
    }

    default EntityId getMethodId() {
        return getForeignEntityId("method");
    }

    default Method getMethod() {
        return getForeignEntity("method");
    }

    default void setParent(Object parent) {
        setForeignField("parent", parent);
    }

    default EntityId getParentId() {
        return getForeignEntityId("parent");
    }

    default MoneyTransfer getParent() {
        return getForeignEntity("parent");
    }

    default void setSpread(Boolean spread) {
        setFieldValue("spread", spread);
    }

    default Boolean isSpread() {
        return getBooleanFieldValue("spread");
    }

    default void setDate(LocalDateTime date) {
        setFieldValue("date", date);
    }

    default LocalDateTime getDate() {
        return getLocalDateTimeFieldValue("date");
    }

    default void setPending(Boolean pending) {
        setFieldValue("pending", pending);
    }

    default Boolean isPending() {
        return getBooleanFieldValue("pending");
    }

    default void setSuccessful(Boolean successful) {
        setFieldValue("successful", successful);
    }

    default Boolean isSuccessful() {
        return getBooleanFieldValue("successful");
    }

    default void setFromMoneyAccount(Object parent) {
        setForeignField("fromMoneyAccount", parent);
    }

    default EntityId getFromMoneyAccountId() {
        return getForeignEntityId("fromMoneyAccount");
    }

    default MoneyAccount getFromMoneyAccount() {
        return getForeignEntity("fromMoneyAccount");
    }

    default void setToMoneyAccount(Object parent) {
        setForeignField("toMoneyAccount", parent);
    }

    default EntityId getToMoneyAccountId() {
        return getForeignEntityId("toMoneyAccount");
    }

    default MoneyAccount getToMoneyAccount() {
        return getForeignEntity("toMoneyAccount");
    }

    default void setTransactionRef(String transactionRef) {
        setFieldValue("transactionRef", transactionRef);
    }

    default String getTransactionRef() {
        return getStringFieldValue("transactionRef");
    }

    default void setStatus(String status) {
        setFieldValue("status", status);
    }

    default String getStatus() {
        return getStringFieldValue("status");
    }

    default void setGatewayResponse(String gatewayResponse) {
        setFieldValue("gatewayResponse", gatewayResponse);
    }

    default String getGatewayResponse() {
        return getStringFieldValue("gatewayResponse");
    }

    default void setComment(String comment) {
        setFieldValue("comment", comment);
    }

    default String getComment() {
        return getStringFieldValue("comment");
    }

}
