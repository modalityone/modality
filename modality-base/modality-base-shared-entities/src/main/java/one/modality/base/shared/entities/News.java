package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasChannel;
import one.modality.base.shared.entities.markers.EntityHasMediaInfo;
import one.modality.base.shared.entities.markers.EntityHasTopic;

public interface News extends EntityHasChannel, EntityHasMediaInfo, EntityHasTopic {

    default void setChannelNewsId(Integer channelNewsId) {
        setFieldValue("channelNewsId", channelNewsId);
    }

    default Integer getChannelNewsId() {
        return getIntegerFieldValue("channelNewsId");
    }

    default void setLinkUrl(String linkUrl) {
        setFieldValue("linkUrl", linkUrl);
    }

    default String getLinkUrl() {
        return getStringFieldValue("linkUrl");
    }

}
