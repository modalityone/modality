package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasCorporation;

/**
 * @author Bruno Salmon
 */
public interface FrontendAccount extends Entity, EntityHasCorporation {
    String username = "username";
    String password = "password";
    String lang = "lang";
    String salt = "salt";

    default void setUsername(String value) {
        setFieldValue(username, value);
    }

    default String getUsername() {
        return getStringFieldValue(username);
    }

    default void setPassword(String value) {
        setFieldValue(password, value);
    }

    default String getPassword() {
        return getStringFieldValue(password);
    }

    default void setSalt(String value) {
        setFieldValue(salt, value);
    }

    default String getSalt() {
        return getStringFieldValue(salt);
    }

    default void setLang(String value) {
        setFieldValue(lang, value);
    }

    default String getLang() {
        return getStringFieldValue(lang);
    }
}