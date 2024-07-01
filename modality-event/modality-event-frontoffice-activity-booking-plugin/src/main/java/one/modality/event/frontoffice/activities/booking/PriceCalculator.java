package one.modality.event.frontoffice.activities.booking;

import one.modality.base.shared.entities.*;

/**
 * First draft version.
 *
 * @author Bruno Salmon
 */
public class PriceCalculator {

    private final WorkingBooking workingBooking;

    public PriceCalculator(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
    }

    public int calculateTotalPrice() {
        return workingBooking.getLastestDocumentAggregate().getDocumentLinesStream()
                .mapToInt(this::calculateLinePrice)
                .sum();
    }

    public int calculateLinePrice(DocumentLine line) {
        return workingBooking.getLastestDocumentAggregate().getLineAttendancesStream(line)
                .mapToInt(this::calculateAttendancePrice)
                .sum();
    }

    public int calculateAttendancePrice(Attendance attendance) {
        DocumentLine documentLine = attendance.getDocumentLine();
        Site site = documentLine.getSite();
        Item item = documentLine.getItem();
        return workingBooking.getLastestDocumentAggregate().getPolicyAggregate().getSiteItemRatesStream(site, item)
                .mapToInt(Rate::getPrice)
                .min()
                .orElse(0);
    }

}
