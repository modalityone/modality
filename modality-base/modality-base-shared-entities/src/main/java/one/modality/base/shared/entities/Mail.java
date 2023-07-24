package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasDocument;

/**
 * @author Bruno Salmon
 */
public interface Mail extends Entity, EntityHasDocument {

  default void setFromName(String fromName) {
    setFieldValue("fromName", fromName);
  }

  default String getFromName() {
    return getStringFieldValue("fromName");
  }

  default void setFromEmail(String fromEmail) {
    setFieldValue("fromEmail", fromEmail);
  }

  default String getFromEmail() {
    return getStringFieldValue("fromEmail");
  }

  default void setSubject(String subject) {
    setFieldValue("subject", subject);
  }

  default String getSubject() {
    return getStringFieldValue("subject");
  }

  default void setContent(String content) {
    setFieldValue("content", content);
  }

  default String getContent() {
    return getStringFieldValue("content");
  }

  default void setOut(Boolean out) {
    setFieldValue("out", out);
  }

  default Boolean isOut() {
    return getBooleanFieldValue("out");
  }
}
