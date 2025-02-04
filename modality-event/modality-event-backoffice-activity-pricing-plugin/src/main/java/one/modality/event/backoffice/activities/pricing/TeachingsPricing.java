package one.modality.event.backoffice.activities.pricing;

import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
final class TeachingsPricing extends AbstractItemFamilyPricing {

    public TeachingsPricing(PolicyAggregate eventPolicy) {
        super(KnownItemFamily.TEACHING, EventPricingI18nKeys.Teachings, eventPolicy, true);
    }
}
