package one.modality.event.frontoffice.bookingform.recurringevent;

import one.modality.base.shared.entities.Event;
import one.modality.booking.client.scheduleditemsselector.WorkingBookingSyncer;
import one.modality.booking.client.selecteditemsselector.box.BoxScheduledItemsSelector;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;
import one.modality.booking.frontoffice.bookingform.multipages.BookingFormPage;
import one.modality.booking.frontoffice.bookingform.multipages.MultiPageBookingForm;

/**
 * @author Bruno Salmon
 */
public final class RecurringEventBookingForm extends MultiPageBookingForm {

    private final Event event;
    private final EventBookingFormSettings settings;
    private final BookEventActivity activity;
    private final BoxScheduledItemsSelector boxScheduledItemsSelector = new BoxScheduledItemsSelector(false, true);
    private BookingFormPage[] pages;
    private RecurringSchedulePage recurringSchedulePage;

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
                    recurringSchedulePage = new RecurringSchedulePage(this),
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
        return activity;
    }

    public BoxScheduledItemsSelector getScheduledItemBoxesSelector() {
        return boxScheduledItemsSelector;
    }

    @Override
    public void onWorkingBookingLoaded() {
        if (recurringSchedulePage != null) {
            // This call is required to sync the already booked dates (which changed because of the booking change)
            recurringSchedulePage.setWorkingBookingProperties(workingBookingProperties);
        }
        // Automatically booking the whole event if it's a new booking and partial event is not allowed
        if (getWorkingBooking().isNewBooking() && !settings.partialEventAllowed())
            bookWholeEvent();
        super.onWorkingBookingLoaded();
    }

    public void syncWorkingBookingFromScheduledItemsSelector() {
        WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(getWorkingBooking(), boxScheduledItemsSelector, true);
    }

    public void syncScheduledItemsSelectorFromWorkingBooking() {
        WorkingBookingSyncer.syncScheduledItemsSelectorFromWorkingBooking(boxScheduledItemsSelector, getWorkingBooking());
    }

}
