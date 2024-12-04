package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasChannel;
import one.modality.base.shared.entities.markers.EntityHasMediaInfo;
import one.modality.base.shared.entities.markers.EntityHasTopic;

public interface News extends EntityHasChannel, EntityHasMediaInfo, EntityHasTopic {
    String channelNewsId = "channelNewsId";
    String linkUrl = "linkUrl";

    default void setChannelNewsId(Integer value) {
        setFieldValue(channelNewsId, value);
    }

    default Integer getChannelNewsId() {
        return getIntegerFieldValue(channelNewsId);
    }

    default void setLinkUrl(String value) {
        setFieldValue(linkUrl, value);
    }

    default String getLinkUrl() {
        return getStringFieldValue(linkUrl);
    }
}