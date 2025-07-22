package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasDocument;
import one.modality.base.shared.entities.markers.EntityHasUserPerson;

/**
 * @author Bruno Salmon
 */
public interface History extends Entity, EntityHasDocument, EntityHasUserPerson {
    String username = "username";
    String comment = "comment";
    String changes = "changes";
    String mail = "mail";
    String moneyTransfer = "moneyTransfer";
    String request = "request";

    default void setUsername(String value) {
        setFieldValue(username, value);
    }

    default String getUsername() {
        return getStringFieldValue(username);
    }

    default void setComment(String value) {
        setFieldValue(comment, value);
    }

    default String getComment() {
        return getStringFieldValue(comment);
    }

    default void setChanges(String value) {
        setFieldValue(changes, value);
    }

    default String getRequest() {
        return getStringFieldValue(request);
    }

    default void setRequest(String value) {
        setFieldValue(request, value);
    }

    default String getChanges() {
        return getStringFieldValue(changes);
    }

    default void setMail(Object value) {
        setForeignField(mail, value);
    }

    default EntityId getMailId() {
        return getForeignEntityId(mail);
    }

    default Mail getMail() {
        return getForeignEntity(mail);
    }

    default void setMoneyTransfer(Object value) {
        setForeignField(moneyTransfer, value);
    }

    default EntityId getMoneyTransferId() {
        return getForeignEntityId(moneyTransfer);
    }

    default Document getMoneyTransfer() {
        return getForeignEntity(moneyTransfer);
    }
}