package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface GatewayParameter extends EntityHasName, EntityHasIcon {

    default void setValue(String value) {
        setFieldValue("value", value);
    }

    default String getValue() {
        return getStringFieldValue("value");
    }

    default void setTest(Boolean test) {
        setFieldValue("test", test);
    }

    default Boolean isTest() {
        return getBooleanFieldValue("test");
    }

    default void setLive(Boolean live) {
        setFieldValue("live", live);
    }

    default Boolean isLive() {
        return getBooleanFieldValue("live");
    }
}
