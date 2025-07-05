package one.modality.event.frontoffice.bookingforms.recurringevent;

import javafx.scene.layout.Background;
import one.modality.base.client.brand.Brand;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.process.event.*;
import one.modality.event.frontoffice.eventheader.LocalEventHeader;

/**
 * @author Bruno Salmon
 */
public class RecurringEventBookingFormProvider implements BookingFormProvider {

    @Override
    public boolean acceptEvent(Event event) {
        return event.isRecurring();
    }

    @Override
    public int getPriority() {
        return MODALITY_PRIORITY;
    }

    @Override
    public BookingForm createBookingForm(Event event, BookEventActivity activity) {
        return new RecurringEventBookingForm(event, activity, new BookingFormSettingsBuilder()
            .setEventHeader(new LocalEventHeader())
            .setHeaderBackground(Background.fill(Brand.getBrandMainColor()))
            .setBookAsAGuestAllowed(true)
            .setPartialEventAllowed(true)
            .build()
        );
    }

}
