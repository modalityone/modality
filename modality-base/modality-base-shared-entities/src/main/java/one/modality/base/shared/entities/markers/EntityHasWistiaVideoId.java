package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasWistiaVideoId extends Entity, HasWistiaVideoId {

    String wistiaVideoId = "wistiaVideoId";

    @Override
    default void setWistiaVideoId(String value) {
        setFieldValue(wistiaVideoId, value);
    }

    @Override
    default String getWistiaVideoId() {
        return getStringFieldValue(wistiaVideoId);
    }

}