package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasChannel;
import one.modality.base.shared.entities.markers.EntityHasMediaInfo;
import one.modality.base.shared.entities.markers.EntityHasAudioUrl;
import one.modality.base.shared.entities.markers.EntityHasWistiaVideoId;
import one.modality.base.shared.entities.markers.EntityHasTeacher;

public interface Podcast extends EntityHasChannel, EntityHasMediaInfo, EntityHasAudioUrl, EntityHasWistiaVideoId, EntityHasTeacher {
    String channelPodcastId = "channelPodcastId";

    default void setChannelPodcastId(Integer value) {
        setFieldValue(channelPodcastId, value);
    }

    default Integer getChannelPodcastId() {
        return getIntegerFieldValue(channelPodcastId);
    }
}