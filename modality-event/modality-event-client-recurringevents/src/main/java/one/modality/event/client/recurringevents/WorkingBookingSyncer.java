package one.modality.event.client.recurringevents;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingSyncer {

    public static void syncWorkingBookingFromEventSchedule(WorkingBooking workingBooking, RecurringEventSchedule recurringEventSchedule, boolean addOnly) {
        syncWorkingBookingFromSelectedScheduledItems(workingBooking, recurringEventSchedule.getSelectedScheduledItem(), addOnly);
    }

    public static void syncWorkingBookingFromSelectedScheduledItems(WorkingBooking workingBooking, List<ScheduledItem> scheduledItems, boolean addOnly) {
      //  if (workingBooking.getDocument() == null)
            workingBooking.cancelChanges(); // weird, but this is to ensure the document is created
        workingBooking.bookScheduledItems(scheduledItems, addOnly); // We re-apply the selected items to the booking
    }

    public static void syncEventScheduleFromWorkingBooking(WorkingBooking workingBooking, RecurringEventSchedule recurringEventSchedule) {
        List<Attendance> attendanceAdded = workingBooking.getAttendanceAdded();
        List<LocalDate> datesAdded = Collections.map(attendanceAdded, Attendance::getDate);
        recurringEventSchedule.setScheduledItems(workingBooking.getScheduledItemsOnEvent(), true);
        recurringEventSchedule.addSelectedDates(datesAdded);
    }

/* Same API but with WorkingBookingProperties
    public static void syncEventScheduleFromWorkingBooking(WorkingBookingProperties workingBookingProperties, RecurringEventSchedule recurringEventSchedule) {
        syncEventScheduleFromWorkingBooking(workingBookingProperties.getWorkingBooking(), recurringEventSchedule);
    }

    public static void syncWorkingBookingFromNewSelectedScheduledItems(WorkingBookingProperties workingBookingProperties, List<ScheduledItem> scheduledItemsAdded) {
        syncWorkingBookingFromNewSelectedScheduledItems(workingBookingProperties.getWorkingBooking(), scheduledItemsAdded);
        workingBookingProperties.updateAll();
    }
*/

}
