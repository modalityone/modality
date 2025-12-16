package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

/**
 * Interface for the "Pending Payment" section of a booking form.
 * This section displays payment verification pending information when
 * payment requires manual confirmation (bank transfer, check, etc.).
 *
 * Design based on PaymentPending.jsx mockup.
 * Tone should be reassuring - "we've got this, you're all set"
 *
 * @author Claude
 * @see BookingFormSection
 */
public interface HasPendingPaymentSection extends BookingFormSection {

    /**
     * Types of pending payment methods.
     */
    enum PendingPaymentType {
        /** Manual bank transfer needs verification */
        BANK_TRANSFER,
        /** Card payment pending 3D Secure / bank auth */
        PENDING_AUTHORIZATION,
        /** Check payment needs to clear */
        CHECK_PAYMENT,
        /** Invoice payment pending receipt */
        INVOICE
    }

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the booking reference number to display.
     */
    void setBookingReference(String reference);

    /**
     * Sets the event name to display.
     */
    void setEventName(String name);

    /**
     * Sets the event date range.
     */
    void setEventDates(LocalDate start, LocalDate end);

    /**
     * Sets the guest name to display.
     */
    void setGuestName(String name);

    /**
     * Sets the guest email for confirmation message.
     */
    void setGuestEmail(String email);

    /**
     * Sets the total amount in cents.
     */
    void setTotalAmount(int amount);

    /**
     * Sets the pending payment type and estimated verification time.
     */
    void setPendingPaymentDetails(PendingPaymentType type, String estimatedTime);

    /**
     * Sets whether the user has an account (affects display of account info box and actions).
     */
    void setHasAccount(boolean hasAccount);

    /**
     * Sets the contact email to display in the contact section.
     */
    void setContactEmail(String email);

    /**
     * Sets the callback for when "View My Orders" is clicked.
     */
    void setOnViewOrders(Runnable callback);

    /**
     * Sets the callback for when "Return to Event" is clicked.
     */
    void setOnReturnToEvent(Runnable callback);

    /**
     * Sets the callback for when "Browse More Events" is clicked.
     */
    void setOnBrowseEvents(Runnable callback);
}
