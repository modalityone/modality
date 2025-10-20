package one.modality.booking.client.workingbooking;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Attendance;
import one.modality.ecommerce.document.service.events.book.AddRequestEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public final class WorkingBookingHistoryHelper {

    private final WorkingBooking workingBooking;

    public WorkingBookingHistoryHelper(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
    }

    public String generateHistoryComment() {
        StringBuilder sb = new StringBuilder();
        appendHistoryCommentFromAttendances(workingBooking.getAttendancesAdded(true), true, sb);
        appendHistoryCommentFromAttendances(workingBooking.getAttendancesRemoved(true), false, sb);
        // "Wrote a request" comment if the user wrote a request
        AddRequestEvent addRequestEvent = workingBooking.findAddRequestEvent(true);
        if (addRequestEvent != null) {
            newSection(sb).append("Wrote a request"); // No need to say more, as the request itself will be copied in
            // the history (request) on the server side (see HistoryRecorder).
        }
        PriceDocumentLineEvent priceDocumentLineEvent = workingBooking.findPriceDocumentLineEvent(true);
        if (priceDocumentLineEvent != null) {
            Integer priceDiscount = priceDocumentLineEvent.getPrice_discount();
            if (priceDiscount != null) {
                newSection(sb);
                if (priceDiscount == 0)
                    sb.append("Removed the discount on ");
                else
                    sb.append("Applied a ").append(priceDiscount).append("% discount on ");
                sb.append(priceDocumentLineEvent.getDocumentLine().getItem().getName());
            }
        }
        return sb.toString();
    }

    private void appendHistoryCommentFromAttendances(List<Attendance> attendances, boolean added, StringBuilder sb) {
        if (!attendances.isEmpty()) {
            newSection(sb).append(!added ? "Removed" : workingBooking.isNewBooking() ? "Booked" : "Added");
            attendances.stream().collect(Collectors.groupingBy(a -> a.getScheduledItem().getItem()))
                .forEach((item, itemAttendances) -> {
                    List<LocalDate> dates = Collections.map(itemAttendances, a -> a.getScheduledItem().getDate());
                    sb.append("  ⦿ ").append(item.getName()).append(" ").append(ModalityDates.formatDateSeries(dates));
                });
        }
    }

    private StringBuilder newSection(StringBuilder sb) {
        if (sb.length() > 0) // GWT
            sb.append("  ~  ");
        return sb;
    }

}
