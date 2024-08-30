package one.modality.event.frontoffice.activities.booking;

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

    public PriceCalculator(Supplier<DocumentAggregate> documentAggregateSupplier) {
        this.documentAggregateSupplier = documentAggregateSupplier;
    }

    public PriceCalculator(DocumentAggregate documentAggregate) {
        this(() -> documentAggregate);
    }

    public DocumentAggregate getDocumentAggregate() {
        return documentAggregateSupplier.get();
    }

    public int calculateTotalPrice() {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        return documentAggregate.getDocumentLinesStream()
                .mapToInt(this::calculateLinePrice)
                .sum();
    }

    public int calculateLinePrice(DocumentLine line) {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        return calculateLinePrice(documentAggregate.getLineAttendances(line));
    }

    public int calculateLinePrice(List<Attendance> attendances) {
        if (attendances == null || attendances.isEmpty())
            return 0;
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        // 1) Calculating the price consisting of applying the cheapest (if multiple) daily rate on each attendance
        int dailyRatePrice = attendances.stream()
            .mapToInt(this::calculateAttendancePrice)
            .sum();
        // 2) Calculating the price consisting of applying the cheapest (if multiple) fixed rate
        DocumentLine line = attendances.get(0).getDocumentLine();
        int fixedRatePrice = documentAggregate.getPolicyAggregate()
            .getSiteItemFixedRatesStream(line.getSite(), line.getItem())
            // TODO: check if the rate is applicable to this booking. For this first version, we just assume so.
            .mapToInt(Rate::getPrice)
            .min()
            .orElse(0);
        // 3) Returning the cheapest price
        if (fixedRatePrice > 0 && fixedRatePrice < dailyRatePrice) // Typically a discount over a whole series of GP classes
            return fixedRatePrice;
        return dailyRatePrice;
    }

    public int calculateAttendancePrice(Attendance attendance) {
        DocumentAggregate documentAggregate = getDocumentAggregate();
        if (documentAggregate == null)
            return 0;
        DocumentLine documentLine = attendance.getDocumentLine();
        Site site = documentLine.getSite();
        Item item = documentLine.getItem();
        return documentAggregate.getPolicyAggregate()
            .getSiteItemDailyRatesStream(site, item)
                .mapToInt(Rate::getPrice)
                .min()
                .orElse(0);
    }

}
