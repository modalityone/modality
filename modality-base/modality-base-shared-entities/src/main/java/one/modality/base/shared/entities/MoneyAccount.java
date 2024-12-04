package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

public interface MoneyAccount extends
    EntityHasOrganization,
    EntityHasEvent,
    EntityHasIcon,
    EntityHasName {

    String currency = "currency";
    String type = "type";
    String closed = "closed";
    String gatewayCompany = "gatewayCompany";

    default void setCurrency(Object value) {
        setForeignField(currency, value);
    }

    default EntityId getCurrencyId() {
        return getForeignEntityId(currency);
    }

    default Currency getCurrency() {
        return getForeignEntity(currency);
    }

    default void setType(Object value) {
        setForeignField(type, value);
    }

    default EntityId getTypeId() {
        return getForeignEntityId(type);
    }

    default MoneyAccountType getType() {
        return getForeignEntity(type);
    }

    default void setClosed(Boolean value) {
        setFieldValue(closed, value);
    }

    default Boolean isClosed() {
        return getBooleanFieldValue(closed);
    }

    default void setGatewayCompany(Object value) {
        setForeignField(gatewayCompany, value);
    }

    default EntityId getGatewayCompanyId() {
        return getForeignEntityId(gatewayCompany);
    }

    default GatewayCompany getGatewayCompany() {
        return getForeignEntity(gatewayCompany);
    }
}