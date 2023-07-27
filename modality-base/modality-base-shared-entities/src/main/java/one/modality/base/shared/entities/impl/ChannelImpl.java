package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Channel;

/**
 * @author Bruno Salmon
 */
public final class ChannelImpl extends DynamicEntity implements Channel {

    public ChannelImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Channel> {
        public ProvidedFactory() {
            super(Channel.class, ChannelImpl::new);
        }
    }
}
