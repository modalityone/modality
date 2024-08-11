package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasMediaInfo;
import one.modality.base.shared.entities.markers.EntityHasTeacher;
import one.modality.base.shared.entities.markers.EntityHasWistiaVideoId;

public interface Video extends EntityHasMediaInfo, EntityHasWistiaVideoId, EntityHasTeacher {

    default void setNews(Object news) {
        setForeignField("news", news);
    }

    default EntityId getNewsId() {
        return getForeignEntityId("news");
    }

    default News getNews() {
        return getForeignEntity("news");
    }
    
    default void setWidth(Integer width) {
        setFieldValue("width", width);
    }

    default Integer getWidth() {
        return getIntegerFieldValue("width");
    }

    default void setHeight(Integer height) {
        setFieldValue("height", height);
    }

    default Integer getHeight() {
        return getIntegerFieldValue("height");
    }

}
