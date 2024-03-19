package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Topic extends
        EntityHasName,
        EntityHasLabel {

    default void setChannelTopicId(String channelNewsId) {
        setFieldValue("channelTopicId", channelNewsId);
    }

    default String getChannelTopicId() {
        return getStringFieldValue("channelTopicId");
    }


}
