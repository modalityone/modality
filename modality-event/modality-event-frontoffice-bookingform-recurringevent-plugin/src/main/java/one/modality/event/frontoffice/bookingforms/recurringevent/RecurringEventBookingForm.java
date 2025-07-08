package one.modality.event.frontoffice.bookingforms.recurringevent;

import one.modality.base.shared.entities.Event;
import one.modality.event.client.booking.BookableDatesUi;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormSettings;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages.BookingFormPage;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages.MultiPageBookingForm;

/**
 * @author Bruno Salmon
 */
final class RecurringEventBookingForm extends MultiPageBookingForm {

    private final Event event;
    private final BookEventActivity bookEventActivity;
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private BookingFormPage[] pages;

    public RecurringEventBookingForm(Event event, BookEventActivity bookEventActivity, BookingFormSettings settings) {
        super(bookEventActivity, settings);
        this.event = event;
        this.bookEventActivity = bookEventActivity;
    }

    @Override
    protected BookingFormPage[] getPages() {
        if (pages == null) {
            pages = new BookingFormPage[] {
                new SchedulePage(event, bookEventActivity, recurringEventSchedule)
            };
        }
        return pages;
    }

    @Override
    public BookableDatesUi getBookableDatesUi() {
        return recurringEventSchedule;
    }

}
