package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.async.Future;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import one.modality.base.shared.entities.Document;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for the "Payment" section of a booking form.
 * This section handles payment option selection, payment method selection,
 * and terms acceptance before processing payment.
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasPaymentSection extends BookingFormSection {

    /**
     * Payment amount options.
     */
    enum PaymentOption {
        DEPOSIT("Minimum Deposit", "10% deposit required"),
        CUSTOM("Custom Amount", "Choose your amount"),
        FULL("Pay in Full", "Complete payment");

        private final String title;
        private final String description;

        PaymentOption(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    /**
     * Available payment methods.
     */
    enum PaymentMethod {
        CARD, PAYPAL, BANK
    }

    /**
     * Represents a booking item displayed in the payment summary.
     */
    class PaymentBookingItem {
        private final Document document;
        private final String personName;
        private final String details;
        private final int amount;

        /**
         * Creates a booking item with a specific ID.
         */
        public PaymentBookingItem(Document document, String personName, String details, int amount) {
            this.document = document;
            this.personName = personName;
            this.details = details;
            this.amount = amount;
        }


        public Document getDocument() { return document; }
        public String getPersonName() { return personName; }
        public String getDetails() { return details; }
        public int getAmount() { return amount; }
        Object getDocumentPrimaryKey() { return document.getPrimaryKey(); }
    }

    /**
     * Result of a payment submission.
     */
    class PaymentResult {
        private final PaymentOption paymentOption;
        private final PaymentMethod paymentMethod;
        private final int amount;
        private final Map<Object, Integer> allocations;

        public PaymentResult(PaymentOption paymentOption, PaymentMethod paymentMethod, int amount, Map<Object, Integer> allocations) {
            this.paymentOption = paymentOption;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
            this.allocations = allocations;
        }

        public PaymentOption getPaymentOption() { return paymentOption; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public int getAmount() { return amount; }
        public Map<Object, Integer> getAllocations() { return allocations; }
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
     * Sets the total amount to be paid.
     */
    void setTotalAmount(int amount);

    /**
     * Returns the total amount.
     */
    int getTotalAmount();

    /**
     * Sets the minimum deposit amount.
     */
    void setDepositAmount(int amount);

    /**
     * Sets the amount already paid (for display in booking summary).
     * When set to a value > 0, a "Payments Made" row will be displayed
     * in the booking summary section showing this amount.
     * @param amount the amount already paid in cents
     */
    void setPaymentsMade(int amount);

    /**
     * Returns the amount already paid.
     */
    int getPaymentsMade();

    /**
     * Adds a booking item to the payment summary.
     */
    void addBookingItem(PaymentBookingItem item);

    /**
     * Sets all booking items in the payment summary.
     */
    void setBookingItems(List<PaymentBookingItem> items);

    /**
     * Clears all booking items.
     */
    void clearBookingItems();

    /**
     * Returns the selected payment option.
     */
    PaymentOption getSelectedPaymentOption();

    /**
     * Returns the selected payment method.
     */
    PaymentMethod getSelectedPaymentMethod();

    /**
     * Returns the amount to be charged based on selected option.
     */
    int getPaymentAmount();

    // Note: Terms acceptance is now validated on the Pending Bookings page (DefaultTermsSection)

    /**
     * Sets the callback for when payment is submitted.
     */
    void setOnPaymentSubmit(AsyncFunction<PaymentResult, Void> callback);

    /**
     * Sets the callback for when back is pressed.
     */
    void setOnBackPressed(Runnable callback);

    /**
     * Submits the payment asynchronously.
     * @return Future containing the PaymentResult when complete
     */
    Future<PaymentResult> submitPaymentAsync();

    /**
     * Returns a property that indicates when the pay button should be disabled.
     * This is typically bound to terms acceptance and processing state.
     */
    ObservableBooleanValue payButtonDisabledProperty();

    /**
     * Returns a string property containing the formatted pay button text.
     * Text format: "Pay £{amount} Now →" (or equivalent with current currency symbol).
     * Updates automatically when payment amount changes.
     */
    javafx.beans.property.StringProperty payButtonTextProperty();

    /**
     * Sets the processing state (disables UI during payment processing).
     */
    void setProcessing(boolean processing);

    /**
     * Returns the current payment allocations by booking item ID.
     * @return map of item ID to allocated amount
     */
    Map<Object, Integer> getAllocations();

    /**
     * Sets the allocation amount for a specific booking item.
     * @param itemId the booking item ID
     * @param amount the amount to allocate
     */
    void setAllocation(Object itemId, int amount);
    void setAllocation(String itemId, double amount);

    /**
     * Sets which payment options are available for selection.
     * By default, all options (DEPOSIT, CUSTOM, FULL) are available.
     * @param options The set of payment options to show (e.g., EnumSet.of(PaymentOption.FULL))
     */
    void setAvailablePaymentOptions(Set<PaymentOption> options);

    /**
     * Returns the currently available payment options.
     */
    Set<PaymentOption> getAvailablePaymentOptions();

    /**
     * Convenience method to allow only full payment (no deposit or custom options).
     * Equivalent to setAvailablePaymentOptions(EnumSet.of(PaymentOption.FULL))
     * @param fullPaymentOnly true to only allow full payment, false to allow all options
     */
    default void setFullPaymentOnly(boolean fullPaymentOnly) {
        if (fullPaymentOnly) {
            setAvailablePaymentOptions(EnumSet.of(PaymentOption.FULL));
        } else {
            setAvailablePaymentOptions(EnumSet.allOf(PaymentOption.class));
        }
    }

    /**
     * Sets which payment methods are available for selection.
     * By default, CARD and BANK are available (PAYPAL is not currently implemented).
     * @param methods The set of payment methods to show (e.g., EnumSet.of(PaymentMethod.CARD))
     */
    void setAvailablePaymentMethods(Set<PaymentMethod> methods);

    /**
     * Returns the currently available payment methods.
     */
    Set<PaymentMethod> getAvailablePaymentMethods();

    /**
     * Convenience method to allow only card payment.
     * Equivalent to setAvailablePaymentMethods(EnumSet.of(PaymentMethod.CARD))
     * @param cardOnly true to only allow card payment, false to allow all methods
     */
    default void setCardPaymentOnly(boolean cardOnly) {
        if (cardOnly) {
            setAvailablePaymentMethods(EnumSet.of(PaymentMethod.CARD));
        } else {
            setAvailablePaymentMethods(EnumSet.of(PaymentMethod.CARD, PaymentMethod.BANK));
        }
    }
}
