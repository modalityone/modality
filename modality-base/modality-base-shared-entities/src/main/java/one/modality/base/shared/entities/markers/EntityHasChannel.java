package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Channel;

/**
 * @author Bruno Salmon
 */
public interface EntityHasChannel extends Entity, HasChannel {

    String channel = "channel";

    @Override
    default void setChannel(Object value) {
        setForeignField(channel, value);
    }

    @Override
    default EntityId getChannelId() {
        return getForeignEntityId(channel);
    }

    @Override
    default Channel getChannel() {
        return getForeignEntity(channel);
    }
}