package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface FrontendAccount extends Entity {

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

}
