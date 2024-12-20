package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Topic extends
    EntityHasName,
    EntityHasLabel {

    String channelTopicId = "channelTopicId";

    default void setChannelTopicId(String value) {
        setFieldValue(channelTopicId, value);
    }

    default String getChannelTopicId() {
        return getStringFieldValue(channelTopicId);
    }
}