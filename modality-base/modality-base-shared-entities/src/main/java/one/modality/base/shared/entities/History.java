package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.markers.EntityHasDocument;

/**
 * @author Bruno Salmon
 */
public interface History extends Entity, EntityHasDocument {

    default void setUsername(String username) {
        setFieldValue("username", username);
    }

    default String getUsername() {
        return getStringFieldValue("username");
    }

    default void setComment(String comment) {
        setFieldValue("comment", comment);
    }

    default String getComment() {
        return getStringFieldValue("comment");
    }

    default void setMail(Object mail) {
        setForeignField("mail", mail);
    }

    default EntityId getMailId() {
        return getForeignEntityId("mail");
    }

    default Mail getMail() {
        return getForeignEntity("mail");
    }
}
