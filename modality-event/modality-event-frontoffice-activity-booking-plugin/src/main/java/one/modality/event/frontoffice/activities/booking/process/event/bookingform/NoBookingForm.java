package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.scene.Node;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
public final class NoBookingForm extends BookingFormBase {

    public NoBookingForm(BookEventActivity activity, BookingFormSettings settings) {
        super(activity, settings);
    }

    @Override
    public Node buildUi() {
        return null;
    }

    @Override
    public void onWorkingBookingLoaded() {
        bookWholeEvent();
    }
}
