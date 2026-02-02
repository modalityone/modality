package one.modality.booking.frontoffice.bookingpage;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface BookingFormHeader {

    Node getView();

    void setBookingForm(MultiPageBookingForm bookingForm);

    void updateState();

    void setNavigationClickable(boolean clickable);

    /**
     * Forces a rebuild of the steps list.
     * Call this when conditions affecting page applicability have changed
     * (e.g., user login/logout state).
     */
    default void forceRebuildSteps() {
        // Default: no-op. Override in implementations that track steps.
    }
}
