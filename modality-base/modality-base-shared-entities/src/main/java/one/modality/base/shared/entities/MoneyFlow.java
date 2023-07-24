package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

public interface MoneyFlow extends EntityHasIcon, EntityHasOrganization {

    default void setFromMoneyAccount(Object fromMoneyAccount) {
        setForeignField("fromMoneyAccount", fromMoneyAccount);
    }

    default EntityId getFromMoneyAccountId() {
        return getForeignEntityId("fromMoneyAccount");
    }

    default MoneyAccount getFromMoneyAccount() {
        return getForeignEntity("fromMoneyAccount");
    }

    default void setToMoneyAccount(Object toMoneyAccount) {
        setForeignField("toMoneyAccount", toMoneyAccount);
    }

    default EntityId getToMoneyAccountId() {
        return getForeignEntityId("toMoneyAccount");
    }

    default MoneyAccount getToMoneyAccount() {
        return getForeignEntity("toMoneyAccount");
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

    default void setNegativeAccount(Boolean negativeAmount) {
        setFieldValue("negativeAmount", negativeAmount);
    }

    default Boolean isNegativeAmount() {
        return getBooleanFieldValue("negativeAmount");
    }

    default void setPostiveAccount(Boolean positiveAmount) {
        setFieldValue("positiveAmount", positiveAmount);
    }

    default Boolean isPositiveAmount() {
        return getBooleanFieldValue("positiveAmount");
    }
}
