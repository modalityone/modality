package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface representing booking data for the Gantt view.
 *
 * @author Claude Code Assistant
 */
public interface GanttBookingData {
    String getGuestName();
    String getFirstName();
    String getLastName();
    String getGender();
    String getEvent();
    LocalDate getStartDate();
    LocalDate getEndDate();
    BookingStatus getStatus();
    String getComments();
    List<String> getSpecialNeeds();

    /**
     * Returns true if the guest has been marked as arrived (checked in).
     */
    boolean isArrived();
}
