package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for the "Pending Bookings" (basket) section of a booking form.
 * This section displays all bookings in the cart before proceeding to payment.
 * It supports multiple bookings for the same event (e.g., family members).
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasPendingBookingsSection extends BookingFormSection {

    /**
     * Represents a line item within a booking.
     */
    class BookingLineItem {
        private final String name;
        private final double amount;
        private final boolean included;
        private final String familyCode;

        public BookingLineItem(String name, double amount, boolean included) {
            this(name, amount, included, null);
        }

        public BookingLineItem(String name, double amount, boolean included, String familyCode) {
            this.name = name;
            this.amount = amount;
            this.included = included;
            this.familyCode = familyCode;
        }

        public String getName() { return name; }
        public double getAmount() { return amount; }
        public boolean isIncluded() { return included; }
        public String getFamilyCode() { return familyCode; }
    }

    /**
     * Represents a booking item in the cart.
     */
    class BookingItem {
        private final Object cartItemId;
        private final String personName;
        private final String personEmail;
        private final String eventName;
        private final String eventDetails;
        private final List<BookingLineItem> lineItems;
        private double totalAmount;
        private boolean paid;
        private double paidAmount;
        private String bookingReference;

        public BookingItem(Object cartItemId, String personName, String personEmail,
                          String eventName, String eventDetails) {
            this.cartItemId = cartItemId;
            this.personName = personName;
            this.personEmail = personEmail;
            this.eventName = eventName;
            this.eventDetails = eventDetails;
            this.lineItems = new ArrayList<>();
        }

        /**
         * Simplified constructor for database-loaded bookings.
         */
        public BookingItem(String personName, String personEmail, String eventName, double totalAmount) {
            this.cartItemId = null;
            this.personName = personName;
            this.personEmail = personEmail;
            this.eventName = eventName;
            this.eventDetails = "";
            this.lineItems = new ArrayList<>();
            this.totalAmount = totalAmount;
        }

        public Object getCartItemId() { return cartItemId; }
        public String getPersonName() { return personName; }
        public String getPersonEmail() { return personEmail; }
        public String getEventName() { return eventName; }
        public String getEventDetails() { return eventDetails; }
        public List<BookingLineItem> getLineItems() { return lineItems; }

        public void addLineItem(String name, double amount) {
            lineItems.add(new BookingLineItem(name, amount, false));
            calculateTotal();
        }

        public void addLineItem(String name, double amount, boolean included) {
            lineItems.add(new BookingLineItem(name, amount, included));
            calculateTotal();
        }

        public void addLineItem(String name, String familyCode, double amount) {
            lineItems.add(new BookingLineItem(name, amount, false, familyCode));
            calculateTotal();
        }

        public void addLineItem(BookingLineItem item) {
            lineItems.add(item);
            calculateTotal();
        }

        private void calculateTotal() {
            totalAmount = lineItems.stream()
                    .filter(item -> !item.isIncluded())
                    .mapToDouble(BookingLineItem::getAmount)
                    .sum();
        }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double amount) { this.totalAmount = amount; }

        public boolean isPaid() { return paid; }
        public void setPaid(boolean paid) { this.paid = paid; }

        public double getPaidAmount() { return paidAmount; }
        public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

        public double getBalance() { return totalAmount - paidAmount; }

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
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
     * Adds a booking to the cart.
     */
    void addBooking(BookingItem booking);

    /**
     * Removes a booking from the cart.
     */
    void removeBooking(BookingItem booking);

    /**
     * Clears all bookings from the cart.
     */
    void clearBookings();

    /**
     * Returns the observable list of bookings.
     */
    ObservableList<BookingItem> getBookings();

    /**
     * Returns the total amount of all bookings.
     */
    double getTotalAmount();

    /**
     * Returns the number of bookings in the cart.
     */
    int getBookingCount();

    /**
     * Sets the callback for when "Register Another Person" is clicked.
     */
    void setOnRegisterAnotherPerson(Runnable callback);

    /**
     * Sets the callback for when "Proceed to Payment" is clicked.
     */
    void setOnProceedToPayment(Runnable callback);

    /**
     * Sets the callback for when back is pressed.
     */
    void setOnBackPressed(Runnable callback);

    /**
     * Sets the callback for when a booking is removed.
     */
    void setOnRemoveBooking(java.util.function.Consumer<BookingItem> callback);
}
