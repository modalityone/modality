package one.modality.booking.frontoffice.bookingpage.sections.roommate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.List;

/**
 * Interface for the "Roommate Information" section of a booking form.
 * This section supports two use cases:
 *
 * <h3>1. Share Accommodation (isRoomBooker = false)</h3>
 * <p>Used when a user selects "Share Accommodation" option, meaning they will share
 * a room with someone else who is booking the accommodation. Shows a single field
 * for the room booker's name.</p>
 *
 * <h3>2. Room Booking (isRoomBooker = true)</h3>
 * <p>Used when a user books a room with capacity > 1 (e.g., double room, triple room).
 * Shows multiple fields for roommate names/booking references. The number of fields
 * is (capacity - 1) since the current user counts as one occupant.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Dynamic number of text fields based on room capacity</li>
 *   <li>Configurable mandatory fields based on minimum occupancy</li>
 *   <li>Info text explaining the booking process for room bookers</li>
 *   <li>Validation requiring mandatory fields to be filled</li>
 *   <li>Visibility control based on accommodation selection</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasRoommateInfoSection extends BookingFormSection, ResettableSection {

    // === Configuration ===

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    // === Room Booking Configuration ===

    /**
     * Property for the room capacity.
     * Used to determine how many roommate fields to display (capacity - 1).
     */
    IntegerProperty roomCapacityProperty();

    /**
     * Sets the room capacity. The section will display (capacity - 1) roommate fields.
     * @param capacity the total room capacity (e.g., 2 for double room)
     */
    void setRoomCapacity(int capacity);

    /**
     * Returns the room capacity.
     */
    default int getRoomCapacity() {
        return roomCapacityProperty().get();
    }

    /**
     * Property for the minimum occupancy.
     * Used to determine how many roommate fields are mandatory (minOccupancy - 1).
     */
    IntegerProperty minOccupancyProperty();

    /**
     * Sets the minimum occupancy. The first (minOccupancy - 1) fields will be mandatory.
     * @param minOccupancy the minimum number of people required (e.g., 2 means 1 roommate is required)
     */
    void setMinOccupancy(int minOccupancy);

    /**
     * Returns the minimum occupancy.
     */
    default int getMinOccupancy() {
        return minOccupancyProperty().get();
    }

    /**
     * Property indicating whether the user is the room booker (true) or sharing someone else's room (false).
     * When true: shows multiple roommate fields with info text about separate registrations.
     * When false: shows single field for the room booker's name (Share Accommodation scenario).
     */
    BooleanProperty isRoomBookerProperty();

    /**
     * Sets whether the user is the room booker.
     * @param isRoomBooker true if booking the room, false if sharing someone else's room
     */
    void setIsRoomBooker(boolean isRoomBooker);

    /**
     * Returns whether the user is the room booker.
     */
    default boolean isRoomBooker() {
        return isRoomBookerProperty().get();
    }

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

    // === Data Retrieval ===

    /**
     * Returns all roommate names/references entered in the section.
     * For Share Accommodation: returns a single-element list with the roommate's name.
     * For Room Booking: returns a list with all entered roommate names (non-empty values only).
     */
    List<String> getAllRoommateNames();

    // === Selection State Binding ===

    /**
     * Binds this section to a BookingSelectionState.
     * When bound, the section will push roommate info changes to the state.
     *
     * @param selectionState the selection state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        if (selectionState != null) {
            // Push isRoomBooker changes
            isRoomBookerProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setIsRoomBooker(newVal);
            });
            // Push roommate name changes (for share scenario)
            roommateNameProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setShareRoommateName(newVal);
            });
            // Initialize state from current values
            selectionState.setIsRoomBooker(isRoomBooker());
            if (getRoommateName() != null) {
                selectionState.setShareRoommateName(getRoommateName());
            }
        }
    }

    /**
     * Pushes current roommate names to the selection state.
     * Should be called after roommate fields are updated (for room booker scenario).
     *
     * @param selectionState the selection state to update
     */
    default void pushRoommateNamesToState(BookingSelectionState selectionState) {
        if (selectionState != null) {
            selectionState.setRoommateNames(getAllRoommateNames());
        }
    }
}
