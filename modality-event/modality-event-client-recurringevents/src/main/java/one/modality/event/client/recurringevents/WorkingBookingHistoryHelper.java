package one.modality.event.client.recurringevents;

import dev.webfx.platform.util.time.Times;
import one.modality.base.shared.entities.Attendance;

import java.util.List;

public final class WorkingBookingHistoryHelper {

    private final List<Attendance> attendanceAdded;
    private final List<Attendance> attendanceRemoved;
    private final StringBuilder history = new StringBuilder();

    public WorkingBookingHistoryHelper(List<Attendance> attendanceAdded, List<Attendance> attendanceRemoved) {
        this.attendanceAdded = attendanceAdded;
        this.attendanceRemoved = attendanceRemoved;
    }

    public String buildHistory() {
        boolean first = true;
        if (!attendanceAdded.isEmpty()) {
            history.append("Booked ");
            for (Attendance attendance : attendanceAdded) {
                if (!first)
                    history.append(", ");
                // We get the date throw the scheduledItem associated to the attendance, because the
                // attendance date is not loaded from the database if it comes from a previous booking
                history.append(Times.format(attendance.getScheduledItem().getDate(), "dd/MM"));
                first = false;
            }
        }
        if (!attendanceRemoved.isEmpty()) {
            history.append(first ? "Removed " : " & removed ");
            first = true;
            for (Attendance attendance : attendanceRemoved) {
                if (!first)
                    history.append(", ");
                history.append(Times.format(attendance.getScheduledItem().getDate(), "dd/MM"));
                first = false;
            }
        }
        return history.toString();
    }

}
