package one.modality.booking.frontoffice.bookingform;

import javafx.scene.Node;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.ecommerce.policy.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public abstract class BookingFormBase implements BookingForm {

    protected HasWorkingBookingProperties activity;
    protected WorkingBookingProperties workingBookingProperties;
    protected final BookingFormSettings settings;
    protected BookingFormActivityCallback activityCallback;

    public BookingFormBase(HasWorkingBookingProperties activity, BookingFormSettings settings) {
        this.activity = activity;
        this.workingBookingProperties = activity.getWorkingBookingProperties();
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

    public void bookWholeEvent() {
        WorkingBooking workingBooking = getWorkingBooking();
        // Automatically booking the whole event if it's a new booking
        if (workingBooking.isNewBooking()) {
            PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
            workingBooking.bookScheduledItems(policyAggregate.filterTeachingScheduledItems(), false);
        }
    }

    public HasWorkingBookingProperties getActivity() {
        return activity;
    }

    public WorkingBookingProperties getWorkingBookingProperties() {
        return workingBookingProperties;
    }

    public WorkingBooking getWorkingBooking() {
        return workingBookingProperties.getWorkingBooking();
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
