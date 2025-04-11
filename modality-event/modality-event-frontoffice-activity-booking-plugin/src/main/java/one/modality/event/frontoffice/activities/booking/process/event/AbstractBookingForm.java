package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.Node;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.event.client.booking.BookableDatesUi;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractBookingForm implements BookingForm {

    protected final BookEventActivity activity;
    protected final boolean bookAsAGuestAllowed;
    protected final boolean partialEventAllowed;

    public AbstractBookingForm(BookEventActivity activity, boolean bookAsAGuestAllowed, boolean partialEventAllowed) {
        this.activity = activity;
        this.bookAsAGuestAllowed = bookAsAGuestAllowed;
        this.partialEventAllowed = partialEventAllowed;
    }

    @Override
    public boolean isBookAsAGuestAllowed() {
        return bookAsAGuestAllowed;
    }

    @Override
    public boolean isPartialEventAllowed() {
        return partialEventAllowed;
    }

    @Override
    public abstract Node buildUi();

    @Override
    public abstract void onWorkingBookingLoaded();

    protected void bookWholeEvent() {
        WorkingBookingProperties workingBookingProperties = activity.getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        // Automatically booking the whole event if it's a new booking
        if (workingBooking.isNewBooking()) {
            workingBooking.bookScheduledItems(workingBooking.getPolicyAggregate().getTeachingScheduledItems(), false);
        }
    }

    @Override
    public BookableDatesUi getBookableDatesUi() {
        return null;
    }
}
