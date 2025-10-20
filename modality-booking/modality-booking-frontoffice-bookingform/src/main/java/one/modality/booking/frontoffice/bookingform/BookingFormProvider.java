package one.modality.booking.frontoffice.bookingform;

import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public interface BookingFormProvider {

    int MODALITY_PRIORITY = 0;
    int APP_PRIORITY = 10;

    boolean acceptEvent(Event event);

    int getPriority(); // The highest priority is used to decide which provider to choose when several accept the event

    BookingForm createBookingForm(Event event, HasWorkingBookingProperties activity);

}
