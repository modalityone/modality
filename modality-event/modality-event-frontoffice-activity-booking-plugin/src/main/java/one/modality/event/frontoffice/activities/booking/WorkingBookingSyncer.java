package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingSyncer {

    public static void syncWorkingBookingFromEventSchedule(WorkingBooking workingBooking, RecurringEventSchedule recurringEventSchedule) {
        syncWorkingBookingFromNewSelectedScheduledItems(workingBooking, recurringEventSchedule.getSelectedScheduledItem());
    }

    public static void syncWorkingBookingFromNewSelectedScheduledItems(WorkingBooking workingBooking, List<ScheduledItem> scheduledItemsAdded) {
        // Then we re-apply the selected dates to the new booking (move this up once TODO is done)
        workingBooking.cancelChanges(); // weird, but this is to ensure the document is created
        workingBooking.bookScheduledItems(scheduledItemsAdded); // Booking the selected dates
    }

    public static void syncEventScheduleFromWorkingBooking(WorkingBooking workingBooking, RecurringEventSchedule recurringEventSchedule) {
        List<Attendance> attendanceAdded = workingBooking.getAttendanceAdded();
        List<LocalDate> datesAdded = Collections.map(attendanceAdded, Attendance::getDate);
        recurringEventSchedule.setScheduledItems(workingBooking.getScheduledItemsOnEvent(), true);
        recurringEventSchedule.addSelectedDates(datesAdded);
    }

/*
    public static void syncEventScheduleFromWorkingBooking(WorkingBookingProperties workingBookingProperties, RecurringEventSchedule recurringEventSchedule) {
        syncEventScheduleFromWorkingBooking(workingBookingProperties.getWorkingBooking(), recurringEventSchedule);
    }

    public static void syncWorkingBookingFromNewSelectedScheduledItems(WorkingBookingProperties workingBookingProperties, List<ScheduledItem> scheduledItemsAdded) {
        syncWorkingBookingFromNewSelectedScheduledItems(workingBookingProperties.getWorkingBooking(), scheduledItemsAdded);
        workingBookingProperties.updateAll();
    }
*/

}
