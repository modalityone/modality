package one.modality.booking.backoffice.bookingeditor.family;

import javafx.scene.Node;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.policy.service.PolicyAggregate;

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
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        return policyAggregate.filterScheduledItemsOfFamily(knownItemFamily);
    }

    protected List<Attendance> getBookedAttendances() {
        return workingBooking.getBookedAttendances();
    }

    protected List<ScheduledItem> getBookedScheduledItems() {
        return ScheduledItems.fromAttendances(getBookedAttendances());
    }

    protected List<ScheduledItem> getBookedFamilyScheduledItems(KnownItemFamily family) {
        return ScheduledItems.filterFamily(getBookedScheduledItems(), family);
    }

    protected List<ScheduledItem> getBookedFamilyScheduledItems() {
        return getBookedFamilyScheduledItems(getFamily());
    }

    protected List<ScheduledItem> getBookedTeachingScheduledItems() {
        return getBookedFamilyScheduledItems(KnownItemFamily.TEACHING);
    }

    protected List<LocalDate> getBookedTeachingDates() {
        return ScheduledItems.toDates(getBookedTeachingScheduledItems());
    }

    protected Node embedInFamilyFrame(Node content) {
        return embedInFrame(content, getFamily().getI18nKey());
    }

    public KnownItemFamily getFamily() {
        return knownItemFamily;
    }
}
