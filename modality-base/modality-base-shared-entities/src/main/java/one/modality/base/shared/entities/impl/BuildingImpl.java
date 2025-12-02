package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Building;

/**
 * @author Bruno Salmon
 */
public final class BuildingImpl extends DynamicEntity implements Building {

    public BuildingImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Building> {
        public ProvidedFactory() {
            super(Building.class, BuildingImpl::new);
        }
    }
}
