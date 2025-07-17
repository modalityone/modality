package one.modality.event.frontoffice.bookingform.recurringevent;

import javafx.scene.layout.Background;
import one.modality.base.client.brand.Brand;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.client.workingbooking.HasWorkingBookingProperties;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;
import one.modality.ecommerce.frontoffice.bookingform.BookingForm;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormProvider;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettingsBuilder;
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
    public BookingForm createBookingForm(Event event, HasWorkingBookingProperties activity) {
        return new RecurringEventBookingForm(event, (BookEventActivity) activity, new EventBookingFormSettingsBuilder()
            .setEventHeader(new LocalEventHeader())
            .setHeaderBackground(Background.fill(Brand.getBrandMainColor()))
            .setExtraSpaceBetweenHeaderAndBookingForm(0.03) // 3% of the booking form width
            .setBookAsAGuestAllowed(true)
            .setPartialEventAllowed(true)
            .setAutoLoadExistingBooking(true)
            .build()
        );
    }

}
