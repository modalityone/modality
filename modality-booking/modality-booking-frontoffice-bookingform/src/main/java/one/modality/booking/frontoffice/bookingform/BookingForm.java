package one.modality.booking.frontoffice.bookingform;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingForm {

    BookingFormSettings getSettings();

    Node buildUi();

    /**
     * Returns the main view node for this booking form.
     * This is a convenience method that defaults to {@link #buildUi()}.
     *
     * <p>Forms that manage their own view lifecycle (like modification forms)
     * can override this to return a cached view instance.</p>
     *
     * @return the form's view node
     */
    default Node getView() {
        return buildUi();
    }

    default String getEventFieldsToLoad() {
        return null;
    }

    void onWorkingBookingLoaded();

    default ObservableBooleanValue transitingProperty() {
        return new SimpleBooleanProperty(false);
    }

    void setActivityCallback(BookingFormActivityCallback activityCallback);

    BookingFormActivityCallback getActivityCallback();

    /**
     * Called when a guest has submitted their information.
     * Multi-page forms can override this to navigate to the review page.
     * Default implementation submits the booking directly.
     */
    default void onGuestSubmitted() {
        BookingFormActivityCallback callback = getActivityCallback();
        if (callback != null) {
            callback.submitBooking(0); // Default behavior: submit directly
        }
    }

    /**
     * Returns true if this form supports a modification view for adding options
     * to existing bookings. Forms that return true should also implement
     * {@link #getModificationView()}.
     *
     * @return true if modification view is supported
     */
    default boolean supportsModificationView() {
        return false;
    }

    /**
     * Returns the modification view for adding options to an existing booking.
     * This view is used when the user is modifying an existing booking (not a new
     * booking and not a payment request).
     *
     * @return The modification view, or null if not supported
     */
    default Node getModificationView() {
        return null;
    }

    /**
     * Returns the sticky header node if this form has one, or null otherwise.
     * The sticky header should be added to the main frame overlay area
     * (e.g., FXMainFrameOverlayArea.getOverlayChildren()) by the calling code.
     *
     * @return The sticky header node, or null if none
     */
    default Node getStickyHeader() {
        return null;
    }

}
