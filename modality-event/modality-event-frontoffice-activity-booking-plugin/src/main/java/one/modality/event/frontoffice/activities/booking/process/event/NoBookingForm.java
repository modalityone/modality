package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public final class NoBookingForm extends AbstractBookingForm {

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
