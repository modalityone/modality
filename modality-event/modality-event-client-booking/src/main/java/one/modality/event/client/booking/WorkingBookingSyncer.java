package one.modality.event.client.booking;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingSyncer {

    public static void syncWorkingBookingFromEventSchedule(WorkingBooking workingBooking, BookableDatesUi bookableDatesUi, boolean addOnly) {
        syncWorkingBookingFromSelectedScheduledItems(workingBooking, bookableDatesUi.getSelectedScheduledItem(), addOnly);
    }

    public static void syncWorkingBookingFromSelectedScheduledItems(WorkingBooking workingBooking, List<ScheduledItem> scheduledItems, boolean addOnly) {
        workingBooking.cancelChanges(); // weird, but this is to ensure the document is created
        workingBooking.bookScheduledItems(scheduledItems, addOnly); // We re-apply the selected items to the booking
    }

    public static void syncEventScheduleFromWorkingBooking(WorkingBooking workingBooking, BookableDatesUi bookableDatesUi) {
        bookableDatesUi.setScheduledItems(workingBooking.getScheduledItemsOnEvent(), true);
        List<Attendance> attendanceAdded = workingBooking.getAttendancesAdded(true);
        List<LocalDate> datesAdded = Collections.map(attendanceAdded, Attendance::getDate);
        bookableDatesUi.addSelectedDates(datesAdded);
    }

}
