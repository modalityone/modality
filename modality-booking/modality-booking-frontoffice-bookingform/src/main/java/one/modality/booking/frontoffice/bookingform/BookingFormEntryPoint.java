package one.modality.booking.frontoffice.bookingform;

/**
 * Defines the entry point context for creating a booking form.
 * This allows a single BookingFormProvider to handle different booking scenarios.
 *
 * @author Bruno Salmon
 */
public enum BookingFormEntryPoint {

    /**
     * User is creating a new booking from scratch.
     * This is the default entry point for most booking flows.
     */
    NEW_BOOKING,

    /**
     * User is modifying an existing booking (e.g., adding options like audio recordings).
     * The working booking will already contain the existing booking data.
     */
    MODIFY_BOOKING,

    /**
     * User is returning to complete a pending payment.
     * The working booking will contain the partially completed booking.
     */
    RESUME_PAYMENT
}
