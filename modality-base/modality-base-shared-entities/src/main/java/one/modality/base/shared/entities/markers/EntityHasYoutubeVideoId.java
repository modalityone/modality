package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasYoutubeVideoId extends Entity, HasYoutubeVideoId {

    @Override
    default void setYoutubeVideoId(String wistiaVideoId) {
        setFieldValue("youtubeVideoId", wistiaVideoId);
    }

    @Override
    default String getYoutubeVideoId() {
        return getStringFieldValue("youtubeVideoId");
    }

}
