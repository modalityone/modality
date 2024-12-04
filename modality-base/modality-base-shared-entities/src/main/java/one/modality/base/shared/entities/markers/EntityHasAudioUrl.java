package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasAudioUrl extends Entity, HasAudioUrl {

    String audioUrl = "audioUrl";

    @Override
    default void setAudioUrl(String value) {
        setFieldValue(audioUrl, value);
    }

    @Override
    default String getAudioUrl() {
        return getStringFieldValue(audioUrl);
    }
}