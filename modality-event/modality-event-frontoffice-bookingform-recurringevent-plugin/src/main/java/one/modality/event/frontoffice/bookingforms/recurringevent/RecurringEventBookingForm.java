package one.modality.event.frontoffice.bookingforms.recurringevent;

import one.modality.base.shared.entities.Event;
import one.modality.event.client.booking.WorkingBookingSyncer;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormSettings;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages.BookingFormPage;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages.MultiPageBookingForm;

/**
 * @author Bruno Salmon
 */
public final class RecurringEventBookingForm extends MultiPageBookingForm {

    private final Event event;
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private BookingFormPage[] pages;

    public RecurringEventBookingForm(Event event, BookEventActivity bookEventActivity, BookingFormSettings settings) {
        super(bookEventActivity, settings);
        this.event = event;
    }

    @Override
    protected BookingFormPage[] getPages() {
        if (pages == null) {
            if (settings.partialEventAllowed()) {
                pages = new BookingFormPage[]{
                    new SchedulePage(this),
                    new RecurringSummaryPage(this)
                };
            } else {
                pages = new BookingFormPage[]{
                    new RecurringSummaryPage(this)
                };
            }

        }
        return pages;
    }

    public Event getEvent() {
        return event;
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return recurringEventSchedule;
    }

    public void syncWorkingBookingFromEventSchedule() {
        WorkingBookingSyncer.syncWorkingBookingFromEventSchedule(activity.getWorkingBooking(), recurringEventSchedule, true);
    }

    public void syncEventScheduleFromWorkingBooking() {
        WorkingBookingSyncer.syncEventScheduleFromWorkingBooking(activity.getWorkingBooking(), recurringEventSchedule);
    }

}
