package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Channel;

/**
 * @author Bruno Salmon
 */
public interface HasChannel {

    void setChannel(Object channel);

    EntityId getChannelId();

    Channel getChannel();

}
