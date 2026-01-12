package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.PhaseCoverage;

/**
 * @author Bruno Salmon
 */
public final class PhaseCoverageImpl extends DynamicEntity implements PhaseCoverage {

    public PhaseCoverageImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<PhaseCoverage> {
        public ProvidedFactory() {
            super(PhaseCoverage.class, PhaseCoverageImpl::new);
        }
    }
}
