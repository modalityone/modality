package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Interface for the "Roommate Information" section of a booking form.
 * This section collects information about the person whose room the user is sharing.
 *
 * <p>Used when a user selects "Share Accommodation" option, meaning they will share
 * a room with someone else who is booking the accommodation.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Text field for roommate's full name (mandatory)</li>
 *   <li>Text field for roommate's registration number (optional)</li>
 *   <li>Validation requiring name to be provided</li>
 *   <li>Visibility control based on accommodation selection</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasRoommateInfoSection extends BookingFormSection {

    // === Configuration ===

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    // === Visibility ===

    /**
     * Property controlling whether this section is visible.
     * When not visible, the section should be considered valid
     * to avoid blocking navigation.
     */
    BooleanProperty visibleProperty();

    /**
     * Sets whether this section is visible.
     * When set to false, the section UI is hidden and validation is bypassed.
     */
    void setVisible(boolean visible);

    /**
     * Returns whether this section is currently visible.
     */
    boolean isVisible();

    // === Data Properties ===

    /**
     * Property for the roommate's full name.
     * This is a mandatory field - the section is invalid until a name is provided.
     */
    StringProperty roommateNameProperty();

    /**
     * Returns the roommate's full name.
     */
    default String getRoommateName() {
        return roommateNameProperty().get();
    }

    /**
     * Sets the roommate's full name.
     */
    default void setRoommateName(String name) {
        roommateNameProperty().set(name);
    }

    /**
     * Property for the roommate's registration number.
     * This is an optional field.
     */
    StringProperty registrationNumberProperty();

    /**
     * Returns the roommate's registration number.
     */
    default String getRegistrationNumber() {
        return registrationNumberProperty().get();
    }

    /**
     * Sets the roommate's registration number.
     */
    default void setRegistrationNumber(String number) {
        registrationNumberProperty().set(number);
    }

    // === Validation ===

    /**
     * Returns a validation message if the section is invalid, or null if valid.
     * Used by ValidationWarningZone to display aggregated validation messages.
     */
    String getValidationMessage();

    /**
     * Resets the section to its initial state.
     * Clears all fields and resets validation.
     */
    void reset();
}
