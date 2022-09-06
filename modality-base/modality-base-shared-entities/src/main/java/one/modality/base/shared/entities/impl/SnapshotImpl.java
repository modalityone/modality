package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Snapshot;

/**
 * @author Dan Newman
 */
public class SnapshotImpl extends DynamicEntity implements Snapshot {

    public SnapshotImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Snapshot> {
        public ProvidedFactory() {
            super(Snapshot.class, SnapshotImpl::new);
        }
    }
}
