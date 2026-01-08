package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Interface for the "Additional Options" section of a booking form.
 * This section allows selection of additional services like parking, shuttle, and accessibility options.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Assisted listening checkbox (typically free)</li>
 *   <li>Parking checkbox with price per day</li>
 *   <li>Parking type selection (standard/handicap)</li>
 *   <li>Shuttle/transport checkboxes (outbound/return)</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasAdditionalOptionsSection extends BookingFormSection {

    /**
     * Parking type options.
     */
    enum ParkingType {
        STANDARD,
        HANDICAP
    }

    // === Configuration ===

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the parking price per day.
     */
    void setParkingPricePerDay(int price);

    /**
     * Sets the shuttle price (one-way).
     */
    void setShuttlePrice(int price);

    /**
     * Sets the number of days for the booking (for calculating totals).
     */
    void setDaysCount(int count);

    // === Assisted Listening ===

    /**
     * Property for assisted listening device request.
     */
    BooleanProperty assistedListeningProperty();

    /**
     * Gets whether assisted listening is requested.
     */
    default boolean needsAssistedListening() {
        return assistedListeningProperty().get();
    }

    /**
     * Sets whether assisted listening is requested.
     */
    default void setNeedsAssistedListening(boolean needs) {
        assistedListeningProperty().set(needs);
    }

    // === Parking ===

    /**
     * Property for parking request.
     */
    BooleanProperty needsParkingProperty();

    /**
     * Gets whether parking is requested.
     */
    default boolean needsParking() {
        return needsParkingProperty().get();
    }

    /**
     * Sets whether parking is requested.
     */
    default void setNeedsParking(boolean needs) {
        needsParkingProperty().set(needs);
    }

    /**
     * Property for parking type.
     */
    ObjectProperty<ParkingType> parkingTypeProperty();

    /**
     * Gets the parking type.
     */
    default ParkingType getParkingType() {
        return parkingTypeProperty().get();
    }

    /**
     * Sets the parking type.
     */
    default void setParkingType(ParkingType type) {
        parkingTypeProperty().set(type);
    }

    // === Shuttle/Transport ===

    /**
     * Property for outbound shuttle request (to the event).
     */
    BooleanProperty shuttleOutboundProperty();

    /**
     * Gets whether outbound shuttle is requested.
     */
    default boolean needsShuttleOutbound() {
        return shuttleOutboundProperty().get();
    }

    /**
     * Sets whether outbound shuttle is requested.
     */
    default void setNeedsShuttleOutbound(boolean needs) {
        shuttleOutboundProperty().set(needs);
    }

    /**
     * Property for return shuttle request (from the event).
     */
    BooleanProperty shuttleReturnProperty();

    /**
     * Gets whether return shuttle is requested.
     */
    default boolean needsShuttleReturn() {
        return shuttleReturnProperty().get();
    }

    /**
     * Sets whether return shuttle is requested.
     */
    default void setNeedsShuttleReturn(boolean needs) {
        shuttleReturnProperty().set(needs);
    }

    // === Calculated Values ===

    /**
     * Returns the total parking cost based on days count.
     */
    int getTotalParkingCost();

    /**
     * Returns the total shuttle cost based on selections.
     */
    int getTotalShuttleCost();

    /**
     * Returns the total additional options cost.
     */
    default int getTotalAdditionalOptionsCost() {
        return getTotalParkingCost() + getTotalShuttleCost();
    }
}
