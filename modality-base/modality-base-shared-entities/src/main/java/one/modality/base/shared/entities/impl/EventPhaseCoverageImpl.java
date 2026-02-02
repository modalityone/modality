package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.EventPhase;
import one.modality.base.shared.entities.EventPhaseCoverage;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class EventPhaseCoverageImpl extends DynamicEntity implements EventPhaseCoverage {

    private List<EventPhase> phases;

    public EventPhaseCoverageImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public List<EventPhase> getPhases() {
        if (phases == null)
            phases = Collections.removeNulls(Collections.listOf(getPhase1(), getPhase2(), getPhase3(), getPhase4()));
        return phases;
    }

    @Override
    public void setPhases(List<EventPhase> phases) {
        this.phases = phases;
        setPhase1(Collections.get(phases, 0));
        setPhase2(Collections.get(phases, 1));
        setPhase3(Collections.get(phases, 2));
        setPhase4(Collections.get(phases, 3));
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventPhaseCoverage> {
        public ProvidedFactory() {
            super(EventPhaseCoverage.class, EventPhaseCoverageImpl::new);
        }
    }
}
