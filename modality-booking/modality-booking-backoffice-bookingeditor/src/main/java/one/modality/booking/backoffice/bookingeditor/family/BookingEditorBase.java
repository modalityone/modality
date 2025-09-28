package one.modality.booking.backoffice.bookingeditor.family;

import dev.webfx.platform.async.Future;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingHistoryHelper;

/**
 * @author Bruno Salmon
 */
public abstract class BookingEditorBase implements BookingEditor {

    protected final WorkingBooking workingBooking;

    protected BookingEditorBase(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        // Final subclasses should call this method
        // initiateUiAndSyncFromWorkingBooking();
    }

    protected abstract void initiateUiAndSyncFromWorkingBooking();

    public abstract void syncWorkingBookingFromUi();

    @Override
    public Future<Void> saveChanges() {
        syncWorkingBookingFromUi();
        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
        return workingBooking.submitChanges(historyHelper.generateHistoryComment())
            .mapEmpty();
    }

}
