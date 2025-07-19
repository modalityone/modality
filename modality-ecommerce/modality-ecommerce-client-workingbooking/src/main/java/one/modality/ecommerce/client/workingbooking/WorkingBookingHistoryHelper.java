package one.modality.ecommerce.client.workingbooking;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Attendance;

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
        return sb.toString();
    }

    private void appendHistoryCommentFromAttendances(List<Attendance> attendances, boolean added, StringBuilder sb) {
        if (!attendances.isEmpty()) {
            if (sb.length() > 0) // GWT
                sb.append("  ~  ");
            sb.append(!added ? "Removed" : workingBooking.isNewBooking() ? "Booked" : "Added");
            attendances.stream().collect(Collectors.groupingBy(a -> a.getScheduledItem().getItem()))
                .forEach((item, itemAttendances) -> {
                    List<LocalDate> dates = Collections.map(itemAttendances, a -> a.getScheduledItem().getDate());
                    sb.append("  ⦿ ").append(item.getName()).append(" ").append(ModalityDates.formatDateSeries(dates));
                });
        }
    }

}
