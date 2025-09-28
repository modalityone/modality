package one.modality.booking.backoffice.bookingeditor.family;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public abstract class FamilyBookingEditorBase extends BookingEditorBase {

    protected final KnownItemFamily knownItemFamily;

    public FamilyBookingEditorBase(WorkingBooking workingBooking, KnownItemFamily knownItemFamily) {
        super(workingBooking);
        this.knownItemFamily = knownItemFamily;
        // Final subclasses should call this method
        // initiateUiAndSyncFromWorkingBooking();
    }

    // These methods are not directly used by this class but are provided to be used by the subclasses.

    protected List<ScheduledItem> getPolicyFamilyScheduledItems() {
        return workingBooking.getPolicyAggregate().getFamilyScheduledItems(knownItemFamily);
    }

    protected List<ScheduledItem> getAlreadyBookedFamilyScheduledItems() {
        return getAlreadyBookedFamilyScheduledItems(knownItemFamily);
    }

    protected List<ScheduledItem> getAlreadyBookedFamilyScheduledItems(KnownItemFamily knownItemFamily) {
        return workingBooking.getAlreadyBookedFamilyScheduledItems(knownItemFamily);
    }

    protected List<ScheduledItem> getBookedFamilyScheduledItems(KnownItemFamily knownItemFamily) {
        // TODO: this logic should be in the WorkingBooking class
        List<Attendance> attendancesAdded = workingBooking.getAttendancesAdded(false);
        List<Attendance> attendancesRemoved = workingBooking.getAttendancesRemoved(false);
        List<Attendance> bookedAttendances = Collections.filter(attendancesAdded, attendance -> !attendancesRemoved.contains(attendance));
        List<ScheduledItem> bookedScheduledItems = Collections.map(bookedAttendances, Attendance::getScheduledItem);
        return Collections.filter(bookedScheduledItems, scheduledItem -> Entities.samePrimaryKey(scheduledItem.getItem().getFamily(), knownItemFamily.getPrimaryKey()));
    }

    protected List<LocalDate> getAlreadyBookedFamilyDates() {
        return getAlreadyBookedFamilyDates(knownItemFamily);
    }

    protected List<LocalDate> getAlreadyBookedFamilyDates(KnownItemFamily knownItemFamily) {
        return Collections.map(getAlreadyBookedFamilyScheduledItems(knownItemFamily), ScheduledItem::getDate);
    }

    public KnownItemFamily getFamily() {
        return knownItemFamily;
    }
}
