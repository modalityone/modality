package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasCorporation;

/**
 * @author Bruno Salmon
 */
public interface FrontendAccount extends Entity, EntityHasCorporation {

    default void setUsername(String username) {
        setFieldValue("username", username);
    }

    default String getUsername() {
        return getStringFieldValue("username");
    }

    default void setPassword(String username) {
        setFieldValue("password", username);
    }

    default String getPassword() {
        return getStringFieldValue("password");
    }

    default void setLang(String lang) {
        setFieldValue("lang", lang);
    }

    default String getLang() {
        return getStringFieldValue("lang");
    }

}
