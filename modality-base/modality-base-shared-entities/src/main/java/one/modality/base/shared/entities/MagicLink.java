package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface MagicLink extends Entity {

    default void setCreationDate(LocalDateTime creationDate) {
        setFieldValue("creationDate", creationDate);
    }

    default LocalDateTime getCreationDate() {
        return getLocalDateTimeFieldValue("creationDate");
    }

    default void setUsageDate(LocalDateTime creationDate) {
        setFieldValue("usageDate", creationDate);
    }

    default LocalDateTime getUsageDate() {
        return getLocalDateTimeFieldValue("usageDate");
    }

    default void setRunId(String runId) {
        setFieldValue("runId", runId);
    }

    default String getRunId() {
        return getStringFieldValue("runId");
    }

    default void setToken(String token) {
        setFieldValue("token", token);
    }

    default String getToken() {
        return getStringFieldValue("token");
    }

    default void setLink(String link) {
        setFieldValue("link", link);
    }

    default String getLink() {
        return getStringFieldValue("link");
    }

    default void setEmail(String email) {
        setFieldValue("email", email);
    }

    default String getEmail() {
        return getStringFieldValue("email");
    }

}
