package mongoose.base.shared.entities;

import dev.webfx.framework.shared.orm.entity.EntityId;
import mongoose.base.shared.entities.markers.EntityHasEvent;
import mongoose.base.shared.entities.markers.EntityHasIcon;
import mongoose.base.shared.entities.markers.EntityHasName;
import mongoose.base.shared.entities.markers.EntityHasOrganization;

public interface MoneyAccount extends
        EntityHasOrganization,
        EntityHasEvent,
        EntityHasIcon,
        EntityHasName {

    default void setCurrency(Object currency) {
        setForeignField("currency", currency);
    }

    default EntityId getCurrencyId() {
        return getForeignEntityId("currency");
    }

    default Currency getCurrency() {
        return getForeignEntity("currency");
    }

    default void setType(Object type) {
        setForeignField("type", type);
    }

    default EntityId getTypeId() {
        return getForeignEntityId("type");
    }

    default MoneyAccountType getType() {
        return getForeignEntity("type");
    }

    default void setClosed(Boolean closed) {
        setFieldValue("closed", closed);
    }

    default Boolean isClosed() {
        return getBooleanFieldValue("closed");
    }

}
