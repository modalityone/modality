package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.BuildingZone;

/**
 * @author Bruno Salmon
 */
public final class BuildingZoneImpl extends DynamicEntity implements BuildingZone {

    public BuildingZoneImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<BuildingZone> {
        public ProvidedFactory() {
            super(BuildingZone.class, BuildingZoneImpl::new);
        }
    }
}
