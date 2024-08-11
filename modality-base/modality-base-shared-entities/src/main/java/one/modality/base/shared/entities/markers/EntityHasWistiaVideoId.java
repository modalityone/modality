package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasWistiaVideoId extends Entity, HasWistiaVideoId {

    @Override
    default void setWistiaVideoId(String wistiaVideoId) {
        setFieldValue("wistiaVideoId", wistiaVideoId);
    }

    @Override
    default String getWistiaVideoId() {
        return getStringFieldValue("wistiaVideoId");
    }

}
