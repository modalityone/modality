package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasLocalDateTime;
import one.modality.base.shared.entities.markers.EntityHasUserPerson;

/**
 * @author Bruno Salmon
 */
public interface Error extends Entity, EntityHasLocalDateTime, EntityHasUserPerson {

    String message = "message";
    String userAgent = "userAgent";
    String route = "route";

    default void setMessage(String value) {
        setFieldValue(message, value);
    }

    default String getMessage() {
        return getStringFieldValue(message);
    }

    default void setUserAgent(String value) {
        setFieldValue(userAgent, value);
    }

    default String getUserAgent() {
        return getStringFieldValue(userAgent);
    }

    default void setRoute(String value) {
        setFieldValue(route, value);
    }

    default String getRoute() {
        return getStringFieldValue(route);
    }

}