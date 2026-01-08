package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
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
public interface HasMealsSelectionSection extends BookingFormSection {

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
     */
    ObjectProperty<DietaryPreference> dietaryPreferenceProperty();

    /**
     * Gets the dietary preference.
     */
    default DietaryPreference getDietaryPreference() {
        return dietaryPreferenceProperty().get();
    }

    /**
     * Sets the dietary preference.
     */
    default void setDietaryPreference(DietaryPreference preference) {
        dietaryPreferenceProperty().set(preference);
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
}
