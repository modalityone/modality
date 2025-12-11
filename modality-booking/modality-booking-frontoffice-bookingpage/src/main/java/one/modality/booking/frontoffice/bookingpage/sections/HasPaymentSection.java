package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.platform.async.Future;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import dev.webfx.platform.util.uuid.Uuid;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
        private final String id;
        private final String personName;
        private final String details;
        private final double amount;

        /**
         * Creates a booking item with a specific ID.
         */
        public PaymentBookingItem(String id, String personName, String details, double amount) {
            this.id = id;
            this.personName = personName;
            this.details = details;
            this.amount = amount;
        }

        /**
         * Creates a booking item with an auto-generated ID.
         */
        public PaymentBookingItem(String personName, String details, double amount) {
            this(Uuid.randomUuid(), personName, details, amount);
        }

        public String getId() { return id; }
        public String getPersonName() { return personName; }
        public String getDetails() { return details; }
        public double getAmount() { return amount; }
    }

    /**
     * Result of a payment submission.
     */
    class PaymentResult {
        private final PaymentOption paymentOption;
        private final PaymentMethod paymentMethod;
        private final double amount;
        private final Map<String, Double> allocations;

        public PaymentResult(PaymentOption paymentOption, PaymentMethod paymentMethod, double amount, Map<String, Double> allocations) {
            this.paymentOption = paymentOption;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
            this.allocations = allocations;
        }

        public PaymentOption getPaymentOption() { return paymentOption; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public double getAmount() { return amount; }
        public Map<String, Double> getAllocations() { return allocations; }
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
     * Sets the currency symbol for price display.
     */
    void setCurrencySymbol(String symbol);

    /**
     * Sets the total amount to be paid.
     */
    void setTotalAmount(double amount);

    /**
     * Returns the total amount.
     */
    double getTotalAmount();

    /**
     * Sets the minimum deposit amount.
     */
    void setDepositAmount(double amount);

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
    double getPaymentAmount();

    /**
     * Returns whether the terms and conditions have been accepted.
     */
    boolean isTermsAccepted();

    /**
     * Sets the callback for when payment is submitted.
     */
    void setOnPaymentSubmit(Consumer<PaymentResult> callback);

    /**
     * Sets the callback for when back is pressed.
     */
    void setOnBackPressed(Runnable callback);

    /**
     * Submits the payment synchronously.
     */
    void submitPayment();

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
    Map<String, Double> getAllocations();

    /**
     * Sets the allocation amount for a specific booking item.
     * @param itemId the booking item ID
     * @param amount the amount to allocate
     */
    void setAllocation(String itemId, double amount);
}
