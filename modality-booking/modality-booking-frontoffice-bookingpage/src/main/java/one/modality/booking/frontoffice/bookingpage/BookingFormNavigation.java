package one.modality.booking.frontoffice.bookingpage;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

/**
 * @author Bruno Salmon
 */
public interface BookingFormNavigation {

    void setBookingForm(MultiPageBookingForm bookingForm);

    Node getView();

    void updateState();

    // New method to set arbitrary buttons
    default void setButtons(BookingFormButton... buttons) {
    }

    // Deprecated or Default methods for backward compatibility if needed,
    // but ideally we move to the new system.
    // For now, we keep getBackButton/getNextButton for the default implementation
    // but MultiPageBookingForm will need to handle both cases.
    // Actually, to make it "extremely modular", we should probably standardize on
    // setButtons.
    // But existing implementations (ButtonNavigation,
    // StandardBookingFormNavigation) use specific fields.
    // Let's keep the old ones for now to avoid breaking everything immediately,
    // but add the new one.

    default javafx.scene.control.ToggleButton getBackButton() {
        return null;
    }

    default javafx.scene.control.ToggleButton getNextButton() {
        return null;
    }
}
