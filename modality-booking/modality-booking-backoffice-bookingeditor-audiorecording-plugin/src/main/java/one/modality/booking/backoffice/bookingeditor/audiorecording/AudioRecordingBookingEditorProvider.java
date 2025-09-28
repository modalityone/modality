package one.modality.booking.backoffice.bookingeditor.audiorecording;

import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;
import one.modality.booking.client.workingbooking.WorkingBooking;

/**
 * @author Bruno Salmon
 */
public final class AudioRecordingBookingEditorProvider implements BookingEditorProvider {

    @Override
    public boolean acceptBooking(WorkingBooking workingBooking) {
        return true;
    }

    @Override
    public BookingEditor createBookingEditor(WorkingBooking workingBooking) {
        return new AudioRecordingBookingEditor(workingBooking);
    }
}
