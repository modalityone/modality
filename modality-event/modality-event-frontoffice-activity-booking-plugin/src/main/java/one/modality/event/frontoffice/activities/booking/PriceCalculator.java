package one.modality.event.frontoffice.activities.booking;

import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.DocumentAggregate;

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
        return getDocumentAggregate().getDocumentLinesStream()
                .mapToInt(this::calculateLinePrice)
                .sum();
    }

    public int calculateLinePrice(DocumentLine line) {
        return getDocumentAggregate().getLineAttendancesStream(line)
                .mapToInt(this::calculateAttendancePrice)
                .sum();
    }

    public int calculateAttendancePrice(Attendance attendance) {
        DocumentLine documentLine = attendance.getDocumentLine();
        Site site = documentLine.getSite();
        Item item = documentLine.getItem();
        return getDocumentAggregate().getPolicyAggregate().getSiteItemRatesStream(site, item)
                .mapToInt(Rate::getPrice)
                .min()
                .orElse(0);
    }

    public int calculateBalanceToPay() {
        return calculateTotalPrice() - getDocumentAggregate().getDeposit();
    }

}
