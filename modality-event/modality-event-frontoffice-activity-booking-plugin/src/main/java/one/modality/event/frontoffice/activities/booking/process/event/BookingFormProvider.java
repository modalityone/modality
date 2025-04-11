package one.modality.event.frontoffice.activities.booking.process.event;

import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public interface BookingFormProvider {

    int MODALITY_PRIORITY = 0;
    int APP_PRIORITY = 10;

    int getPriority();

    boolean acceptEvent(Event event);

    BookingForm createBookingForm(Event event, BookEventActivity activity);

}
