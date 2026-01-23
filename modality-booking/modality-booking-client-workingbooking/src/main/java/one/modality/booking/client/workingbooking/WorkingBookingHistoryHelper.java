package one.modality.booking.client.workingbooking;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.util.Attendances;
import one.modality.ecommerce.document.service.events.book.AddRequestEvent;
import one.modality.ecommerce.document.service.events.book.ApplyFacilityFeeEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public final class WorkingBookingHistoryHelper {

    public static String generateHistoryComment(WorkingBooking workingBooking) {
        StringBuilder sb = new StringBuilder();
        appendHistoryCommentFromAttendances(workingBooking.getAttendancesAdded(true), true, sb, workingBooking);
        appendHistoryCommentFromAttendances(workingBooking.getAttendancesRemoved(true), false, sb, workingBooking);
        // "Selected member rate" if the user selected the member rate
        ApplyFacilityFeeEvent applyFacilityFeeEvent = workingBooking.findApplyFacilityFeeEvent(true);
        if (applyFacilityFeeEvent != null) {
            newSection(sb).append(applyFacilityFeeEvent.isApply() ? "Selected member rate" : "Unselected member rate");
        }
        // "Wrote a request" if the user wrote a request
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

    private static void appendHistoryCommentFromAttendances(List<Attendance> attendances, boolean added, StringBuilder sb, WorkingBooking workingBooking) {
        if (!attendances.isEmpty()) {
            newSection(sb).append(!added ? "Removed" : workingBooking.isNewBooking() ? "Booked" : "Added");
            attendances.stream().collect(Collectors.groupingBy(Attendances::getItem))
                // Sorting by item family ordinal then item ordinal
                .entrySet().stream().sorted(Comparator.<Map.Entry<Item, List<Attendance>>, Integer>comparing(entry -> entry.getKey().getFamily().getOrd()).thenComparing(entry -> entry.getKey().getOrd()))
                .forEach(entry -> {
                    Item item = entry.getKey();
                    List<Attendance> itemAttendances = entry.getValue();
                    List<LocalDate> dates = Collections.map(itemAttendances, Attendances::getDate);
                    sb.append("  ⦿ ").append(item.getName()).append(" ").append(ModalityDates.formatDateSeries(dates));
                });
        }
    }

    private static StringBuilder newSection(StringBuilder sb) {
        if (sb.length() > 0) // GWT
            sb.append("  ~  ");
        return sb;
    }

}
