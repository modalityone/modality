package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLocalDateTime;

public interface News extends EntityHasLocalDateTime {

    default void setChannel(Object channel) {
        setForeignField("channel", channel);
    }

    default EntityId getChannelId() {
        return getForeignEntityId("channel");
    }

    default Channel getChannel() {
        return getForeignEntity("channel");
    }

    default void setChannelNewsId(Integer channelNewsId) {
        setFieldValue("channelNewsId", channelNewsId);
    }

    default Integer getChannelNewsId() {
        return getIntegerFieldValue("channelNewsId");
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

    default void setLang(String lang) {
        setFieldValue("lang", lang);
    }

    default String getLang() {
        return getStringFieldValue("lang");
    }

    default void setImageUrl(String imageUrl) {
        setFieldValue("imageUrl", imageUrl);
    }

    default String getImageUrl() {
        return getStringFieldValue("imageUrl");
    }

    default void setLinkUrl(String linkUrl) {
        setFieldValue("linkUrl", linkUrl);
    }

    default String getLinkUrl() {
        return getStringFieldValue("linkUrl");
    }

    default void setTopic(Object channel) {
        setForeignField("topic", channel);
    }

    default EntityId getTopicId() {
        return getForeignEntityId("topic");
    }

    default Topic getTopic() {
        return getForeignEntity("topic");
    }

}
