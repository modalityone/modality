package one.modality.booking.client.scheduleditemsselector;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingSyncer {

    public static void syncWorkingBookingFromScheduledItemsSelector(WorkingBooking workingBooking, ScheduledItemsSelector scheduledItemsSelector, boolean addOnly) {
        syncWorkingBookingFromSelectedScheduledItems(workingBooking, scheduledItemsSelector.getSelectedScheduledItems(), addOnly);
    }

    public static void syncWorkingBookingFromSelectedScheduledItems(WorkingBooking workingBooking, List<ScheduledItem> selectedScheduledItems, boolean addOnly) {
        workingBooking.cancelChanges(); // weird, but this is to ensure the document is created
        workingBooking.bookScheduledItems(selectedScheduledItems, addOnly); // We re-apply the selected items to the booking
    }

    public static void syncScheduledItemsSelectorFromWorkingBooking(ScheduledItemsSelector scheduledItemsSelector, WorkingBooking workingBooking) {
        List<ScheduledItem> policyScheduledItems = workingBooking.getPolicyScheduledItems();
        List<Attendance> attendanceAdded = workingBooking.getAttendancesAdded(true);
        List<LocalDate> datesAdded = Collections.map(attendanceAdded, Attendance::getDate);
        syncScheduledItemsSelectorFromPolicyAndBookedDates(scheduledItemsSelector, policyScheduledItems, datesAdded);
    }

    public static void syncScheduledItemsSelectorFromPolicyAndBookedDates(ScheduledItemsSelector scheduledItemsSelector, List<ScheduledItem> policyScheduledItems, List<LocalDate> bookedDates) {
        scheduledItemsSelector.setSelectableScheduledItems(policyScheduledItems, true);
        scheduledItemsSelector.addSelectedDates(bookedDates);
    }

}
