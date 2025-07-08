package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
public interface BookingFormProvider {

    int MODALITY_PRIORITY = 0;
    int APP_PRIORITY = 10;

    boolean acceptEvent(Event event);

    int getPriority(); // The highest priority is used to decide which provider to choose when several accept the event

    BookingForm createBookingForm(Event event, BookEventActivity activity);

}
