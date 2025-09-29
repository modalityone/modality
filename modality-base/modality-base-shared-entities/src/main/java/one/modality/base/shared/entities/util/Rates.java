package one.modality.base.shared.entities.util;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.Site;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class Rates {

    public static boolean isDailyRate(Rate rate) {
        return rate.isPerDay();
    }

    public static boolean isFixedRate(Rate rate) {
        return !isDailyRate(rate);
    }

    public static boolean hasFacilityRate(Rate rate) {
        return rate.getFacilityFeePrice() != null || rate.getFacilityFeeDiscount() != null;
    }

    public static Stream<Rate> filterDailyRates(Stream<Rate> rates) {
        return rates.filter(Rates::isDailyRate);
    }

    public static Stream<Rate> filterFixedRates(Stream<Rate> rates) {
        return rates.filter(Rates::isFixedRate);
    }

    public static List<Rate> filterDailyRates(List<Rate> rates) {
        return Collections.filter(rates, Rate::isPerDay);
    }

    public static Stream<Rate> filterRatesOfSiteAndItem(Stream<Rate> rates, Site site, Item item) {
        return rates.filter(r -> Entities.sameId(r.getSite(), site) && Entities.sameId(r.getItem(), item));
    }

    public static boolean hasFacilityFees(Stream<Rate> rates) {
        return rates.anyMatch(Rates::hasFacilityRate);
    }


}
