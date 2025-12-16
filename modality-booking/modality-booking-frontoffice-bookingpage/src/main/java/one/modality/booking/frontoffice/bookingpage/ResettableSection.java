package one.modality.booking.frontoffice.bookingpage;

/**
 * Interface for booking form sections that can be reset to their initial state.
 * Implement this interface to have the section automatically reset when
 * registering another person.
 *
 * @author Bruno Salmon
 */
public interface ResettableSection {

    /**
     * Resets this section to its initial state.
     * Called when preparing to register another person.
     */
    void reset();
}
