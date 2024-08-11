package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Topic;

/**
 * @author Bruno Salmon
 */
public interface HasTopic {

    void setTopic(Object channel);

    EntityId getTopicId();

    Topic getTopic();

}
