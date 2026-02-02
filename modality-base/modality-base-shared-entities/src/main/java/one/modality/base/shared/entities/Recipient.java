package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasPerson;

/**
 * Recipient entity representing email recipients linked to a Mail record.
 *
 * @author David Hello
 */
public interface Recipient extends Entity, EntityHasPerson {

    //// Domain fields

    String mail = "mail";
    String name = "name";
    String email = "email";
    String to = "to";
    String cc = "cc";
    String bcc = "bcc";
    String ok = "ok";
    String permanentError = "permanentError";
    String temporaryError = "temporaryError";

    //// Domain field accessors

    default void setMail(Object value) {
        setForeignField(mail, value);
    }

    default EntityId getMailId() {
        return getForeignEntityId(mail);
    }

    default Mail getMail() {
        return getForeignEntity(mail);
    }

    default void setName(String value) {
        setFieldValue(name, value);
    }

    default String getName() {
        return getStringFieldValue(name);
    }

    default void setEmail(String value) {
        setFieldValue(email, value);
    }

    default String getEmail() {
        return getStringFieldValue(email);
    }

    default void setTo(Boolean value) {
        setFieldValue(to, value);
    }

    default Boolean isTo() {
        return getBooleanFieldValue(to);
    }

    default void setCc(Boolean value) {
        setFieldValue(cc, value);
    }

    default Boolean isCc() {
        return getBooleanFieldValue(cc);
    }

    default void setBcc(Boolean value) {
        setFieldValue(bcc, value);
    }

    default Boolean isBcc() {
        return getBooleanFieldValue(bcc);
    }

    default void setOk(Boolean value) {
        setFieldValue(ok, value);
    }

    default Boolean isOk() {
        return getBooleanFieldValue(ok);
    }

    default void setPermanentError(Boolean value) {
        setFieldValue(permanentError, value);
    }

    default Boolean hasPermanentError() {
        return getBooleanFieldValue(permanentError);
    }

    default void setTemporaryError(Boolean value) {
        setFieldValue(temporaryError, value);
    }

    default Boolean hasTemporaryError() {
        return getBooleanFieldValue(temporaryError);
    }
}
