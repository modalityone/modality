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

    String amount = "amount";
    String method = "method";
    String parent = "parent";
    String spread = "spread";
    String date = "date";
    String pending = "pending";
    String successful = "successful";
    String fromMoneyAccount = "fromMoneyAccount";
    String toMoneyAccount = "toMoneyAccount";
    String transactionRef = "transactionRef";
    String status = "status";
    String gatewayResponse = "gatewayResponse";
    String comment = "comment";

    default void setAmount(Integer value) {
        setFieldValue(amount, value);
    }

    default Integer getAmount() {
        return getIntegerFieldValue(amount);
    }

    default void setMethod(Object value) {
        setForeignField(method, value);
    }

    default EntityId getMethodId() {
        return getForeignEntityId(method);
    }

    default Method getMethod() {
        return getForeignEntity(method);
    }

    default void setParent(Object value) {
        setForeignField(parent, value);
    }

    default EntityId getParentId() {
        return getForeignEntityId(parent);
    }

    default MoneyTransfer getParent() {
        return getForeignEntity(parent);
    }

    default void setSpread(Boolean value) {
        setFieldValue(spread, value);
    }

    default Boolean isSpread() {
        return getBooleanFieldValue(spread);
    }

    default void setDate(LocalDateTime value) {
        setFieldValue(date, value);
    }

    default LocalDateTime getDate() {
        return getLocalDateTimeFieldValue(date);
    }

    default void setPending(Boolean value) {
        setFieldValue(pending, value);
    }

    default Boolean isPending() {
        return getBooleanFieldValue(pending);
    }

    default void setSuccessful(Boolean value) {
        setFieldValue(successful, value);
    }

    default Boolean isSuccessful() {
        return getBooleanFieldValue(successful);
    }

    default void setFromMoneyAccount(Object value) {
        setForeignField(fromMoneyAccount, value);
    }

    default EntityId getFromMoneyAccountId() {
        return getForeignEntityId(fromMoneyAccount);
    }

    default MoneyAccount getFromMoneyAccount() {
        return getForeignEntity(fromMoneyAccount);
    }

    default void setToMoneyAccount(Object value) {
        setForeignField(toMoneyAccount, value);
    }

    default EntityId getToMoneyAccountId() {
        return getForeignEntityId(toMoneyAccount);
    }

    default MoneyAccount getToMoneyAccount() {
        return getForeignEntity(toMoneyAccount);
    }

    default void setTransactionRef(String value) {
        setFieldValue(transactionRef, value);
    }

    default String getTransactionRef() {
        return getStringFieldValue(transactionRef);
    }

    default void setStatus(String value) {
        setFieldValue(status, value);
    }

    default String getStatus() {
        return getStringFieldValue(status);
    }

    default void setGatewayResponse(String value) {
        setFieldValue(gatewayResponse, value);
    }

    default String getGatewayResponse() {
        return getStringFieldValue(gatewayResponse);
    }

    default void setComment(String value) {
        setFieldValue(comment, value);
    }

    default String getComment() {
        return getStringFieldValue(comment);
    }
}