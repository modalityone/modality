package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasMediaInfo;
import one.modality.base.shared.entities.markers.EntityHasTeacher;
import one.modality.base.shared.entities.markers.EntityHasWistiaVideoId;
import one.modality.base.shared.entities.markers.EntityHasYoutubeVideoId;

public interface Video extends EntityHasMediaInfo, EntityHasWistiaVideoId, EntityHasYoutubeVideoId, EntityHasTeacher {
    String news = "news";
    String width = "width";
    String height = "height";
    String mediaId = "mediaId";
    String ord = "ord";

    default void setNews(Object value) {
        setForeignField(news, value);
    }

    default EntityId getNewsId() {
        return getForeignEntityId(news);
    }

    default News getNews() {
        return getForeignEntity(news);
    }

    default void setWidth(Integer value) {
        setFieldValue(width, value);
    }

    default Integer getWidth() {
        return getIntegerFieldValue(width);
    }

    default void setHeight(Integer value) {
        setFieldValue(height, value);
    }

    default Integer getHeight() {
        return getIntegerFieldValue(height);
    }

    default void setMediaId(String value) {
        setFieldValue(mediaId, value);
    }

    default String getMediaId() {
        return getStringFieldValue(mediaId);
    }

    default void setOrd(Integer value) {
        setFieldValue(ord, value);
    }

    default Integer getOrd() {
        return getIntegerFieldValue(ord);
    }
}