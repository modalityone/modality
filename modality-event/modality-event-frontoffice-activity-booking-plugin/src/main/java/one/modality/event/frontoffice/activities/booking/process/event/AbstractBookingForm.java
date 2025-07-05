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
    protected final BookingFormSettings settings;

    public AbstractBookingForm(BookEventActivity activity, BookingFormSettings settings) {
        this.activity = activity;
        this.settings = settings;
    }

    @Override
    public BookingFormSettings getSettings() {
        return settings;
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
