package one.modality.booking.frontoffice.bookingpage.sections.payment;

import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

/**
 * Interface for the "Failed Payment" section of a booking form.
 * This section displays payment failure information, error details,
 * and provides options to retry or cancel the booking.
 *
 * @author Claude
 * @see BookingFormSection
 */
public interface HasFailedPaymentSection extends BookingFormSection {

    /**
     * Types of payment errors that can be displayed.
     */
    enum PaymentErrorType {
        /** Card was declined by the bank */
        CARD_DECLINED,
        /** Insufficient funds in account */
        INSUFFICIENT_FUNDS,
        /** Technical processing error */
        PROCESSING_ERROR,
        /** Payment request timed out */
        TIMEOUT,
        /** Card has expired */
        EXPIRED_CARD,
        /** Generic unknown error */
        UNKNOWN
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
     * Sets the amount due in cents.
     */
    void setAmountDue(int amount);

    /**
     * Sets the error type and optional bank message.
     */
    void setErrorDetails(PaymentErrorType errorType, String bankErrorMessage, String errorCode);

    /**
     * Sets whether the user has an account (affects display of account info box).
     */
    void setHasAccount(boolean hasAccount);

    /**
     * Sets the contact email to display in the footer.
     */
    void setContactEmail(String email);

    /**
     * Sets the callback for when "Retry Payment" is clicked.
     */
    void setOnRetryPayment(Runnable callback);

    /**
     * Sets the callback for when "Cancel Booking" is clicked.
     */
    void setOnCancelBooking(Runnable callback);

    /**
     * Sets the callback for when "Go to My Orders" is clicked.
     */
    void setOnGoToOrders(Runnable callback);
}
