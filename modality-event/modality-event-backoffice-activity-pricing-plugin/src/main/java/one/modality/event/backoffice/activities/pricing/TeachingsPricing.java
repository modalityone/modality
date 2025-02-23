package one.modality.event.backoffice.activities.pricing;

import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.Rate;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
final class TeachingsPricing extends AbstractItemFamilyPricing {

    public TeachingsPricing(PolicyAggregate eventPolicy) {
        super(KnownItemFamily.TEACHING, EventPricingI18nKeys.Teachings, eventPolicy, true);
    }

    @Override
    protected void completeRate(Rate rate) {
        super.completeRate(rate);
        rate.setAge1Max(7);
        rate.setAge1Price(0);
        rate.setAge2Max(15);
        rate.setAge2Discount(50);
        rate.setResidentPrice(0);
        rate.setResident2Discount(50);
    }
}
