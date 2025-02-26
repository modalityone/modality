package one.modality.event.frontoffice.bookingforms.recurringevent;

import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.BookingForm;
import one.modality.event.frontoffice.activities.booking.process.event.BookingFormProvider;
/**
 * @author Bruno Salmon
 */
public class RecurringEventBookingFormProvider implements BookingFormProvider {

    @Override
    public int getPriority() {
        return MODALITY_PRIORITY;
    }

    @Override
    public boolean acceptEvent(Event event) {
        return event.isRecurring();
    }

    @Override
    public BookingForm createBookingForm(Event event, BookEventActivity activity) {
        return new RecurringEventBookingForm(event, activity);
    }

}
