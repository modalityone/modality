package one.modality.booking.backoffice.bookingeditor.multi;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.backoffice.bookingeditor.family.BookingEditorBase;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class MultiBookingEditor extends BookingEditorBase {

    private final List<BookingEditor> bookingEditors;

    public MultiBookingEditor(WorkingBooking workingBooking, List<BookingEditor> bookingEditors) {
        super(workingBooking);
        this.bookingEditors = bookingEditors;
        // Final subclasses should call this method
        initiateUiAndSyncFromWorkingBooking();
    }

    @Override
    protected void initiateUiAndSyncFromWorkingBooking() {
        // Already done in individual booking editors
    }

    @Override
    public void syncWorkingBookingFromUi() {
        bookingEditors.forEach(bookingEditor -> {
            if (bookingEditor instanceof BookingEditorBase beb) {
                beb.syncWorkingBookingFromUi();
            }
        });
    }

    @Override
    public Node buildUi() {
        return new VBox(20,
            bookingEditors.stream().map(BookingEditor::buildUi).toArray(Node[]::new)
        );
    }

}
