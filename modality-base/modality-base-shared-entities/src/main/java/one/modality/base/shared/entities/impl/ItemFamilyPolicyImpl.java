package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ItemFamilyPolicy;
import one.modality.base.shared.entities.EventPhaseCoverage;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ItemFamilyPolicyImpl extends DynamicEntity implements ItemFamilyPolicy {

    private List<EventPhaseCoverage> eventPhaseCoverages;

    public ItemFamilyPolicyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public List<EventPhaseCoverage> getEventPhaseCoverages() {
        if (eventPhaseCoverages == null)
            eventPhaseCoverages = Collections.removeNulls(Collections.listOf(
                getEventPhaseCoverage1(),
                getEventPhaseCoverage2(),
                getEventPhaseCoverage3(),
                getEventPhaseCoverage4())
            );
        return eventPhaseCoverages;
    }

    @Override
    public void setEventPhaseCoverages(List<EventPhaseCoverage> phaseCoverages) {
        this.eventPhaseCoverages = phaseCoverages;
        setEventPhaseCoverage1(Collections.get(phaseCoverages, 0));
        setEventPhaseCoverage2(Collections.get(phaseCoverages, 1));
        setEventPhaseCoverage3(Collections.get(phaseCoverages, 2));
        setEventPhaseCoverage4(Collections.get(phaseCoverages, 3));
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ItemFamilyPolicy> {
        public ProvidedFactory() {
            super(ItemFamilyPolicy.class, ItemFamilyPolicyImpl::new);
        }
    }
}
