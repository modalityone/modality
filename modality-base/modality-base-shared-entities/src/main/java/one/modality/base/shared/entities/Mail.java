package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasDocument;
import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface Mail extends Entity, EntityHasDocument, EntityHasOrganization {
    String fromName = "fromName";
    String fromEmail = "fromEmail";
    String subject = "subject";
    String content = "content";
    String out = "out";
    String magicLink = "magicLink";

    default void setFromName(String value) {
        setFieldValue(fromName, value);
    }

    default String getFromName() {
        return getStringFieldValue(fromName);
    }

    default void setFromEmail(String value) {
        setFieldValue(fromEmail, value);
    }

    default String getFromEmail() {
        return getStringFieldValue(fromEmail);
    }

    default void setSubject(String value) {
        setFieldValue(subject, value);
    }

    default String getSubject() {
        return getStringFieldValue(subject);
    }

    default void setContent(String value) {
        setFieldValue(content, value);
    }

    default String getContent() {
        return getStringFieldValue(content);
    }

    default void setOut(Boolean value) {
        setFieldValue(out, value);
    }

    default Boolean isOut() {
        return getBooleanFieldValue(out);
    }

    default void setMagicLink(Object value) {
        setForeignField(magicLink, value);
    }

    default EntityId getMagicLinkId() {
        return getForeignEntityId(magicLink);
    }

    default Organization getMagicLink() {
        return getForeignEntity(magicLink);
    }
}