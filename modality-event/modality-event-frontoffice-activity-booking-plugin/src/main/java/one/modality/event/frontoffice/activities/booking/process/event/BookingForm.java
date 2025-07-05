package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.Node;
import one.modality.event.client.booking.BookableDatesUi;

/**
 * @author Bruno Salmon
 */
public interface BookingForm {

    BookingFormSettings getSettings();

    Node buildUi();

    void onWorkingBookingLoaded();

    BookableDatesUi getBookableDatesUi();
}
