package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLocalDateTime;

public interface Podcast extends EntityHasLocalDateTime {

    default void setChannel(Object channel) {
        setForeignField("channel", channel);
    }

    default EntityId getChannelId() {
        return getForeignEntityId("channel");
    }

    default Channel getChannel() {
        return getForeignEntity("channel");
    }

    default void setTeacher(Object teacher) {
        setForeignField("teacher", teacher);
    }

    default EntityId getTeacherId() {
        return getForeignEntityId("teacher");
    }

    default Teacher getTeacher() {
        return getForeignEntity("teacher");
    }

    default void setChannelPodcastId(Integer channelPodcastId) {
        setFieldValue("channelPodcastId", channelPodcastId);
    }

    default Integer getChannelPodcastId() {
        return getIntegerFieldValue("channelPodcastId");
    }

    default void setTitle(String title) {
        setFieldValue("title", title);
    }

    default String getTitle() {
        return getStringFieldValue("title");
    }

    default void setExcerpt(String excerpt) {
        setFieldValue("excerpt", excerpt);
    }

    default String getExcerpt() {
        return getStringFieldValue("excerpt");
    }

    default void setContent(String content) {
        setFieldValue("content", content);
    }

    default String getContent() {
        return getStringFieldValue("content");
    }

    default void setImageUrl(String imageUrl) {
        setFieldValue("imageUrl", imageUrl);
    }

    default String getImageUrl() {
        return getStringFieldValue("imageUrl");
    }

    default void setAudioUrl(String audioUrl) {
        setFieldValue("audioUrl", audioUrl);
    }

    default String getAudioUrl() {
        return getStringFieldValue("audioUrl");
    }

    default void setWistiaVideoId(String wistiaVideoId) {
        setFieldValue("wistiaVideoId", wistiaVideoId);
    }

    default String getWistiaVideoId() {
        return getStringFieldValue("wistiaVideoId");
    }

    default void setDurationMillis(Long durationMillis) {
        setFieldValue("durationMillis", durationMillis);
    }

    default Long getDurationMillis() {
        return getLongFieldValue("durationMillis");
    }

}
