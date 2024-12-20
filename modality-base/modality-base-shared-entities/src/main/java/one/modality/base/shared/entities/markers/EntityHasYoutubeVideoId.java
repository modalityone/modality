package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasYoutubeVideoId extends Entity, HasYoutubeVideoId {

    String youtubeVideoId = "youtubeVideoId";

    @Override
    default void setYoutubeVideoId(String value) {
        setFieldValue(youtubeVideoId, value);
    }

    @Override
    default String getYoutubeVideoId() {
        return getStringFieldValue(youtubeVideoId);
    }

}