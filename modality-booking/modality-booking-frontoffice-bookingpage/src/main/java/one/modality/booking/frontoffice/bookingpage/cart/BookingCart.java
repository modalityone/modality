package one.modality.booking.frontoffice.bookingpage.cart;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;

/**
 * Manages a shopping cart of booking items for multi-person retreat bookings.
 * Supports adding bookings for multiple household members in a single transaction.
 *
 * <p>Cart Flow:</p>
 * <ol>
 *   <li>User completes booking details for one person</li>
 *   <li>User clicks "Add to cart" on Summary step</li>
 *   <li>CartItem is added, form resets to Member Selection</li>
 *   <li>User can add more people or proceed to Basket</li>
 *   <li>All cart items are processed together at Payment</li>
 * </ol>
 *
 * @author Bruno Salmon
 */
public class BookingCart {

    private final ObservableList<CartItem> items = FXCollections.observableArrayList();
    private final ObjectProperty<Event> event = new SimpleObjectProperty<>();
    private final ObjectProperty<CartItem> currentItem = new SimpleObjectProperty<>();
    private final ObjectProperty<Number> totalPrice = new SimpleObjectProperty<>(0);
    private final IntegerProperty itemCount = new SimpleIntegerProperty(0);
    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    public BookingCart() {
        // Track item count and empty state
        items.addListener((ListChangeListener<CartItem>) change -> {
            itemCount.set(items.size());
            empty.set(items.isEmpty());
            recalculateTotal();
        });

        // Start with a fresh current item
        currentItem.set(new CartItem());
    }

    /**
     * Creates a cart for a specific event.
     *
     * @param event The event being booked
     */
    public BookingCart(Event event) {
        this();
        this.event.set(event);
    }

    /**
     * Recalculates the total price of all items in the cart.
     */
    private void recalculateTotal() {
        double total = 0;
        for (CartItem item : items) {
            Number itemTotal = item.getTotalPrice();
            if (itemTotal != null) {
                total += itemTotal.doubleValue();
            }
        }
        totalPrice.set(total);
    }

    /**
     * Adds the current item to the cart and resets for a new booking.
     * Call this when user clicks "Add to cart" on the Summary page.
     *
     * @return The item that was added
     */
    public CartItem addCurrentItemToCart() {
        CartItem itemToAdd = currentItem.get();
        if (itemToAdd != null && itemToAdd.getPerson() != null) {
            items.add(itemToAdd);
            // Create a fresh item for the next booking
            currentItem.set(new CartItem());
            return itemToAdd;
        }
        return null;
    }

    /**
     * Adds a cart item directly to the cart.
     *
     * @param item The item to add
     */
    public void addItem(CartItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Removes an item from the cart.
     *
     * @param item The item to remove
     */
    public void removeItem(CartItem item) {
        items.remove(item);
    }

    /**
     * Removes an item from the cart by index.
     *
     * @param index The index of the item to remove
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    /**
     * Checks if a person already has a booking in the cart.
     *
     * @param person The person to check
     * @return true if the person already has a cart item
     */
    public boolean hasItemForPerson(Person person) {
        if (person == null) return false;
        return items.stream().anyMatch(item ->
                item.getPerson() != null && item.getPerson().getId().equals(person.getId())
        );
    }

    /**
     * Gets the cart item for a specific person.
     *
     * @param person The person to find
     * @return The cart item, or null if not found
     */
    public CartItem getItemForPerson(Person person) {
        if (person == null) return null;
        return items.stream()
                .filter(item -> item.getPerson() != null && item.getPerson().getId().equals(person.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Clears all items from the cart and resets the current item.
     */
    public void clear() {
        items.clear();
        currentItem.set(new CartItem());
    }

    /**
     * Prepares the current item for a specific person.
     * Sets the person and their name on the current item being edited.
     *
     * @param person The person to book for
     */
    public void setCurrentPerson(Person person) {
        CartItem current = currentItem.get();
        if (current != null) {
            current.setPerson(person);
            if (person != null) {
                String fullName = person.getFirstName();
                if (person.getLastName() != null) {
                    fullName += " " + person.getLastName();
                }
                current.setPersonName(fullName);
            }
        }
    }

    /**
     * Edits an existing cart item. Removes it from the cart and sets it as current.
     *
     * @param item The item to edit
     */
    public void editItem(CartItem item) {
        if (item != null && items.contains(item)) {
            items.remove(item);
            currentItem.set(item);
        }
    }

    // === Property accessors ===

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public ObjectProperty<Event> eventProperty() {
        return event;
    }

    public Event getEvent() {
        return event.get();
    }

    public void setEvent(Event event) {
        this.event.set(event);
    }

    public ObjectProperty<CartItem> currentItemProperty() {
        return currentItem;
    }

    public CartItem getCurrentItem() {
        return currentItem.get();
    }

    public ObjectProperty<Number> totalPriceProperty() {
        return totalPrice;
    }

    public Number getTotalPrice() {
        return totalPrice.get();
    }

    public IntegerProperty itemCountProperty() {
        return itemCount;
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public BooleanProperty emptyProperty() {
        return empty;
    }

    public boolean isEmpty() {
        return empty.get();
    }
}
