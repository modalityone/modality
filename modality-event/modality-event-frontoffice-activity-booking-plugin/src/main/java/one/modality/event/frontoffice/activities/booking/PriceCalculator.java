package one.modality.event.frontoffice.activities.booking;

import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * First draft version.
 *
 * @author Bruno Salmon
 */
public class PriceCalculator {

    private final PolicyAggregate policyAggregate;

    public PriceCalculator(PolicyAggregate policyAggregate) {
        this.policyAggregate = policyAggregate;
    }

    public int calculateTotalPrice(DocumentAggregate documentAggregate) {
        return documentAggregate.getDocumentLinesStream()
                .mapToInt(line -> calculateLinePrice(documentAggregate, line))
                .sum();
    }

    public int calculateLinePrice(DocumentAggregate documentAggregate, DocumentLine line) {
        return documentAggregate.getLineAttendancesStream(line)
                .mapToInt(a -> calculateAttendancePrice(documentAggregate, a))
                .sum();
    }

    public int calculateAttendancePrice(DocumentAggregate documentAggregate, Attendance attendance) {
        DocumentLine documentLine = attendance.getDocumentLine();
        Site site = documentLine.getSite();
        Item item = documentLine.getItem();
        return policyAggregate.getSiteItemRatesStream(site, item)
                .mapToInt(Rate::getPrice)
                .min()
                .orElse(0);
    }

}
