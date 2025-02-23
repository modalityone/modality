package one.modality.ecommerce.client.workingbooking;

import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.util.List;
import java.util.function.Supplier;

/**
 * First draft version.
 *
 * @author Bruno Salmon
 */
public class PriceCalculator {

    private final Supplier<DocumentAggregate> documentAggregateSupplier;
    private boolean ignoreDiscounts;

    public PriceCalculator(Supplier<DocumentAggregate> documentAggregateSupplier) {
        this.documentAggregateSupplier = documentAggregateSupplier;
    }

    public PriceCalculator(DocumentAggregate documentAggregate) {
        this(() -> documentAggregate);
    }

    public DocumentAggregate getDocumentAggregate() {
        return documentAggregateSupplier.get();
    }

    public int calculateNoDiscountTotalPrice() {
        ignoreDiscounts = true;
        int price = calculateTotalPrice();
        ignoreDiscounts = false;
        return price;
    }

    public int calculateTotalPrice() {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        // TODO: consider ignoreDiscounts flag in KBS2 price algorithm
        return Kbs2PriceAlgorithm.computeBookingPrice(getDocumentAggregate(), false);
        /* Old commented code (now using KBS2 price algorithm instead
        return documentAggregate.getDocumentLinesStream()
            .mapToInt(this::calculateLinePrice)
            .sum();
         */
    }

    // The remaining methods are not based on kbs2 price algorithm, they are simpler and meant to be used for simple
    // events with no complex pricing rules, and that needs to display the details for each day (ex: GP classes).

    public int calculateLinePrice(DocumentLine line) {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        return calculateLinePrice(line.getSite(), line.getItem(), documentAggregate.getLineAttendances(line));
    }

    public int calculateLinePrice(List<Attendance> attendances) {
        if (Collections.isEmpty(attendances))
            return 0;
        DocumentLine line = attendances.get(0).getDocumentLine();
        return calculateLinePrice(line.getSite(), line.getItem(), attendances);
    }

    public int calculateLinePrice(Site site, Item item, List<Attendance> attendances) {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        // 1) Calculating the price consisting of applying the cheapest (if multiple) daily rate on each attendance
        int dailyRatePrice;
        if (attendances == null || attendances.isEmpty())
            dailyRatePrice = 0;
        else
            dailyRatePrice = attendances.stream()
                //.mapToInt(this::calculateAttendancePrice) // assumes Attendance DocumentLine is set
                .mapToInt(attendance -> calculateAttendancePrice(site, item)) // works even if DocumentLine is not set
                .sum();
        if (ignoreDiscounts && dailyRatePrice > 0)
            return dailyRatePrice;
        // 2) Calculating the price consisting of applying the cheapest (if multiple) fixed rate
        int fixedRatePrice = documentAggregate.getPolicyAggregate()
            .getSiteItemFixedRatesStream(site, item)
            .filter(this::isRateApplicable)
            .mapToInt(this::getCheapestApplicableRatePrice)
            .min()
            .orElse(0);
        // 3) Returning the cheapest price
        if (fixedRatePrice > 0 && // if a fixed rate was set
            (dailyRatePrice == 0 || // and no daily rate was set
             fixedRatePrice < dailyRatePrice) // or is cheaper (ex: discount over a whole series of GP classes)
        ) // then we return the fixed rate price
            return fixedRatePrice;
        return dailyRatePrice; // otherwise the daily rate price
    }

    private boolean isRateApplicable(Rate rate) {
        // TODO: check if the rate is applicable to this booking. For this first version, we just assume so.
        return true;
    }

    private int getCheapestApplicableRatePrice(Rate rate) {
        int price = rate.getPrice();
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate != null && !ignoreDiscounts) {
            if (Booleans.isTrue(documentAggregate.getDocument().isPersonFacilityFee())) {
                Integer facilityFeePrice = rate.getFacilityFeePrice();
                if (facilityFeePrice != null && facilityFeePrice < price)
                    price = facilityFeePrice;
            }
        }
        return price;
    }

    public int calculateAttendancePrice(Attendance attendance) {
        DocumentLine line = attendance.getDocumentLine();
        return calculateAttendancePrice(line);
    }

    public int calculateAttendancePrice(DocumentLine line) {
        return calculateAttendancePrice(line.getSite(), line.getItem());
    }

    public int calculateAttendancePrice(Site site, Item item) {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        return documentAggregate.getPolicyAggregate()
            .getSiteItemDailyRatesStream(site, item)
            .filter(this::isRateApplicable)
            .mapToInt(this::getCheapestApplicableRatePrice)
            .min()
            .orElse(0);
    }

}
