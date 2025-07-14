package one.modality.event.frontoffice.bookingforms.recurringevent;

import one.modality.base.shared.entities.Event;
import one.modality.event.client.booking.WorkingBookingSyncer;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.EventBookingFormSettings;
import one.modality.ecommerce.frontoffice.bookingform.multipages.BookingFormPage;
import one.modality.ecommerce.frontoffice.bookingform.multipages.MultiPageBookingForm;

/**
 * @author Bruno Salmon
 */
public final class RecurringEventBookingForm extends MultiPageBookingForm {

    private final Event event;
    private final EventBookingFormSettings settings;
    private final BookEventActivity activity;
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private BookingFormPage[] pages;

    public RecurringEventBookingForm(Event event, BookEventActivity activity, EventBookingFormSettings settings) {
        super(activity, settings);
        this.event = event;
        this.settings = settings;
        this.activity = activity;
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

    public BookEventActivity getActivity() {
        return (BookEventActivity) activity;
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
