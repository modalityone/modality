package one.modality.base.shared.entities.util;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Period;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.Site;

import java.time.LocalDate;
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

    public static boolean isOnToday(Rate rate) {
        return (rate.getOnDate() == null || Times.isPastOrToday(rate.getOnDate()))
               && (rate.getOffDate() == null || Times.isFutureOrToday(rate.getOffDate()));
    }

    public static boolean isApplicableOverPeriod(Rate rate, Period period) {
        return isApplicableOverPeriod(rate, period.getStartDate(), period.getEndDate());
    }

    public static boolean isApplicableOverPeriod(Rate rate, LocalDate startDate, LocalDate endDate) {
        return (rate.getStartDate() == null || Times.isBeforeOrEquals(rate.getStartDate(), endDate))
               && (rate.getEndDate() == null || Times.isAfterOrEquals(rate.getEndDate(), startDate));
    }

    public static boolean isOfSiteAndItem(Rate rate, Site site, Item item) {
        return Entities.sameId(rate.getSite(), site) && Entities.sameId(rate.getItem(), item);
    }

    public static boolean isOnTodayAndApplicableOverPeriod(Rate rate, LocalDate startDate, LocalDate endDate) {
        return isOnToday(rate) && isApplicableOverPeriod(rate, startDate, endDate);
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
        return rates.filter(rate -> isOfSiteAndItem(rate, site, item));
    }

    public static boolean hasFacilityFees(Stream<Rate> rates) {
        return rates.anyMatch(Rates::hasFacilityRate);
    }

}
