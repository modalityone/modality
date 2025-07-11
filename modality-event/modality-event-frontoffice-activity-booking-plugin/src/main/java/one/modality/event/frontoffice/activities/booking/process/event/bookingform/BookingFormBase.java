package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
public abstract class BookingFormBase implements BookingForm {

    protected final BookEventActivity activity;
    protected final BookingFormSettings settings;
    protected final BooleanProperty showLoginProperty = new SimpleBooleanProperty();
    protected final BooleanProperty showDefaultSubmitButtonProperty = new SimpleBooleanProperty();
    protected final BooleanProperty disableSubmitButtonProperty = new SimpleBooleanProperty();
    protected BookingFormActivityCallback activityCallback;

    public BookingFormBase(BookEventActivity activity, BookingFormSettings settings) {
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

    public BookEventActivity getActivity() {
        return activity;
    }

    @Override
    public ObservableBooleanValue showLoginProperty() {
        return showLoginProperty;
    }

    public void setShowLogin(boolean show) {
        showLoginProperty.set(show);
    }

    @Override
    public ObservableBooleanValue showDefaultSubmitButtonProperty() {
        return showDefaultSubmitButtonProperty;
    }

    public void setShowSubmitButton(boolean show) {
        showDefaultSubmitButtonProperty.set(show);
    }

    @Override
    public ObservableBooleanValue disableSubmitButtonProperty() {
        return disableSubmitButtonProperty;
    }

    @Override
    public BookingFormActivityCallback getActivityCallback() {
        return activityCallback;
    }

    @Override
    public void setActivityCallback(BookingFormActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }

}
