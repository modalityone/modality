package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

public interface MoneyFlow extends
    EntityHasIcon,
    EntityHasOrganization {

    String fromMoneyAccount = "fromMoneyAccount";
    String toMoneyAccount = "toMoneyAccount";
    String method = "method";
    String negativeAmount = "negativeAmount";
    String positiveAmount = "positiveAmount";

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

    default void setMethod(Object value) {
        setForeignField(method, value);
    }

    default EntityId getMethodId() {
        return getForeignEntityId(method);
    }

    default Method getMethod() {
        return getForeignEntity(method);
    }

    default void setNegativeAccount(Boolean value) {
        setFieldValue(negativeAmount, value);
    }

    default Boolean isNegativeAmount() {
        return getBooleanFieldValue(negativeAmount);
    }

    default void setPostiveAccount(Boolean value) {
        setFieldValue(positiveAmount, value);
    }

    default Boolean isPositiveAmount() {
        return getBooleanFieldValue(positiveAmount);
    }
}