package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasAudioUrl extends Entity, HasAudioUrl {

    @Override
    default void setAudioUrl(String audioUrl) {
        setFieldValue("audioUrl", audioUrl);
    }

    @Override
    default String getAudioUrl() {
        return getStringFieldValue("audioUrl");
    }

}
