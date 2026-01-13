package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ItemFamilyPolicy;
import one.modality.base.shared.entities.PhaseCoverage;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ItemFamilyPolicyImpl extends DynamicEntity implements ItemFamilyPolicy {

    private List<PhaseCoverage> phaseCoverages;

    public ItemFamilyPolicyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public List<PhaseCoverage> getPhaseCoverages() {
        if (phaseCoverages == null)
            phaseCoverages = Collections.removeNulls(Collections.listOf(getPhaseCoverage1(), getPhaseCoverage2(), getPhaseCoverage3(), getPhaseCoverage4()));
        return phaseCoverages;
    }

    @Override
    public void setPhaseCoverages(List<PhaseCoverage> phaseCoverages) {
        this.phaseCoverages = phaseCoverages;
        setPhaseCoverage1(Collections.get(phaseCoverages, 0));
        setPhaseCoverage2(Collections.get(phaseCoverages, 1));
        setPhaseCoverage3(Collections.get(phaseCoverages, 2));
        setPhaseCoverage4(Collections.get(phaseCoverages, 3));
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ItemFamilyPolicy> {
        public ProvidedFactory() {
            super(ItemFamilyPolicy.class, ItemFamilyPolicyImpl::new);
        }
    }
}
