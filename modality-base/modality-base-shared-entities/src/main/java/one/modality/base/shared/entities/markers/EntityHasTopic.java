package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Topic;

/**
 * @author Bruno Salmon
 */
public interface EntityHasTopic extends Entity, HasTopic {

    @Override
    default void setTopic(Object channel) {
        setForeignField("topic", channel);
    }

    @Override
    default EntityId getTopicId() {
        return getForeignEntityId("topic");
    }

    @Override
    default Topic getTopic() {
        return getForeignEntity("topic");
    }

}
