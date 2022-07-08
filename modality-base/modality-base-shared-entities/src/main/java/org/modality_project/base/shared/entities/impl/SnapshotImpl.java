package org.modality_project.base.shared.entities.impl;

import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;
import org.modality_project.base.shared.entities.Snapshot;

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
