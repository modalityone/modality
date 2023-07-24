package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import java.time.LocalDateTime;
import one.modality.base.shared.entities.markers.EntityHasDocument;
import one.modality.base.shared.entities.markers.EntityHasIcon;

/**
 * @author Bruno Salmon
 */
public interface MoneyTransfer extends EntityHasDocument, EntityHasIcon {

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

  default Method getParent() {
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
}
