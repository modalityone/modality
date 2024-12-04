package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface GatewayParameter extends
    EntityHasName,
    EntityHasIcon {
    String value = "value";
    String test = "test";
    String live = "live";

    default void setValue(String value) {
        setFieldValue(value, value);
    }

    default String getValue() {
        return getStringFieldValue(value);
    }

    default void setTest(Boolean value) {
        setFieldValue(test, value);
    }

    default Boolean isTest() {
        return getBooleanFieldValue(test);
    }

    default void setLive(Boolean value) {
        setFieldValue(live, value);
    }

    default Boolean isLive() {
        return getBooleanFieldValue(live);
    }
}