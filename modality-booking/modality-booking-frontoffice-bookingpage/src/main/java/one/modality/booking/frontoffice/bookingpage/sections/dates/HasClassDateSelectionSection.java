package one.modality.booking.frontoffice.bookingpage.sections.dates;

import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasRateTypeSection;

/**
 * Interface for a class date selection section in a General Program booking form.
 * This section displays a grid of selectable date cards representing individual classes
 * that users can select/deselect.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Individual date card selection from a class series</li>
 *   <li>Full term discount when all classes are selected</li>
 *   <li>Rate type integration for price calculations</li>
 *   <li>Support for already-booked dates (locked in modification mode)</li>
 * </ul>
 *
 * @author Claude
 * @see BookingFormSection
 * @see ResettableSection
 */
public interface HasClassDateSelectionSection extends BookingFormSection, ResettableSection {

    /**
     * Sets the rate type for price calculations.
     * Called when the user changes between standard and member pricing.
     *
     * @param rateType the selected rate type
     */
    void setRateType(HasRateTypeSection.RateType rateType);

    /**
     * Returns the list of selected ScheduledItems (class dates).
     * This is an observable list that fires change events when selections change.
     *
     * @return the observable list of selected items
     */
    ObservableList<ScheduledItem> getSelectedItems();

    /**
     * Returns whether all available dates are selected (full term).
     *
     * @return true if all dates are selected
     */
    boolean isAllDatesSelected();

    /**
     * Returns the total price in cents for the current selection.
     * This includes any full-term discount if applicable.
     *
     * @return the total price in cents
     */
    int getTotalPrice();

    /**
     * Returns the subtotal price in cents (before discount).
     * This is pricePerClass × number of selected classes.
     *
     * @return the subtotal in cents
     */
    int getSubtotal();

    /**
     * Returns the discount amount in cents when all classes are selected.
     *
     * @return the discount amount in cents, or 0 if not all classes selected
     */
    int getDiscount();

    /**
     * Returns the price per individual class in cents.
     *
     * @return the price per class in cents
     */
    int getPricePerClass();
}
