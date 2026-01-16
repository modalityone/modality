package one.modality.ecommerce.shared.pricecalculator;

import one.modality.base.shared.entities.Rate;

/**
 * @author Bruno Salmon
 */
@SuppressWarnings("unusable-by-js")
record PriceMemo(Rate rate, int dailyPrice, int price, int consumableDays) {
}
