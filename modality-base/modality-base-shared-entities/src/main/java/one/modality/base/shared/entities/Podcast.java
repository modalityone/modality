package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.*;

public interface Podcast extends EntityHasChannel, EntityHasMediaInfo, EntityHasAudioUrl, EntityHasWistiaVideoId, EntityHasTeacher {

    default void setChannelPodcastId(Integer channelPodcastId) {
        setFieldValue("channelPodcastId", channelPodcastId);
    }

    default Integer getChannelPodcastId() {
        return getIntegerFieldValue("channelPodcastId");
    }

}
