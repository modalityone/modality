package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface MagicLink extends Entity {
    String creationDate = "creationDate";
    String usageDate = "usageDate";
    String usageRunId = "usageRunId";
    String loginRunId = "loginRunId";
    String token = "token";
    String email = "email";
    String oldEmail = "oldEmail";
    String lang = "lang";
    String link = "link";
    String requestedPath = "requestedPath";

    default void setCreationDate(LocalDateTime value) {
        setFieldValue(creationDate, value);
    }

    default LocalDateTime getCreationDate() {
        return getLocalDateTimeFieldValue(creationDate);
    }

    default void setUsageDate(LocalDateTime value) {
        setFieldValue(usageDate, value);
    }

    default LocalDateTime getUsageDate() {
        return getLocalDateTimeFieldValue(usageDate);
    }

    default void setUsageRunId(String value) {
        setFieldValue(usageRunId, value);
    }

    default String getUsageRunId() {
        return getStringFieldValue(usageRunId);
    }

    default void setLoginRunId(String value) {
        setFieldValue(loginRunId, value);
    }

    default String getLoginRunId() {
        return getStringFieldValue(loginRunId);
    }

    default void setToken(String value) {
        setFieldValue(token, value);
    }

    default String getToken() {
        return getStringFieldValue(token);
    }

    default void setEmail(String value) {
        setFieldValue(email, value);
    }

    default String getEmail() {
        return getStringFieldValue(email);
    }

    default void setOldEmail(String value) {
        setFieldValue(oldEmail, value);
    }

    default String getOldEmail() {
        return getStringFieldValue(oldEmail);
    }

    default void setLang(String value) {
        setFieldValue(lang, value);
    }

    default String getLang() {
        return getStringFieldValue(lang);
    }

    default void setLink(String value) {
        setFieldValue(link, value);
    }

    default String getLink() {
        return getStringFieldValue(link);
    }

    default void setRequestedPath(String value) {
        setFieldValue(requestedPath, value);
    }

    default String getRequestedPath() {
        return getStringFieldValue(requestedPath);
    }
}