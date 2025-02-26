package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public final class NoBookingForm extends AbstractBookingForm {

    public NoBookingForm(BookEventActivity activity, boolean bookAsAGuestAllowed, boolean partialEventAllowed) {
        super(activity, bookAsAGuestAllowed, partialEventAllowed);
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
