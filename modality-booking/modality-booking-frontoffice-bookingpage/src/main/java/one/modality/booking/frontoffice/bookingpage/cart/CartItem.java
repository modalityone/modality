package one.modality.booking.frontoffice.bookingpage.cart;

import javafx.beans.property.*;
import one.modality.base.shared.entities.Person;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single booking item in the cart for one person.
 * Contains all selected options for a retreat booking including package, room, dates, meals, and extras.
 *
 * @author Bruno Salmon
 */
public class CartItem {

    // Person this booking is for
    private final ObjectProperty<Person> person = new SimpleObjectProperty<>();
    private final StringProperty personName = new SimpleStringProperty();

    // Package/Rate selection
    private final StringProperty packageId = new SimpleStringProperty();
    private final StringProperty packageName = new SimpleStringProperty();
    private final ObjectProperty<Number> packagePrice = new SimpleObjectProperty<>(0);
    private final StringProperty rateType = new SimpleStringProperty(); // "residential" or "non-residential"

    // Accommodation
    private final StringProperty roomId = new SimpleStringProperty();
    private final StringProperty roomName = new SimpleStringProperty();
    private final ObjectProperty<Number> roomPrice = new SimpleObjectProperty<>(0);

    // Dates
    private final ObjectProperty<LocalDate> arrivalDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> departureDate = new SimpleObjectProperty<>();

    // Times
    private final ObjectProperty<LocalTime> arrivalTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> departureTime = new SimpleObjectProperty<>();
    private final StringProperty arrivalTimeSlot = new SimpleStringProperty(); // "morning", "afternoon", "evening"
    private final StringProperty departureTimeSlot = new SimpleStringProperty();

    // Meals
    private final BooleanProperty mealsIncluded = new SimpleBooleanProperty(false);
    private final StringProperty dietaryPreference = new SimpleStringProperty();

    // Extras
    private final ListProperty<String> selectedExtras = new SimpleListProperty<>();

    // Special requests
    private final StringProperty specialRequests = new SimpleStringProperty();

    // Computed values
    private final ObjectProperty<Number> totalPrice = new SimpleObjectProperty<>(0);
    private final IntegerProperty nightCount = new SimpleIntegerProperty(0);

    public CartItem() {
        selectedExtras.set(javafx.collections.FXCollections.observableArrayList());

        // Auto-calculate total when components change
        packagePrice.addListener((obs, oldVal, newVal) -> recalculateTotal());
        roomPrice.addListener((obs, oldVal, newVal) -> recalculateTotal());

        // Auto-calculate night count when dates change
        arrivalDate.addListener((obs, oldVal, newVal) -> recalculateNights());
        departureDate.addListener((obs, oldVal, newVal) -> recalculateNights());
    }

    private void recalculateTotal() {
        double total = 0;
        if (packagePrice.get() != null) {
            total += packagePrice.get().doubleValue();
        }
        if (roomPrice.get() != null) {
            total += roomPrice.get().doubleValue() * Math.max(1, nightCount.get());
        }
        totalPrice.set(total);
    }

    private void recalculateNights() {
        if (arrivalDate.get() != null && departureDate.get() != null) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(arrivalDate.get(), departureDate.get());
            nightCount.set((int) Math.max(0, nights));
        } else {
            nightCount.set(0);
        }
        recalculateTotal();
    }

    /**
     * Creates a copy of this cart item.
     *
     * @return A new CartItem with the same values
     */
    public CartItem copy() {
        CartItem copy = new CartItem();
        copy.person.set(person.get());
        copy.personName.set(personName.get());
        copy.packageId.set(packageId.get());
        copy.packageName.set(packageName.get());
        copy.packagePrice.set(packagePrice.get());
        copy.rateType.set(rateType.get());
        copy.roomId.set(roomId.get());
        copy.roomName.set(roomName.get());
        copy.roomPrice.set(roomPrice.get());
        copy.arrivalDate.set(arrivalDate.get());
        copy.departureDate.set(departureDate.get());
        copy.arrivalTime.set(arrivalTime.get());
        copy.departureTime.set(departureTime.get());
        copy.arrivalTimeSlot.set(arrivalTimeSlot.get());
        copy.departureTimeSlot.set(departureTimeSlot.get());
        copy.mealsIncluded.set(mealsIncluded.get());
        copy.dietaryPreference.set(dietaryPreference.get());
        copy.selectedExtras.setAll(new ArrayList<>(selectedExtras.get()));
        copy.specialRequests.set(specialRequests.get());
        return copy;
    }

    /**
     * Resets all values to their defaults.
     */
    public void clear() {
        person.set(null);
        personName.set(null);
        packageId.set(null);
        packageName.set(null);
        packagePrice.set(0);
        rateType.set(null);
        roomId.set(null);
        roomName.set(null);
        roomPrice.set(0);
        arrivalDate.set(null);
        departureDate.set(null);
        arrivalTime.set(null);
        departureTime.set(null);
        arrivalTimeSlot.set(null);
        departureTimeSlot.set(null);
        mealsIncluded.set(false);
        dietaryPreference.set(null);
        selectedExtras.clear();
        specialRequests.set(null);
    }

    // === Property accessors ===

    public ObjectProperty<Person> personProperty() { return person; }
    public Person getPerson() { return person.get(); }
    public void setPerson(Person person) { this.person.set(person); }

    public StringProperty personNameProperty() { return personName; }
    public String getPersonName() { return personName.get(); }
    public void setPersonName(String personName) { this.personName.set(personName); }

    public StringProperty packageIdProperty() { return packageId; }
    public String getPackageId() { return packageId.get(); }
    public void setPackageId(String packageId) { this.packageId.set(packageId); }

    public StringProperty packageNameProperty() { return packageName; }
    public String getPackageName() { return packageName.get(); }
    public void setPackageName(String packageName) { this.packageName.set(packageName); }

    public ObjectProperty<Number> packagePriceProperty() { return packagePrice; }
    public Number getPackagePrice() { return packagePrice.get(); }
    public void setPackagePrice(Number packagePrice) { this.packagePrice.set(packagePrice); }

    public StringProperty rateTypeProperty() { return rateType; }
    public String getRateType() { return rateType.get(); }
    public void setRateType(String rateType) { this.rateType.set(rateType); }

    public StringProperty roomIdProperty() { return roomId; }
    public String getRoomId() { return roomId.get(); }
    public void setRoomId(String roomId) { this.roomId.set(roomId); }

    public StringProperty roomNameProperty() { return roomName; }
    public String getRoomName() { return roomName.get(); }
    public void setRoomName(String roomName) { this.roomName.set(roomName); }

    public ObjectProperty<Number> roomPriceProperty() { return roomPrice; }
    public Number getRoomPrice() { return roomPrice.get(); }
    public void setRoomPrice(Number roomPrice) { this.roomPrice.set(roomPrice); }

    public ObjectProperty<LocalDate> arrivalDateProperty() { return arrivalDate; }
    public LocalDate getArrivalDate() { return arrivalDate.get(); }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate.set(arrivalDate); }

    public ObjectProperty<LocalDate> departureDateProperty() { return departureDate; }
    public LocalDate getDepartureDate() { return departureDate.get(); }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate.set(departureDate); }

    public ObjectProperty<LocalTime> arrivalTimeProperty() { return arrivalTime; }
    public LocalTime getArrivalTime() { return arrivalTime.get(); }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime.set(arrivalTime); }

    public ObjectProperty<LocalTime> departureTimeProperty() { return departureTime; }
    public LocalTime getDepartureTime() { return departureTime.get(); }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime.set(departureTime); }

    public StringProperty arrivalTimeSlotProperty() { return arrivalTimeSlot; }
    public String getArrivalTimeSlot() { return arrivalTimeSlot.get(); }
    public void setArrivalTimeSlot(String arrivalTimeSlot) { this.arrivalTimeSlot.set(arrivalTimeSlot); }

    public StringProperty departureTimeSlotProperty() { return departureTimeSlot; }
    public String getDepartureTimeSlot() { return departureTimeSlot.get(); }
    public void setDepartureTimeSlot(String departureTimeSlot) { this.departureTimeSlot.set(departureTimeSlot); }

    public BooleanProperty mealsIncludedProperty() { return mealsIncluded; }
    public boolean isMealsIncluded() { return mealsIncluded.get(); }
    public void setMealsIncluded(boolean mealsIncluded) { this.mealsIncluded.set(mealsIncluded); }

    public StringProperty dietaryPreferenceProperty() { return dietaryPreference; }
    public String getDietaryPreference() { return dietaryPreference.get(); }
    public void setDietaryPreference(String dietaryPreference) { this.dietaryPreference.set(dietaryPreference); }

    public ListProperty<String> selectedExtrasProperty() { return selectedExtras; }
    public List<String> getSelectedExtras() { return selectedExtras.get(); }
    public void setSelectedExtras(List<String> extras) { this.selectedExtras.setAll(extras); }
    public void addExtra(String extra) { this.selectedExtras.add(extra); }
    public void removeExtra(String extra) { this.selectedExtras.remove(extra); }

    public StringProperty specialRequestsProperty() { return specialRequests; }
    public String getSpecialRequests() { return specialRequests.get(); }
    public void setSpecialRequests(String specialRequests) { this.specialRequests.set(specialRequests); }

    public ObjectProperty<Number> totalPriceProperty() { return totalPrice; }
    public Number getTotalPrice() { return totalPrice.get(); }

    public IntegerProperty nightCountProperty() { return nightCount; }
    public int getNightCount() { return nightCount.get(); }
}
