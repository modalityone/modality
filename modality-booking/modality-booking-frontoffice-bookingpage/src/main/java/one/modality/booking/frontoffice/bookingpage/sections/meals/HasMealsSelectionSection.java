package one.modality.booking.frontoffice.bookingpage.sections.meals;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.entities.Item;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Interface for the "Meals Selection" section of a booking form.
 * This section allows selection of breakfast, lunch and dinner options with dietary preferences.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Checkbox toggles for Breakfast, Lunch and Dinner</li>
 *   <li>Price per day display</li>
 *   <li>Dietary preference radio buttons (Vegetarian/Vegan)</li>
 *   <li>Info text about meal options</li>
 *   <li>Breakfast auto-included with accommodation</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasMealsSelectionSection extends BookingFormSection, ResettableSection {

    /**
     * Dietary preference options.
     */
    enum DietaryPreference {
        VEGETARIAN,
        VEGAN
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
     * Sets the price per day for breakfast.
     */
    void setBreakfastPricePerDay(int price);

    /**
     * Sets the price per day for lunch.
     */
    void setLunchPricePerDay(int price);

    /**
     * Sets the price per day for dinner.
     */
    void setDinnerPricePerDay(int price);

    /**
     * Sets the number of days for the booking (for calculating totals).
     */
    void setDaysCount(int count);

    /**
     * Sets informational text about meals (e.g., "All meals are vegetarian").
     */
    void setInfoText(Object i18nKey);

    // === Accommodation Link ===

    /**
     * Sets whether the user has accommodation (staying overnight).
     * When true, breakfast is automatically included and cannot be deselected.
     * When false (day visitor), breakfast is not available.
     */
    void setHasAccommodation(boolean hasAccommodation);

    /**
     * Returns whether the user has accommodation.
     */
    boolean hasAccommodation();

    /**
     * Sets whether the user has an extended stay (early arrival or late departure).
     * When true, shows a note about different meal pricing outside event dates.
     */
    void setHasExtendedStay(boolean hasExtendedStay);

    /**
     * Returns whether the user has an extended stay.
     */
    boolean hasExtendedStay();

    // === Selection ===

    /**
     * Property for breakfast selection.
     * Note: Breakfast is automatically included with accommodation.
     */
    BooleanProperty wantsBreakfastProperty();

    /**
     * Gets whether breakfast is selected.
     */
    default boolean wantsBreakfast() {
        return wantsBreakfastProperty().get();
    }

    /**
     * Sets whether breakfast is selected.
     */
    default void setWantsBreakfast(boolean wants) {
        wantsBreakfastProperty().set(wants);
    }

    /**
     * Property for lunch selection.
     */
    BooleanProperty wantsLunchProperty();

    /**
     * Gets whether lunch is selected.
     */
    default boolean wantsLunch() {
        return wantsLunchProperty().get();
    }

    /**
     * Sets whether lunch is selected.
     */
    default void setWantsLunch(boolean wants) {
        wantsLunchProperty().set(wants);
    }

    /**
     * Property for dinner selection.
     */
    BooleanProperty wantsDinnerProperty();

    /**
     * Gets whether dinner is selected.
     */
    default boolean wantsDinner() {
        return wantsDinnerProperty().get();
    }

    /**
     * Sets whether dinner is selected.
     */
    default void setWantsDinner(boolean wants) {
        wantsDinnerProperty().set(wants);
    }

    /**
     * Property for dietary preference.
     * @deprecated Use selectedDietaryItemProperty() instead for API-driven options.
     */
    @Deprecated
    ObjectProperty<DietaryPreference> dietaryPreferenceProperty();

    /**
     * Gets the dietary preference.
     * @deprecated Use getSelectedDietaryItem() instead for API-driven options.
     */
    @Deprecated
    default DietaryPreference getDietaryPreference() {
        return dietaryPreferenceProperty().get();
    }

    /**
     * Sets the dietary preference.
     * @deprecated Use setSelectedDietaryItem() instead for API-driven options.
     */
    @Deprecated
    default void setDietaryPreference(DietaryPreference preference) {
        dietaryPreferenceProperty().set(preference);
    }

    // === Dietary Options (API-driven) ===

    /**
     * Property for the selected dietary item (from API).
     */
    ObjectProperty<Item> selectedDietaryItemProperty();

    /**
     * Gets the selected dietary item.
     */
    default Item getSelectedDietaryItem() {
        return selectedDietaryItemProperty().get();
    }

    /**
     * Sets the selected dietary item.
     */
    default void setSelectedDietaryItem(Item item) {
        selectedDietaryItemProperty().set(item);
    }

    // === Calculated Values ===

    /**
     * Returns the total meals cost based on selections and days count.
     */
    int getTotalMealsCost();

    /**
     * Returns whether any meals are selected.
     */
    default boolean hasAnyMeals() {
        return wantsBreakfast() || wantsLunch() || wantsDinner();
    }

    // === DocumentBill Integration ===

    /**
     * Sets the WorkingBooking for DocumentBill-based pricing.
     * This enables accurate price calculation using the booking's price calculator.
     *
     * @param workingBooking the working booking instance
     */
    default void setWorkingBooking(WorkingBooking workingBooking) {
        // Default no-op for backward compatibility
    }

    /**
     * Populates meal prices from the DocumentBill API.
     * This method should be called after setWorkingBooking() and after the booking
     * has been updated (e.g., meal selections changed).
     *
     * <p>The DocumentBill provides accurate pricing that accounts for:</p>
     * <ul>
     *   <li>Rate variations by date (early arrival, late departure)</li>
     *   <li>Person-specific discounts (age, unemployed, facility fee)</li>
     *   <li>Long stay discounts</li>
     * </ul>
     */
    default void populateFromDocumentBill() {
        // Default no-op for backward compatibility
    }

    // === Selection State Binding ===

    /**
     * Binds this section to a BookingSelectionState.
     * When bound, the section will push meal selection changes to the state.
     *
     * @param selectionState the selection state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        if (selectionState != null) {
            // Push meal selection changes
            wantsBreakfastProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setWantsBreakfast(newVal);
            });
            wantsLunchProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setWantsLunch(newVal);
            });
            wantsDinnerProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setWantsDinner(newVal);
            });
            selectedDietaryItemProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setSelectedDietaryItem(newVal);
            });
            // Initialize state from current values
            selectionState.setWantsBreakfast(wantsBreakfast());
            selectionState.setWantsLunch(wantsLunch());
            selectionState.setWantsDinner(wantsDinner());
            if (getSelectedDietaryItem() != null) {
                selectionState.setSelectedDietaryItem(getSelectedDietaryItem());
            }
        }
    }

    // === Reset ===

    /**
     * Resets the section to its initial state.
     * Default implementation resets meal selections to false and clears dietary preference.
     */
    @Override
    default void reset() {
        setWantsBreakfast(false);
        setWantsLunch(false);
        setWantsDinner(false);
        setSelectedDietaryItem(null);
        setDietaryPreference(null);
    }
}
