package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasDate;

public interface News extends EntityHasDate {

    default void setChannel(Object channel) {
        setForeignField("channel", channel);
    }

    default EntityId getChannelId() {
        return getForeignEntityId("channel");
    }

    default Channel getChannel() {
        return getForeignEntity("channel");
    }

    default void setChannelNewsId(String channelNewsId) {
        setFieldValue("channelNewsId", channelNewsId);
    }

    default String getChannelNewsId() {
        return getStringFieldValue("channelNewsId");
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

}
