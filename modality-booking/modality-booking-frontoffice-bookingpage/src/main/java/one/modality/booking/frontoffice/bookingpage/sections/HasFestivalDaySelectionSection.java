package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Interface for the "Festival Day Selection" section of a booking form.
 * This section displays festival days and allows selection of arrival/departure dates.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Horizontal scrollable date cards</li>
 *   <li>Each festival day shows: weekday, day number, month, teaching title, price</li>
 *   <li>Arrival/departure badges on selected dates</li>
 *   <li>Visual highlighting of stay period</li>
 *   <li>Early arrival / late departure support</li>
 *   <li>Arrival/departure time selection</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasFestivalDaySelectionSection extends BookingFormSection {

    /**
     * Time of arrival/departure.
     */
    enum ArrivalDepartureTime {
        MORNING,
        AFTERNOON,
        EVENING
    }

    /**
     * Data class representing a festival day.
     */
    class FestivalDay {
        private final int dayIndex;
        private final LocalDate date;
        private final String title;
        private final int teachingPrice;
        private final boolean isFestivalDay;  // false for early arrival / late departure days

        public FestivalDay(int dayIndex, LocalDate date, String title, int teachingPrice, boolean isFestivalDay) {
            this.dayIndex = dayIndex;
            this.date = date;
            this.title = title;
            this.teachingPrice = teachingPrice;
            this.isFestivalDay = isFestivalDay;
        }

        // Convenience constructor for festival days
        public FestivalDay(int dayIndex, LocalDate date, String title, int teachingPrice) {
            this(dayIndex, date, title, teachingPrice, true);
        }

        public int getDayIndex() { return dayIndex; }
        public LocalDate getDate() { return date; }
        public String getWeekday() { return date.getDayOfWeek().name().substring(0, 3); }
        public int getDayOfMonth() { return date.getDayOfMonth(); }
        public String getMonthShort() { return date.getMonth().name().substring(0, 3); }
        public String getTitle() { return title; }
        public int getTeachingPrice() { return teachingPrice; }
        public boolean isFestivalDay() { return isFestivalDay; }
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

    // === Data Management ===

    /**
     * Sets all festival days to display.
     * @param days List of festival days in chronological order
     */
    void setFestivalDays(List<FestivalDay> days);

    /**
     * Sets the earliest possible arrival date (for early arrival).
     * This date may be before the first festival day.
     * This corresponds to ScheduledBoundary[0] - first meal for early arrival.
     */
    void setEarliestArrivalDate(LocalDate date);

    /**
     * Alias for setEarliestArrivalDate - sets the early arrival date from meal boundaries.
     * @param date the earliest arrival date (ScheduledBoundary[0])
     */
    default void setEarlyArrivalDate(LocalDate date) {
        setEarliestArrivalDate(date);
    }

    /**
     * Sets the event start date (first meal of event itself).
     * This corresponds to ScheduledBoundary[1] and becomes the default arrival date.
     * @param date the event start date (ScheduledBoundary[1])
     */
    void setEventStartDate(LocalDate date);

    /**
     * Sets the event end date (last meal of event itself).
     * This corresponds to ScheduledBoundary[2] and becomes the default departure date.
     * @param date the event end date (ScheduledBoundary[2])
     */
    void setEventEndDate(LocalDate date);

    /**
     * Sets the latest possible departure date (for late departure).
     * This date may be after the last festival day.
     * This corresponds to ScheduledBoundary[3] - last meal for late departure.
     */
    void setLatestDepartureDate(LocalDate date);

    /**
     * Alias for setLatestDepartureDate - sets the late departure date from meal boundaries.
     * @param date the latest departure date (ScheduledBoundary[3])
     */
    default void setLateDepartureDate(LocalDate date) {
        setLatestDepartureDate(date);
    }

    /**
     * Sets the minimum nights constraint from the selected accommodation.
     * When set, departure dates that would result in fewer nights than this
     * will be disabled (grayed out) in the date selection UI.
     * @param minNights minimum number of nights required (0 = no constraint)
     */
    void setMinNightsConstraint(int minNights);

    /**
     * Sets whether this is a Day Visitor booking (no overnight stay).
     * When true, arrival and departure can be the same day (0 nights).
     * When false, at least 1 night is required.
     * @param isDayVisitor true for day visitor, false for overnight accommodation
     */
    void setIsDayVisitor(boolean isDayVisitor);

    /**
     * Resets the section to initial state.
     * Clears selected dates, times, and any other selection state.
     */
    void reset();

    // === Selection ===

    /**
     * Sets the arrival date.
     */
    void setArrivalDate(LocalDate date);

    /**
     * Gets the arrival date property.
     */
    ObjectProperty<LocalDate> arrivalDateProperty();

    /**
     * Gets the currently selected arrival date.
     */
    default LocalDate getArrivalDate() {
        return arrivalDateProperty().get();
    }

    /**
     * Sets the departure date.
     */
    void setDepartureDate(LocalDate date);

    /**
     * Gets the departure date property.
     */
    ObjectProperty<LocalDate> departureDateProperty();

    /**
     * Gets the currently selected departure date.
     */
    default LocalDate getDepartureDate() {
        return departureDateProperty().get();
    }

    /**
     * Sets the arrival time (morning/afternoon/evening).
     */
    void setArrivalTime(ArrivalDepartureTime time);

    /**
     * Gets the arrival time property.
     */
    ObjectProperty<ArrivalDepartureTime> arrivalTimeProperty();

    /**
     * Sets the departure time (morning/afternoon/evening).
     */
    void setDepartureTime(ArrivalDepartureTime time);

    /**
     * Gets the departure time property.
     */
    ObjectProperty<ArrivalDepartureTime> departureTimeProperty();

    // === Callbacks ===

    /**
     * Sets the callback for when dates change.
     * @param callback Receives (arrivalDate, departureDate)
     */
    void setOnDatesChanged(BiConsumer<LocalDate, LocalDate> callback);

    // === Validation ===

    /**
     * Returns an observable boolean indicating if this section is valid.
     * Valid when both arrival and departure dates are selected and arrival <= departure.
     */
    @Override
    ObservableBooleanValue validProperty();

    /**
     * Returns the number of selected festival days (between arrival and departure).
     */
    int getSelectedDaysCount();

    /**
     * Returns the total teaching price for selected days.
     */
    int getTotalTeachingPrice();

    // === Data Population ===

    /**
     * Populates festival days from PolicyAggregate data.
     * Creates FestivalDay objects for each day from event.startDate to event.endDate.
     * For days with teaching ScheduledItems, uses ScheduledItem.getItem().getName() for the title.
     * Days without teachings are marked as non-festival days (early arrival/late departure).
     *
     * @param policyAggregate the policy data containing event and scheduledItems
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate);
}
