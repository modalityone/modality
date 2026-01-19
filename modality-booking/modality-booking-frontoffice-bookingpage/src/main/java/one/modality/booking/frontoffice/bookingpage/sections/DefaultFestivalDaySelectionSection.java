package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventPart;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledBoundary;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.util.ScheduledBoundaries;
import one.modality.base.shared.knownitems.KnownItemFamily;
import dev.webfx.stack.orm.entity.EntityList;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Default implementation of the festival day selection section.
 * Displays festival days and allows selection of arrival/departure dates.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Horizontal scrollable date cards</li>
 *   <li>Each day shows: weekday, day number, month, teaching title, price</li>
 *   <li>Arrival/departure badges on selected dates</li>
 *   <li>Visual highlighting of stay period</li>
 *   <li>Arrival/departure time selection</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-festival-day-section} - section container</li>
 *   <li>{@code .bookingpage-festival-day-card} - day card</li>
 *   <li>{@code .bookingpage-festival-day-card.arrival} - arrival date</li>
 *   <li>{@code .bookingpage-festival-day-card.departure} - departure date</li>
 *   <li>{@code .bookingpage-festival-day-card.in-stay} - between arrival and departure</li>
 *   <li>{@code .bookingpage-festival-day-card.non-festival} - early arrival/late departure day</li>
 *   <li>{@code .bookingpage-time-option} - arrival/departure time option</li>
 *   <li>{@code .bookingpage-time-option.selected} - selected time option</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasFestivalDaySelectionSection
 */
public class DefaultFestivalDaySelectionSection implements HasFestivalDaySelectionSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VALIDITY ===
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === DATE SELECTION ===
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<ArrivalDepartureTime> arrivalTimeProperty = new SimpleObjectProperty<>(ArrivalDepartureTime.AFTERNOON);
    protected final ObjectProperty<ArrivalDepartureTime> departureTimeProperty = new SimpleObjectProperty<>(ArrivalDepartureTime.AFTERNOON);  // Default to afternoon per JSX

    // === CONSTRAINT ===
    protected int minNightsConstraint = 0; // 0 means no constraint
    protected boolean isDayVisitor = false; // true for day visitor (0 nights allowed)
    protected String changingDateMode = null; // null, "arrival", or "departure"

    // === DATA ===
    protected List<FestivalDay> festivalDays = new ArrayList<>();
    protected LocalDate earliestArrivalDate;
    protected LocalDate latestDepartureDate;
    protected LocalDate defaultArrivalDate;  // First festival/teaching day
    protected LocalDate defaultDepartureDate;  // Last festival/teaching day

    // === MEAL TIMES FROM API ===
    // Times are loaded from ScheduledItems - null if not available in database
    // Only lunch and dinner times are used for arrival/departure time selection
    protected LocalTime lunchStartTime;
    protected LocalTime dinnerStartTime;

    // === MEALS AVAILABLE PER DATE ===
    // Tracks which meals are scheduled on each date (loaded from MEALS ScheduledItems)
    // Used to dynamically show correct meal options for arrival/departure days
    protected Map<LocalDate, java.util.Set<String>> mealsAvailableByDate = new HashMap<>();

    // === BOUNDARY MEAL LIMITS ===
    // Maps date to the FIRST/LAST meal available that day (from EventPart boundary scheduledItems)
    // Used to exclude meals BEFORE the first or AFTER the last boundary meal
    protected Map<LocalDate, String> firstMealOnDate = new HashMap<>();  // e.g., "dinner" means no lunch/breakfast on arrival
    protected Map<LocalDate, String> lastMealOnDate = new HashMap<>();   // e.g., "lunch" means no dinner on departure

    // === EARLY ARRIVAL FIRST MEAL ===
    // Tracks the first meal available for early arrivals (separate from main event's first meal)
    // Used for DISPLAY purposes only - showing what meals early arrivals can get
    protected Map<LocalDate, String> earlyArrivalFirstMealOnDate = new HashMap<>();
    // Dates where early arrival allows meals before the main event's first meal
    // For these dates, we show all scheduled meals in arrival time display
    protected java.util.Set<LocalDate> earlyArrivalAvailableDates = new java.util.HashSet<>();

    // === LATE DEPARTURE TRANSITION INFO ===
    // The first meal of late departure (on event end date) - this is an ADDITIONAL meal beyond main event
    // e.g., if main event ends with lunch and late departure starts with dinner on same day
    protected LocalDate lateDepartureTransitionDate = null;
    protected String lateDepartureFirstMeal = null;  // The first meal available to late departures on transition day

    // === MAIN EVENT BOUNDARY INFO ===
    // The actual main event dates (excluding early arrival and late departure periods)
    // Used to correctly identify which meals are within the main event vs early/late
    protected LocalDate mainEventStartDate = null;
    protected LocalDate mainEventEndDate = null;
    protected boolean hasLateDeparturePart = false;  // True if there's a late departure EventPart
    protected LocalDate lateDepartureEndDate = null;  // The end date of the late departure part (for calendar range)

    // === BOUNDARY-DERIVED TIMES ===
    // Times from ScheduledBoundaries for computing arrival/departure defaults
    protected LocalTime eventFirstMealEndTime;   // From boundary[1] - when first event meal ends
    protected LocalTime eventLastMealStartTime;  // From boundary[2] - when last event meal starts
    protected LocalTime arrivalDeadline;         // eventFirstMealEndTime - 10 minutes
    protected LocalTime departureEarliest;       // eventLastMealStartTime + 10 minutes
    protected ArrivalDepartureTime defaultArrivalTime = ArrivalDepartureTime.AFTERNOON;
    protected ArrivalDepartureTime defaultDepartureTime = ArrivalDepartureTime.AFTERNOON;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox instructionBox;
    protected Label instructionLabel;
    protected FlowPane daysContainer;
    protected HBox changeDateButtonsBox;
    protected HBox timeMealsInfoBox;  // Info box explaining meal time selection
    protected VBox arrivalTimeSection;
    protected VBox departureTimeSection;
    protected Label arrivalTimeTitleLabel;  // Title label for arrival time section
    protected Label departureTimeTitleLabel;  // Title label for departure time section

    // === CALLBACKS ===
    protected BiConsumer<LocalDate, LocalDate> onDatesChanged;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultFestivalDaySelectionSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(20);
        container.getStyleClass().add("bookingpage-festival-day-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.YourStayAndFestivalDays, StyledSectionHeader.ICON_CALENDAR);

        // Instruction box with helpful text
        instructionBox = buildInstructionBox();

        // Days container (FlowPane for wrapping like JSX flex-wrap)
        daysContainer = new FlowPane();
        daysContainer.setHgap(8);
        daysContainer.setVgap(8);
        daysContainer.setAlignment(Pos.CENTER);
        daysContainer.setPadding(new Insets(8, 0, 20, 0));

        // Change Arrival/Departure buttons (shown when dates are set)
        changeDateButtonsBox = buildChangeDateButtons();
        changeDateButtonsBox.setVisible(false);
        changeDateButtonsBox.setManaged(false);

        // Info box explaining meal time selection (shown above time sections)
        timeMealsInfoBox = BookingPageUIBuilder.createInfoBox(BookingPageI18nKeys.FestivalDaysTimeMealsInfo, BookingPageUIBuilder.InfoBoxType.NEUTRAL);
        timeMealsInfoBox.setVisible(false);
        timeMealsInfoBox.setManaged(false);

        // Arrival time section
        arrivalTimeSection = buildTimeSelectionSection(true);
        arrivalTimeSection.setVisible(false);
        arrivalTimeSection.setManaged(false);

        // Departure time section
        departureTimeSection = buildTimeSelectionSection(false);
        departureTimeSection.setVisible(false);
        departureTimeSection.setManaged(false);

        container.getChildren().addAll(sectionHeader, instructionBox, daysContainer, changeDateButtonsBox, timeMealsInfoBox, arrivalTimeSection, departureTimeSection);
    }

    protected HBox buildInstructionBox() {
        // Use the standard info box helper with neutral style (matches "Price includes" boxes)
        HBox box = BookingPageUIBuilder.createInfoBox(BookingPageI18nKeys.FestivalDaysInstructions, BookingPageUIBuilder.InfoBoxType.NEUTRAL);

        // Get the message label from the box (first child since NEUTRAL has no icon)
        if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Label) {
            instructionLabel = (Label) box.getChildren().get(0);
        }

        return box;
    }

    protected HBox buildChangeDateButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 0, 16, 0));

        // Change Arrival button - uses CSS class for theme colors
        Button changeArrivalBtn = new Button("→ Change Arrival");
        changeArrivalBtn.getStyleClass().add("bookingpage-change-arrival-btn");
        changeArrivalBtn.setPadding(new Insets(10, 16, 10, 16));
        changeArrivalBtn.setFocusTraversable(false); // Prevent focus-related scroll issues
        changeArrivalBtn.setOnAction(e -> {
            e.consume(); // Prevent event propagation
            changingDateMode = "arrival";
            rebuildDayCards();
            daysContainer.requestFocus(); // Keep focus in the section
        });

        // Change Departure button - uses CSS class for theme colors
        Button changeDepartureBtn = new Button("← Change Departure");
        changeDepartureBtn.getStyleClass().add("bookingpage-change-departure-btn");
        changeDepartureBtn.setPadding(new Insets(10, 16, 10, 16));
        changeDepartureBtn.setFocusTraversable(false); // Prevent focus-related scroll issues
        changeDepartureBtn.setOnAction(e -> {
            e.consume(); // Prevent event propagation
            changingDateMode = "departure";
            rebuildDayCards();
            daysContainer.requestFocus(); // Keep focus in the section
        });

        box.getChildren().addAll(changeArrivalBtn, changeDepartureBtn);
        return box;
    }

    protected VBox buildTimeSelectionSection(boolean isArrival) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16, 20, 16, 20));
        // Use CSS classes for theming - arrival uses primary color, departure uses dark text
        section.getStyleClass().add(isArrival ? "bookingpage-time-section-arrival" : "bookingpage-time-section-departure");

        // Header with clock icon and title with date
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        // Clock SVG icon - uses CSS class for theming
        javafx.scene.shape.SVGPath clockIcon = new javafx.scene.shape.SVGPath();
        clockIcon.setContent("M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67V7z");
        clockIcon.getStyleClass().add(isArrival ? "bookingpage-time-section-icon-arrival" : "bookingpage-time-section-icon-departure");
        clockIcon.setScaleX(0.7);
        clockIcon.setScaleY(0.7);

        // Title label - uses CSS class for styling
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("bookingpage-time-section-title");

        header.getChildren().addAll(clockIcon, titleLabel);

        // Store reference for dynamic update
        if (isArrival) {
            arrivalTimeTitleLabel = titleLabel;
        } else {
            departureTimeTitleLabel = titleLabel;
        }

        // Time options row (FlowPane for wrapping on small screens)
        FlowPane optionsRow = new FlowPane();
        optionsRow.setHgap(10);
        optionsRow.setVgap(10);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        ObjectProperty<ArrivalDepartureTime> timeProperty = isArrival ? arrivalTimeProperty : departureTimeProperty;
        LocalDate dateToCheck = isArrival ? arrivalDateProperty.get() : departureDateProperty.get();

        int optionsAdded = 0;
        for (ArrivalDepartureTime time : ArrivalDepartureTime.values()) {
            // Only show time options that have valid meals for this date
            boolean isValid = isTimeOptionValidForDate(time, isArrival, dateToCheck);
            if (isValid) {
                VBox optionCard = createTimeOptionCard(time, timeProperty, isArrival);
                optionsRow.getChildren().add(optionCard);
                optionsAdded++;
            }
        }

        section.getChildren().addAll(header, optionsRow);
        return section;
    }

    protected VBox createTimeOptionCard(ArrivalDepartureTime time, ObjectProperty<ArrivalDepartureTime> timeProperty, boolean isArrival) {
        String contextClass = isArrival ? "bookingpage-time-option-arrival" : "bookingpage-time-option-departure";

        VBox card = new VBox(4);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setMinWidth(140);
        card.setCursor(Cursor.HAND);
        card.getStyleClass().addAll("bookingpage-time-option", contextClass);

        boolean isSelected = timeProperty.get() == time;

        // Apply initial selected state via CSS class
        if (isSelected) {
            card.getStyleClass().add("selected");
        }

        // Time label - uses CSS class for styling
        Label timeLabel = new Label(getTimeLabel(time));
        timeLabel.getStyleClass().add("bookingpage-time-option-label");

        // Time range - uses CSS class for styling
        Label rangeLabel = new Label(getTimeRange(time, isArrival));
        rangeLabel.getStyleClass().add("bookingpage-time-option-range");

        // Meal note - uses CSS class based on meal availability
        String mealText = getMealNote(time, isArrival);
        boolean hasNoMeals = mealText.contains("\u2717");
        Label mealLabel = new Label(mealText);
        mealLabel.getStyleClass().addAll("bookingpage-time-option-meal",
            hasNoMeals ? "bookingpage-time-option-meal-negative" : "bookingpage-time-option-meal-positive");

        card.getChildren().addAll(timeLabel, rangeLabel, mealLabel);

        // Selection handling - toggle CSS class
        timeProperty.addListener((obs, old, newVal) -> {
            boolean selected = newVal == time;
            if (selected) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
        });

        // Click handler to select this option
        card.setOnMouseClicked(e -> timeProperty.set(time));

        return card;
    }

    protected String getTimeLabel(ArrivalDepartureTime time) {
        return switch (time) {
            case MORNING -> "Morning";
            case AFTERNOON -> "Afternoon";
            case EVENING -> "Evening";
        };
    }

    protected String getTimeRange(ArrivalDepartureTime time, boolean isArrival) {
        // Add 15 minute buffer to meal times for realistic timing
        // - For arrival: buffer after meal start (arrive within 15 min of meal starting to still get it)
        // - For departure: buffer after meal start (stay 15 min after meal starts to have eaten)
        LocalTime lunchWithBuffer = lunchStartTime != null ? lunchStartTime.plusMinutes(15) : null;
        LocalTime dinnerWithBuffer = dinnerStartTime != null ? dinnerStartTime.plusMinutes(15) : null;

        if (isArrival) {
            // Arrival times based on meal schedule - uses API meal times + 15 min buffer
            // Morning: arrive before lunch + buffer → get lunch + dinner
            // Afternoon: arrive after lunch + buffer but before dinner + buffer → get dinner only
            // Evening: arrive after dinner + buffer → no meals that day
            return switch (time) {
                case MORNING -> formatBefore(lunchWithBuffer);
                case AFTERNOON -> formatRange(lunchWithBuffer, dinnerWithBuffer);
                case EVENING -> formatAfter(dinnerWithBuffer);
            };
        } else {
            // Departure times based on meal schedule - uses API meal times
            // Morning: leave before lunch starts → breakfast only
            // Afternoon: leave after lunch + buffer but before dinner → breakfast + lunch
            // Evening: leave after dinner + buffer → all meals
            return switch (time) {
                case MORNING -> formatBefore(lunchStartTime);
                case AFTERNOON -> formatRange(lunchWithBuffer, dinnerStartTime);
                case EVENING -> formatAfter(dinnerWithBuffer);
            };
        }
    }

    /**
     * Formats a LocalTime for display (e.g., "13:00").
     * Returns empty string if time is null.
     */
    protected String formatTime(LocalTime time) {
        if (time == null) return "";
        // GWT-compatible time formatting (String.format not available in GWT)
        int hour = time.getHour();
        int minute = time.getMinute();
        return (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
    }

    /**
     * Formats "Before HH:MM" or empty if time is null.
     */
    protected String formatBefore(LocalTime time) {
        if (time == null) return "";
        return "Before " + formatTime(time);
    }

    /**
     * Formats "After HH:MM" or empty if time is null.
     */
    protected String formatAfter(LocalTime time) {
        if (time == null) return "";
        return "After " + formatTime(time);
    }

    /**
     * Formats "HH:MM - HH:MM" or empty if either time is null.
     */
    protected String formatRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) return "";
        return formatTime(start) + " - " + formatTime(end);
    }

    protected String getMealNote(ArrivalDepartureTime time, boolean isArrival) {
        // Get the actual date to check which meals are available
        LocalDate dateToCheck = isArrival ? arrivalDateProperty.get() : departureDateProperty.get();
        return getMealNoteForDate(time, isArrival, dateToCheck);
    }

    /**
     * Returns the meal note for a specific date, taking into account what meals
     * are actually scheduled on that date in the database AND the boundary limits
     * from EventPart boundaries (which tell us the first/last meals of the event).
     */
    protected String getMealNoteForDate(ArrivalDepartureTime time, boolean isArrival, LocalDate date) {
        // Get meals available on this date (or empty set if no data)
        java.util.Set<String> mealsOnDate = date != null ? mealsAvailableByDate.getOrDefault(date, java.util.Collections.emptySet()) : java.util.Collections.emptySet();
        boolean hasBreakfast = mealsOnDate.contains("breakfast");
        boolean hasLunch = mealsOnDate.contains("lunch");
        boolean hasDinner = mealsOnDate.contains("dinner");

        // If we have no meal data for this date, use default assumptions
        // (breakfast, lunch, dinner all available on festival days)
        if (mealsOnDate.isEmpty() && date != null) {
            // Check if it's a festival day - if so, assume all meals
            boolean isFestivalDay = festivalDays.stream().anyMatch(d -> d.getDate().equals(date) && d.isFestivalDay());
            if (isFestivalDay) {
                hasBreakfast = true;
                hasLunch = true;
                hasDinner = true;
            }
        }

        // Apply boundary limits from EventPart boundaries
        // For arrival: check firstMealOnDate - meals BEFORE the first meal are not included
        // For departure: check lastMealOnDate - meals AFTER the last meal are not included
        if (date != null) {
            if (isArrival) {
                // For arrival days, exclude meals BEFORE the first boundary meal
                // BUT if early arrival is available on this date, show ALL scheduled meals
                // (early arrivals can get meals before the main event's first meal)
                if (earlyArrivalAvailableDates.contains(date)) {
                    // Don't apply any firstMeal restriction - show all scheduled meals
                } else {
                    // For DISPLAY purposes, prefer earlyArrivalFirstMealOnDate (shows what early arrivals can get)
                    // This allows showing lunch even when main event starts with dinner
                    String firstMeal = earlyArrivalFirstMealOnDate.get(date);
                    if (firstMeal == null) {
                        // Fall back to main event's first meal if no early arrival data
                        firstMeal = firstMealOnDate.get(date);
                    }
                    if (firstMeal != null) {
                        // Meal ordering: breakfast < lunch < dinner
                        // If first meal is lunch, exclude breakfast
                        // If first meal is dinner, exclude breakfast and lunch
                        if ("lunch".equals(firstMeal)) {
                            hasBreakfast = false;
                        } else if ("dinner".equals(firstMeal)) {
                            hasBreakfast = false;
                            hasLunch = false;
                        }
                    }
                }
            } else {
                // For departure days, exclude meals AFTER the last boundary meal
                String lastMeal = lastMealOnDate.get(date);
                if (lastMeal != null) {
                    // Meal ordering: breakfast < lunch < dinner
                    // If last meal is lunch, exclude dinner
                    // If last meal is breakfast, exclude lunch and dinner
                    if ("lunch".equals(lastMeal)) {
                        hasDinner = false;
                    } else if ("breakfast".equals(lastMeal)) {
                        hasLunch = false;
                        hasDinner = false;
                    }
                }
            }
        }

        if (isArrival) {
            // Arrival: based on arrival time, which meals will guest get?
            // MORNING: arrives before lunch, gets lunch (if available) + dinner (if available)
            // AFTERNOON: arrives after lunch but before dinner, gets dinner only (if available)
            // EVENING: arrives after dinner, no meals
            return switch (time) {
                case MORNING -> {
                    if (hasLunch && hasDinner) yield "\u2713 Lunch + Dinner";
                    if (hasLunch) yield "\u2713 Lunch only";
                    if (hasDinner) yield "\u2713 Dinner only";
                    yield "\u2717 No meals";
                }
                case AFTERNOON -> {
                    if (hasDinner) yield "\u2713 Dinner only";
                    yield "\u2717 No meals";
                }
                case EVENING -> "\u2717 No meals";
            };
        } else {
            // Departure: based on departure time, which meals will guest get?
            // MORNING: leaves before lunch, gets breakfast only (if available)
            // AFTERNOON: leaves after lunch but before dinner, gets breakfast + lunch
            // EVENING: leaves after dinner, gets all available meals
            return switch (time) {
                case MORNING -> {
                    if (hasBreakfast) yield "\u2713 Breakfast only";
                    yield "\u2717 No meals";
                }
                case AFTERNOON -> {
                    if (hasBreakfast && hasLunch) yield "\u2713 Breakfast + Lunch";
                    if (hasBreakfast) yield "\u2713 Breakfast only";
                    if (hasLunch) yield "\u2713 Lunch only";
                    yield "\u2717 No meals";
                }
                case EVENING -> {
                    List<String> meals = new ArrayList<>();
                    if (hasBreakfast) meals.add("Breakfast");
                    if (hasLunch) meals.add("Lunch");
                    if (hasDinner) meals.add("Dinner");
                    if (meals.isEmpty()) yield "\u2717 No meals";
                    if (meals.size() == 3) yield "\u2713 All meals";
                    yield "\u2713 " + String.join(" + ", meals);
                }
            };
        }
    }

    /**
     * Checks if a time option should be displayed for a given date.
     * Hides time options that reference meals that don't exist on that date.
     *
     * For departure:
     * - EVENING: Only show if dinner exists on that day
     * - AFTERNOON: Only show if lunch exists on that day
     * - MORNING: Always show (breakfast is from previous night's stay)
     *
     * For arrival: Always show all options (arriving at any time is valid)
     */
    protected boolean isTimeOptionValidForDate(ArrivalDepartureTime time, boolean isArrival, LocalDate date) {
        // Arrival options are always valid - you can arrive at any time
        if (isArrival) {
            return true;
        }

        // For departure, check if the required meal exists on that date
        if (date == null) {
            return true; // No date selected yet, show all options
        }

        java.util.Set<String> mealsOnDate = mealsAvailableByDate.getOrDefault(date, java.util.Collections.emptySet());
        String lastMeal = lastMealOnDate.get(date);

        // Check if the requested time option is valid given the boundary limit
        // If lastMeal is set for this date, meals AFTER the last meal are not available
        // EXCEPTION: If there's a late departure part, EVENING is valid on the main event end date
        // because late departure adds dinner on that day
        if (lastMeal != null) {
            boolean isValidGivenBoundary = switch (time) {
                case MORNING -> true; // Breakfast is always before lunch/dinner
                case AFTERNOON -> !lastMeal.equals("breakfast"); // Need lunch, which is after breakfast
                case EVENING -> {
                    // EVENING is valid if:
                    // 1. The last meal on this date is already dinner, OR
                    // 2. There's a late departure part AND this is the main event end date
                    //    (late departure adds dinner on the transition day)
                    boolean isLateDepartureTransition = hasLateDeparturePart &&
                        mainEventEndDate != null && date.equals(mainEventEndDate);
                    yield lastMeal.equals("dinner") || isLateDepartureTransition;
                }
            };
            if (!isValidGivenBoundary) {
                return false;
            }
        }

        // If we have no meal data, check if it's a festival day (assume all meals available)
        if (mealsOnDate.isEmpty()) {
            boolean isFestivalDay = festivalDays.stream().anyMatch(d -> d.getDate().equals(date) && d.isFestivalDay());
            if (isFestivalDay) {
                return true; // Festival day with no data - assume all meals, show all options
            }
            // Non-festival day with no meal data - only show MORNING (breakfast from night before)
            return time == ArrivalDepartureTime.MORNING;
        }

        // Check meal availability for each departure time option
        return switch (time) {
            case MORNING -> true; // Always valid - breakfast is from accommodation night before
            case AFTERNOON -> mealsOnDate.contains("lunch"); // Need lunch to stay until afternoon
            case EVENING -> mealsOnDate.contains("dinner"); // Need dinner to stay until evening
        };
    }

    protected void setupBindings() {
        // Update validity when dates change
        arrivalDateProperty.addListener((obs, old, newVal) -> updateValidity());
        departureDateProperty.addListener((obs, old, newVal) -> updateValidity());

        // Notify callback when dates change
        arrivalDateProperty.addListener((obs, old, newVal) -> notifyDatesChanged());
        departureDateProperty.addListener((obs, old, newVal) -> notifyDatesChanged());

        // Show/hide time sections and change date buttons based on date selection
        arrivalDateProperty.addListener((obs, old, newVal) -> updateSectionVisibility());
        departureDateProperty.addListener((obs, old, newVal) -> updateSectionVisibility());

        // Rebuild time sections when dates change to update meal notes for new dates
        arrivalDateProperty.addListener((obs, old, newVal) -> rebuildTimeSections());
        departureDateProperty.addListener((obs, old, newVal) -> rebuildTimeSections());

        // Rebuild time sections when color scheme changes to apply new colors
        colorScheme.addListener((obs, old, newVal) -> rebuildTimeSections());
    }

    /**
     * Rebuilds the time selection sections with current color scheme.
     * Called when color scheme changes to apply new theme colors.
     * Also validates current time selections and auto-selects valid options if needed.
     */
    protected void rebuildTimeSections() {
        if (container == null || arrivalTimeSection == null || departureTimeSection == null) return;

        // Auto-select valid time options if current selection is no longer valid
        autoSelectValidTimeOptions();

        // Store visibility state
        boolean arrivalVisible = arrivalTimeSection.isVisible();
        boolean departureVisible = departureTimeSection.isVisible();

        // Find indices in container
        int arrivalIndex = container.getChildren().indexOf(arrivalTimeSection);
        int departureIndex = container.getChildren().indexOf(departureTimeSection);

        if (arrivalIndex >= 0) {
            container.getChildren().remove(arrivalTimeSection);
            arrivalTimeSection = buildTimeSelectionSection(true);
            arrivalTimeSection.setVisible(arrivalVisible);
            arrivalTimeSection.setManaged(arrivalVisible);
            container.getChildren().add(arrivalIndex, arrivalTimeSection);
        }

        if (departureIndex >= 0) {
            // Adjust index if arrival was before departure
            int adjustedIndex = arrivalIndex >= 0 && arrivalIndex < departureIndex ? departureIndex : departureIndex;
            container.getChildren().remove(departureTimeSection);
            departureTimeSection = buildTimeSelectionSection(false);
            departureTimeSection.setVisible(departureVisible);
            departureTimeSection.setManaged(departureVisible);
            container.getChildren().add(adjustedIndex, departureTimeSection);
        }

        // Update titles with current dates
        updateSectionVisibility();
    }

    /**
     * Auto-selects valid time options if the current selection is no longer valid for the date.
     * For example, if EVENING is selected but dinner is not available, switches to AFTERNOON or MORNING.
     */
    protected void autoSelectValidTimeOptions() {
        // Check departure time validity
        LocalDate departureDate = departureDateProperty.get();
        ArrivalDepartureTime currentDepartureTime = departureTimeProperty.get();
        if (departureDate != null && currentDepartureTime != null) {
            if (!isTimeOptionValidForDate(currentDepartureTime, false, departureDate)) {
                // Find the best valid option (prefer AFTERNOON over MORNING for better UX)
                if (isTimeOptionValidForDate(ArrivalDepartureTime.AFTERNOON, false, departureDate)) {
                    departureTimeProperty.set(ArrivalDepartureTime.AFTERNOON);
                } else {
                    departureTimeProperty.set(ArrivalDepartureTime.MORNING);
                }
            }
        }

        // Arrival times are always valid, no auto-selection needed
    }

    protected void updateSectionVisibility() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        boolean hasArrival = arrival != null;
        boolean hasDeparture = departure != null;
        boolean hasBothDates = hasArrival && hasDeparture;

        // Hide time sections if arrival and departure are the same day (Day Visitor with 0 nights)
        boolean isSameDay = hasBothDates && arrival.equals(departure);
        boolean showArrivalTime = hasArrival && !isSameDay;
        boolean showDepartureTime = hasDeparture && !isSameDay;

        arrivalTimeSection.setVisible(showArrivalTime);
        arrivalTimeSection.setManaged(showArrivalTime);
        departureTimeSection.setVisible(showDepartureTime);
        departureTimeSection.setManaged(showDepartureTime);

        // Show meal time info box when time sections are visible
        boolean showTimeMealsInfo = showArrivalTime || showDepartureTime;
        timeMealsInfoBox.setVisible(showTimeMealsInfo);
        timeMealsInfoBox.setManaged(showTimeMealsInfo);

        // Update time section titles with dates
        if (arrivalTimeTitleLabel != null && hasArrival) {
            arrivalTimeTitleLabel.setText("Arrival Time on " + formatDateForTitle(arrival));
        }
        if (departureTimeTitleLabel != null && hasDeparture) {
            departureTimeTitleLabel.setText("Departure Time on " + formatDateForTitle(departure));
        }

        // Show change date buttons only when both dates are set and not in changing mode
        boolean showChangeButtons = hasBothDates && changingDateMode == null;
        changeDateButtonsBox.setVisible(showChangeButtons);
        changeDateButtonsBox.setManaged(showChangeButtons);

        // Update instruction text based on current state
        updateInstructionText();
    }

    /**
     * Formats a date for display in time section title.
     * Example: "Mon, 23 Jun"
     */
    protected String formatDateForTitle(LocalDate date) {
        if (date == null) return "";
        String weekday = date.getDayOfWeek().name().substring(0, 3);
        weekday = weekday.substring(0, 1).toUpperCase() + weekday.substring(1).toLowerCase();
        String month = date.getMonth().name().substring(0, 3);
        month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        return weekday + ", " + date.getDayOfMonth() + " " + month;
    }

    protected void updateInstructionText() {
        if (instructionLabel == null || instructionBox == null) return;

        boolean hasArrival = arrivalDateProperty.get() != null;
        boolean hasDeparture = departureDateProperty.get() != null;
        boolean isChangingMode = "arrival".equals(changingDateMode) || "departure".equals(changingDateMode);
        boolean hasMinNightsConstraint = minNightsConstraint > 0;

        // Determine if we should show warning style (changing dates with min nights constraint)
        boolean showWarning = isChangingMode && hasMinNightsConstraint;

        // Update box style based on warning state - CSS handles styling
        instructionBox.getStyleClass().removeAll("bookingpage-info-box-warning", "bookingpage-info-box-neutral");
        if (showWarning) {
            instructionBox.getStyleClass().add("bookingpage-info-box-warning");
        } else {
            instructionBox.getStyleClass().add("bookingpage-info-box-neutral");
        }

        // Determine the base instruction text
        Object baseI18nKey;
        if ("arrival".equals(changingDateMode)) {
            baseI18nKey = BookingPageI18nKeys.FestivalDaysChangingArrival;
        } else if ("departure".equals(changingDateMode)) {
            baseI18nKey = BookingPageI18nKeys.FestivalDaysChangingDeparture;
        } else if (hasArrival && !hasDeparture) {
            baseI18nKey = BookingPageI18nKeys.FestivalDaysSelectDeparture;
        } else {
            // Default instructions
            baseI18nKey = BookingPageI18nKeys.FestivalDaysInstructions;
        }

        // Unbind first (label was created with I18nControls.newLabel which binds the text property)
        instructionLabel.textProperty().unbind();

        // Build the final text and apply style via CSS classes
        String finalText;

        // Update CSS classes for text color based on warning state
        instructionLabel.getStyleClass().removeAll("bookingpage-text-warning", "bookingpage-text-dark");
        instructionLabel.getStyleClass().add("bookingpage-text-base");

        if (isChangingMode && hasMinNightsConstraint) {
            // Warning mode: concatenate instruction + warning about min nights
            instructionLabel.getStyleClass().add("bookingpage-text-warning");
            String baseText = I18n.getI18nText(baseI18nKey);
            String warningText = I18n.getI18nText("FestivalDaysMinNightsWarning", minNightsConstraint);
            finalText = baseText + " " + warningText;
        } else {
            // Normal mode: just the instruction text
            instructionLabel.getStyleClass().add("bookingpage-text-dark");
            finalText = I18n.getI18nText(baseI18nKey);
        }

        // Update the label text
        instructionLabel.setText(finalText);

        // Always keep the instruction box visible
        instructionBox.setVisible(true);
        instructionBox.setManaged(true);
    }

    protected void updateValidity() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        // For day visitors: arrival can equal departure (0 nights)
        // For all others: departure must be after arrival (at least 1 night)
        boolean isValid;
        if (arrival == null || departure == null) {
            isValid = false;
        } else if (isDayVisitor) {
            isValid = !departure.isBefore(arrival);  // departure >= arrival
        } else {
            isValid = departure.isAfter(arrival);  // departure > arrival
        }
        validProperty.set(isValid);
        rebuildDayCards();
    }

    protected void notifyDatesChanged() {
        if (onDatesChanged != null) {
            onDatesChanged.accept(arrivalDateProperty.get(), departureDateProperty.get());
        }
    }

    protected void rebuildDayCards() {
        daysContainer.getChildren().clear();

        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();

        for (FestivalDay day : festivalDays) {
            StackPane card = createDayCard(day, arrival, departure);
            daysContainer.getChildren().add(card);
        }

        // Update change date buttons visibility
        updateSectionVisibility();
    }

    protected StackPane createDayCard(FestivalDay day, LocalDate arrival, LocalDate departure) {
        LocalDate date = day.getDate();
        boolean isArrival = date.equals(arrival);
        boolean isDeparture = date.equals(departure);
        boolean isSameDay = isArrival && isDeparture;
        boolean isInStay = arrival != null && departure != null &&
                          date.isAfter(arrival) && date.isBefore(departure);
        boolean isFestival = day.isFestivalDay();

        // Determine if date is disabled based on constraints
        boolean isDisabled = isDateDisabled(date, arrival, departure);
        boolean isClickable = isDateClickable(date, arrival, departure);
        boolean isChangingMode = changingDateMode != null && isClickable;

        // Card container (VBox inside StackPane for badge positioning)
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 6, isFestival ? 8 : 10, 6));
        card.setMinWidth(100);
        card.setPrefWidth(100);
        card.setCursor(isDisabled ? Cursor.DEFAULT : (isClickable ? Cursor.HAND : Cursor.DEFAULT));

        // Apply CSS classes based on state - CSS handles styling
        card.getStyleClass().add("bookingpage-festival-day-card");
        if (isDisabled) {
            card.getStyleClass().add("disabled");
        } else if (isArrival) {
            card.getStyleClass().add("arrival");
        } else if (isDeparture) {
            card.getStyleClass().add("departure");
        } else if (isChangingMode) {
            card.getStyleClass().add("changing");
        } else if (isInStay) {
            card.getStyleClass().add("in-stay");
        } else if (isFestival) {
            card.getStyleClass().add("festival");
        }

        // Weekday (10px, uppercase, semibold) - CSS handles text color based on card state
        Label weekdayLabel = new Label(day.getWeekday().toUpperCase());
        weekdayLabel.getStyleClass().add("bookingpage-festival-day-weekday");

        // Day number (22px, bold) - CSS handles text color based on card state
        Label dayLabel = new Label(String.valueOf(day.getDayOfMonth()));
        dayLabel.getStyleClass().add("bookingpage-festival-day-number");

        // Month (9px, uppercase, semibold) - CSS handles text color based on card state
        Label monthLabel = new Label(day.getMonthShort().toUpperCase());
        monthLabel.getStyleClass().add("bookingpage-festival-day-month");

        card.getChildren().addAll(weekdayLabel, dayLabel, monthLabel);

        // Teaching info section (only for festival days)
        if (isFestival) {
            VBox.setMargin(monthLabel, new Insets(0, 0, 8, 0));

            // Separator line - CSS handles color based on card state
            Region separator = new Region();
            separator.setMinHeight(1);
            separator.setMaxHeight(1);
            separator.getStyleClass().add("bookingpage-festival-day-separator");
            VBox.setMargin(separator, new Insets(4, 0, 6, 0));

            // Teaching title - CSS handles text color based on card state
            Label titleLabel = new Label(day.getTitle() != null ? day.getTitle() : "");
            titleLabel.getStyleClass().add("bookingpage-festival-day-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(88);
            titleLabel.setMinHeight(24);
            titleLabel.setAlignment(Pos.CENTER);

            // Price - CSS handles text color based on card state
            Label priceLabel = new Label(formatPrice(day.getTeachingPrice()));
            priceLabel.getStyleClass().add("bookingpage-festival-day-price");

            card.getChildren().addAll(separator, titleLabel, priceLabel);
        }

        // Wrap in StackPane for badge positioning
        StackPane wrapper = new StackPane(card);
        wrapper.setAlignment(Pos.TOP_CENTER);

        // Arrival/Departure badge
        if (isFestival && (isArrival || isDeparture)) {
            // For festival days, badge goes at top-right or top-left
            Label badge = new Label(isArrival ? "ARRIVAL" : "DEPARTURE");
            badge.getStyleClass().add("bookingpage-festival-day-badge");
            badge.setPadding(new Insets(2, 6, 2, 6));
            // Add state class for color
            boolean isChangingThisDate = changingDateMode != null && changingDateMode.equals(isArrival ? "arrival" : "departure");
            if (isChangingThisDate) {
                badge.getStyleClass().add("changing");
            } else if (isArrival) {
                badge.getStyleClass().add("arrival");
            } else {
                badge.getStyleClass().add("departure");
            }
            StackPane.setAlignment(badge, isArrival ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(-8, isArrival ? -4 : 0, 0, isDeparture ? -4 : 0));
            wrapper.getChildren().add(badge);
        } else if (!isFestival && (isArrival || isDeparture || isSameDay)) {
            // For non-festival days, badge at bottom
            String badgeText = isSameDay ? "DAY VISIT" : (isArrival ? "→ ARRIVAL" : "← DEPARTURE");
            Label badge = new Label(badgeText);
            badge.getStyleClass().add("bookingpage-festival-day-badge-large");
            // Add state class for color - also determines padding
            if (isSameDay) {
                badge.getStyleClass().add("day-visit");
                badge.setPadding(new Insets(4, 10, 4, 10)); // Wider padding for DAY VISIT
            } else if (isArrival) {
                badge.getStyleClass().add("arrival");
                badge.setPadding(new Insets(4, 8, 4, 8));
            } else {
                badge.getStyleClass().add("departure");
                badge.setPadding(new Insets(4, 8, 4, 8));
            }
            StackPane.setAlignment(badge, Pos.BOTTOM_CENTER);
            StackPane.setMargin(badge, new Insets(0, 0, -10, 0));
            wrapper.getChildren().add(badge);
        }

        // Click handler
        if (!isDisabled && isClickable) {
            card.setOnMouseClicked(e -> handleDayClick(day));
        }

        return wrapper;
    }

    protected boolean isDateDisabled(LocalDate date, LocalDate arrival, LocalDate departure) {
        if (changingDateMode == null) return false;

        // Day visitors have no minimum nights (can arrive and depart same day)
        // All other accommodations require at least 1 night
        int effectiveMinNights = isDayVisitor ? 0 : Math.max(1, minNightsConstraint);

        if (changingDateMode.equals("arrival") && departure != null) {
            // Can't arrive after departure
            if (date.isAfter(departure)) return true;
            // Check min nights constraint - only count nights within main event period
            long nightsIfSelected = calculateNightsWithinMainEvent(date, departure);
            if (nightsIfSelected < effectiveMinNights) return true;
        } else if (changingDateMode.equals("departure") && arrival != null) {
            // Can't depart before arrival
            if (date.isBefore(arrival)) return true;
            // Check min nights constraint - only count nights within main event period
            long nightsIfSelected = calculateNightsWithinMainEvent(arrival, date);
            if (nightsIfSelected < effectiveMinNights) return true;
        }
        return false;
    }

    /**
     * Calculates the number of nights that overlap with the main event period.
     * Only nights within the main event (mainEventStartDate to mainEventEndDate) count
     * toward the minimum nights constraint. Early arrival and late departure nights
     * do NOT count.
     *
     * @param arrival   The arrival date
     * @param departure The departure date
     * @return Number of nights within the main event period
     */
    protected long calculateNightsWithinMainEvent(LocalDate arrival, LocalDate departure) {
        // Fall back to total nights if main event boundaries not set
        if (mainEventStartDate == null || mainEventEndDate == null) {
            return java.time.temporal.ChronoUnit.DAYS.between(arrival, departure);
        }

        // Calculate overlap: max(arrival, eventStart) to min(departure, eventEnd)
        LocalDate overlapStart = arrival.isAfter(mainEventStartDate) ? arrival : mainEventStartDate;
        LocalDate overlapEnd = departure.isBefore(mainEventEndDate) ? departure : mainEventEndDate;

        // No overlap if start >= end
        if (!overlapStart.isBefore(overlapEnd)) {
            return 0;
        }

        return java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd);
    }

    protected boolean isDateClickable(LocalDate date, LocalDate arrival, LocalDate departure) {
        if (arrival == null) return true;
        // When selecting departure:
        // - Day visitors can depart same day as arrival (0 nights)
        // - All others must depart at least 1 day after arrival
        if (departure == null) {
            return isDayVisitor ? !date.isBefore(arrival) : date.isAfter(arrival);
        }
        if (changingDateMode != null) return true;
        return false;
    }

    protected void handleDayClick(FestivalDay day) {
        LocalDate date = day.getDate();
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();

        // If we're in changing mode, handle the date change
        if (changingDateMode != null && arrival != null && departure != null) {
            if (changingDateMode.equals("arrival")) {
                arrivalDateProperty.set(date);
            } else if (changingDateMode.equals("departure")) {
                departureDateProperty.set(date);
            }
            changingDateMode = null;
            updateSectionVisibility();
            rebuildDayCards();
            return;
        }

        // Normal mode: First click sets arrival, second click sets departure
        if (arrival == null) {
            arrivalDateProperty.set(date);
            arrivalTimeProperty.set(defaultArrivalTime);  // Use computed default from boundary meals
        } else if (departure == null) {
            // Day visitors can depart same day as arrival (0 nights)
            // All others must depart at least 1 day after arrival
            boolean validDeparture = isDayVisitor ? !date.isBefore(arrival) : date.isAfter(arrival);
            if (validDeparture) {
                departureDateProperty.set(date);
                departureTimeProperty.set(defaultDepartureTime);  // Use computed default from boundary meals
            }
        }
    }

    protected String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.YourStayAndFestivalDays;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // ========================================
    // HasFestivalDaySelectionSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setFestivalDays(List<FestivalDay> days) {
        this.festivalDays = new ArrayList<>(days);
        UiScheduler.runInUiThread(this::rebuildDayCards);
    }

    @Override
    public void setEarliestArrivalDate(LocalDate date) {
        this.earliestArrivalDate = date;
    }

    @Override
    public void setLatestDepartureDate(LocalDate date) {
        this.latestDepartureDate = date;
    }

    @Override
    public void setEventStartDate(LocalDate date) {
        this.defaultArrivalDate = date;
    }

    @Override
    public void setEventEndDate(LocalDate date) {
        this.defaultDepartureDate = date;
    }

    /**
     * Sets the main event start date (the actual start of the main event, excluding early arrival).
     * This is used to correctly determine meal boundaries for arrival time selection.
     */
    public void setMainEventStartDate(LocalDate date) {
        this.mainEventStartDate = date;
    }

    /**
     * Sets the main event end date (the actual end of the main event, excluding late departure).
     * This is used to correctly determine meal boundaries for departure time selection.
     */
    public void setMainEventEndDate(LocalDate date) {
        this.mainEventEndDate = date;
    }

    /**
     * Sets whether there's a late departure part for this event.
     * When true, the EVENING departure option will be available on the main event end date
     * since late departure provides additional dinner meal.
     */
    public void setHasLateDeparturePart(boolean hasLateDeparture) {
        this.hasLateDeparturePart = hasLateDeparture;
        // Rebuild time sections if needed to show/hide EVENING option
        if (departureDateProperty.get() != null) {
            rebuildTimeSections();
        }
    }

    /**
     * Sets the end date of the late departure part.
     * This extends the calendar to include late departure days (e.g., April 30, May 1)
     * so users can select departure dates during the late departure period.
     */
    public void setLateDepartureEndDate(LocalDate date) {
        this.lateDepartureEndDate = date;
    }

    @Override
    public void setMinNightsConstraint(int minNights) {
        this.minNightsConstraint = minNights;
        // Rebuild cards to reflect constraint changes
        rebuildDayCards();
    }

    @Override
    public void setIsDayVisitor(boolean isDayVisitor) {
        this.isDayVisitor = isDayVisitor;
        // Update validity and rebuild cards when day visitor status changes
        updateValidity();
    }

    @Override
    public void reset() {
        // Reset date selections to event defaults (first and last festival days)
        arrivalDateProperty.set(defaultArrivalDate);
        departureDateProperty.set(defaultDepartureDate);
        // Reset times to computed defaults from boundary meals (or AFTERNOON as fallback)
        arrivalTimeProperty.set(defaultArrivalTime);
        departureTimeProperty.set(defaultDepartureTime);
        // Clear changing mode
        changingDateMode = null;
        // Update UI
        updateSectionVisibility();
        rebuildDayCards();
        // Notify listeners of the date change
        notifyDatesChanged();
    }

    @Override
    public void setArrivalDate(LocalDate date) {
        arrivalDateProperty.set(date);
    }

    @Override
    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDateProperty;
    }

    @Override
    public void setDepartureDate(LocalDate date) {
        departureDateProperty.set(date);
    }

    @Override
    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDateProperty;
    }

    @Override
    public void setArrivalTime(ArrivalDepartureTime time) {
        arrivalTimeProperty.set(time);
    }

    @Override
    public ObjectProperty<ArrivalDepartureTime> arrivalTimeProperty() {
        return arrivalTimeProperty;
    }

    @Override
    public void setDepartureTime(ArrivalDepartureTime time) {
        departureTimeProperty.set(time);
    }

    @Override
    public ObjectProperty<ArrivalDepartureTime> departureTimeProperty() {
        return departureTimeProperty;
    }

    @Override
    public void setOnDatesChanged(BiConsumer<LocalDate, LocalDate> callback) {
        this.onDatesChanged = callback;
    }

    @Override
    public int getSelectedDaysCount() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        if (arrival == null || departure == null) {
            return 0;
        }
        return (int) festivalDays.stream()
            .filter(day -> day.isFestivalDay())
            .filter(day -> !day.getDate().isBefore(arrival) && !day.getDate().isAfter(departure))
            .count();
    }

    @Override
    public int getTotalTeachingPrice() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        if (arrival == null || departure == null) {
            return 0;
        }
        return festivalDays.stream()
            .filter(day -> day.isFestivalDay())
            .filter(day -> !day.getDate().isBefore(arrival) && !day.getDate().isAfter(departure))
            .mapToInt(FestivalDay::getTeachingPrice)
            .sum();
    }

    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            return;
        }

        // Clear boundary meal limits (will be repopulated from EventPart boundaries)
        firstMealOnDate.clear();
        lastMealOnDate.clear();
        earlyArrivalFirstMealOnDate.clear();
        earlyArrivalAvailableDates.clear();
        lateDepartureTransitionDate = null;
        lateDepartureFirstMeal = null;

        Event event = policyAggregate.getEvent();
        if (event == null) {
            return;
        }

        // Get teaching ScheduledItems and map them by date
        List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
        Map<LocalDate, ScheduledItem> teachingsByDate = new HashMap<>();
        LocalDate firstTeachingDate = null;
        LocalDate lastTeachingDate = null;

        for (ScheduledItem si : teachingItems) {
            if (si.getDate() != null) {
                teachingsByDate.put(si.getDate(), si);
                if (firstTeachingDate == null || si.getDate().isBefore(firstTeachingDate)) {
                    firstTeachingDate = si.getDate();
                }
                if (lastTeachingDate == null || si.getDate().isAfter(lastTeachingDate)) {
                    lastTeachingDate = si.getDate();
                }
            }
        }

        if (firstTeachingDate == null || lastTeachingDate == null) {
            return;
        }

        // Get event boundary dates from Event and EventParts (preferred method)
        // - Main event dates from Event entity
        // - Early arrival from EarlyArrivalPart
        // - Late departure from LateDeparturePart
        LocalDate earlyArrivalDate = null;
        LocalDate eventStartDate = null;
        LocalDate eventEndDate = null;
        LocalDate lateDepartureDate = null;

        // Get main event dates from Event entity (authoritative source)
        eventStartDate = event.getStartDate();
        eventEndDate = event.getEndDate();

        // Get early arrival date from EarlyArrivalPart
        EventPart earlyArrivalPart = policyAggregate.getEarlyArrivalPart();
        if (earlyArrivalPart != null) {
            earlyArrivalDate = earlyArrivalPart.getStartDate();
        }

        // Get late departure date from LateDeparturePart
        EventPart lateDeparturePart = policyAggregate.getLateDeparturePart();
        if (lateDeparturePart != null) {
            lateDepartureDate = lateDeparturePart.getEndDate();
        }

        // If PolicyAggregate methods returned null, find earliest/latest dates from all EventParts
        // This handles cases where the Event dates include early arrival/late departure
        EntityList<EventPart> eventParts = policyAggregate.getEventParts();
        if (eventParts != null && !eventParts.isEmpty()) {
            LocalDate earliestStart = null;
            LocalDate latestEnd = null;
            for (EventPart part : eventParts) {
                LocalDate partStart = part.getStartDate();
                LocalDate partEnd = part.getEndDate();
                if (partStart != null && (earliestStart == null || partStart.isBefore(earliestStart))) {
                    earliestStart = partStart;
                }
                if (partEnd != null && (latestEnd == null || partEnd.isAfter(latestEnd))) {
                    latestEnd = partEnd;
                }
            }
            // Use earliest start as early arrival date if not already set
            if (earlyArrivalDate == null && earliestStart != null) {
                earlyArrivalDate = earliestStart;
            }
            // Use latest end as late departure date if not already set
            if (lateDepartureDate == null && latestEnd != null) {
                lateDepartureDate = latestEnd;
            }
        }

        // Store Early Arrival boundaries - first meal available for early arrivals
        if (earlyArrivalPart != null) {
            ScheduledBoundary startBoundary = earlyArrivalPart.getStartBoundary();
            ScheduledBoundary endBoundary = earlyArrivalPart.getEndBoundary();
            logBoundaryInfo("EarlyArrival.start", startBoundary);
            logBoundaryInfo("EarlyArrival.end", endBoundary);

            // Mark the main event start date as having early arrival available
            // This means we should show all scheduled meals on this date (not restricted by main event boundary)
            if (eventStartDate != null) {
                earlyArrivalAvailableDates.add(eventStartDate);
            }

            // Store the FIRST meal for early arrivals (start boundary)
            // This tells us what meal is available when early arrivals can start eating
            if (startBoundary != null) {
                ScheduledItem firstMealOnEarlyArrival = startBoundary.getScheduledItem();
                // Use the boundary's date (from scheduled item), not the part's start date
                // This ensures we store for the correct date (e.g., Apr 24) even if part dates differ
                LocalDate firstMealDate = ScheduledBoundaries.getDate(startBoundary);
                if (firstMealOnEarlyArrival != null && firstMealDate != null) {
                    // Store in earlyArrivalFirstMealOnDate for DISPLAY purposes
                    // (shows what meals early arrivals can get, separate from main event booking logic)
                    storeEarlyArrivalFirstMeal(firstMealDate, firstMealOnEarlyArrival);
                    // Also store in firstMealOnDate (may be overwritten by main event)
                    storeBoundaryMealInfo(firstMealDate, firstMealOnEarlyArrival, true);  // true = isFirstMeal
                }
            }
        }

        // Track late departure start date to avoid restricting meals on days that have late departure
        LocalDate lateDepartureStartDate = null;
        if (lateDeparturePart != null) {
            ScheduledBoundary startBoundary = lateDeparturePart.getStartBoundary();
            ScheduledBoundary endBoundary = lateDeparturePart.getEndBoundary();
            logBoundaryInfo("LateDeparture.start", startBoundary);
            logBoundaryInfo("LateDeparture.end", endBoundary);

            // Track the late departure start date - meals on this day should NOT be restricted
            // by the main event's end boundary, since late departure continues with more meals
            lateDepartureStartDate = lateDeparturePart.getStartDate();

            // Store the FIRST meal on late departure start date
            // This is the first meal that late departures get on the transition day (e.g., dinner on event end date)
            if (startBoundary != null) {
                ScheduledItem firstMealOnLateDepartureStart = startBoundary.getScheduledItem();
                if (firstMealOnLateDepartureStart != null && lateDepartureStartDate != null) {
                    String mealName = firstMealOnLateDepartureStart.getItem() != null ?
                        firstMealOnLateDepartureStart.getItem().getName() : null;
                    // Store in separate fields for late departure transition tracking
                    // This tells us what ADDITIONAL meal late departures get on the event end date
                    lateDepartureTransitionDate = lateDepartureStartDate;
                    if (mealName != null) {
                        mealName = mealName.toLowerCase();
                        if (mealName.contains("breakfast") || mealName.contains("morning")) {
                            lateDepartureFirstMeal = "breakfast";
                        } else if (mealName.contains("lunch") || mealName.contains("midday") || mealName.contains("noon")) {
                            lateDepartureFirstMeal = "lunch";
                        } else if (mealName.contains("dinner") || mealName.contains("evening") || mealName.contains("supper")) {
                            lateDepartureFirstMeal = "dinner";
                        }
                    }
                }
            }

            // The endBoundary.scheduledItem tells us the LAST meal available on the late departure day
            // Store this for use in meal availability detection
            if (endBoundary != null) {
                ScheduledItem lastMealOnLateDeparture = endBoundary.getScheduledItem();
                if (lastMealOnLateDeparture != null && lateDepartureDate != null) {
                    // Store the last meal info for this date - will be used by loadMealTimesFromPolicyAggregate
                    storeBoundaryMealInfo(lateDepartureDate, lastMealOnLateDeparture, false);
                }
            }
        }
        // Make lateDepartureStartDate effectively final for use in lambda
        final LocalDate finalLateDepartureStartDate = lateDepartureStartDate;
        // Make earlyArrivalDate effectively final for use below
        final LocalDate finalEarlyArrivalDate = earlyArrivalDate;
        // Also check main event part boundaries
        if (eventParts != null) {
            for (EventPart part : eventParts) {
                String partName = part.getName() != null ? part.getName() : "unnamed";
                logBoundaryInfo(partName + ".start", part.getStartBoundary());
                logBoundaryInfo(partName + ".end", part.getEndBoundary());

                // Check if this is the main event part (not early arrival or late departure)
                // Use mainEventStartDate/mainEventEndDate if set (from USFestivalBookingForm),
                // otherwise fall back to event dates
                LocalDate partStartDate = part.getStartDate();
                LocalDate partEndDate = part.getEndDate();
                LocalDate targetStartDate = mainEventStartDate != null ? mainEventStartDate : eventStartDate;
                LocalDate targetEndDate = mainEventEndDate != null ? mainEventEndDate : eventEndDate;
                boolean isMainEventPart = targetStartDate != null && targetEndDate != null &&
                    partStartDate != null && partEndDate != null &&
                    partStartDate.equals(targetStartDate) && partEndDate.equals(targetEndDate);

                if (isMainEventPart) {
                    // Store the first meal from the main event's start boundary
                    ScheduledBoundary startBoundary = part.getStartBoundary();
                    if (startBoundary != null) {
                        ScheduledItem firstMealOfEvent = startBoundary.getScheduledItem();
                        if (firstMealOfEvent != null) {
                            LocalDate firstMealDate = ScheduledBoundaries.getDate(startBoundary);
                            storeBoundaryMealInfo(firstMealDate, firstMealOfEvent, true);  // true = isFirstMeal
                        }
                    }
                    // Also store the last meal from the main event's end boundary
                    // BUT ONLY if there's no late departure starting on that date
                    // (late departure would include additional meals like dinner)
                    ScheduledBoundary endBoundary = part.getEndBoundary();
                    if (endBoundary != null) {
                        ScheduledItem lastMealOfEvent = endBoundary.getScheduledItem();
                        if (lastMealOfEvent != null) {
                            LocalDate lastMealDate = ScheduledBoundaries.getDate(endBoundary);

                            // Check if late departure starts on or before this date
                            // If so, don't restrict meals - late departure provides more meals on this day
                            boolean lateDepartureCoversThisDate = finalLateDepartureStartDate != null &&
                                !finalLateDepartureStartDate.isAfter(lastMealDate);
                            if (!lateDepartureCoversThisDate) {
                                storeBoundaryMealInfo(lastMealDate, lastMealOfEvent, false);  // false = isLastMeal
                            }
                        }
                    }
                }
            }
        }

        // Try to extract meal times from ScheduledBoundaries for arrival/departure defaults
        EntityList<ScheduledBoundary> boundaries = policyAggregate.getScheduledBoundaries();

        if (boundaries != null && boundaries.size() >= 3) {
            // Find the boundaries that correspond to event start and end
            for (ScheduledBoundary boundary : boundaries) {
                LocalDate boundaryDate = ScheduledBoundaries.getDate(boundary);
                if (boundaryDate != null) {
                    if (boundaryDate.equals(eventStartDate)) {
                        eventFirstMealEndTime = ScheduledBoundaries.getEndTime(boundary);
                    } else if (boundaryDate.equals(eventEndDate)) {
                        eventLastMealStartTime = ScheduledBoundaries.getStartTime(boundary);
                    }
                }
            }

            // Compute arrival deadline (10 min before first meal ends) and departure earliest (10 min after last meal starts)
            if (eventFirstMealEndTime != null) {
                arrivalDeadline = eventFirstMealEndTime.minusMinutes(10);
            }
            if (eventLastMealStartTime != null) {
                departureEarliest = eventLastMealStartTime.plusMinutes(10);
            }

            // Compute default arrival/departure time slots based on boundary meal times
            computeDefaultTimesFromBoundaries();
        }

        // Use early arrival start date and late departure end date for calendar boundaries
        // These values come from PolicyAggregate methods or fallback to earliest/latest EventPart dates
        LocalDate startDate = earlyArrivalDate != null ? earlyArrivalDate : firstTeachingDate.minusDays(2);
        LocalDate endDate = lateDepartureDate != null ? lateDepartureDate : lastTeachingDate.plusDays(2);

        // Store event boundary dates for use by other components
        if (eventStartDate != null) setEventStartDate(eventStartDate);
        if (eventEndDate != null) setEventEndDate(eventEndDate);

        // Get fallback daily teaching price from PolicyAggregate (used when no date-specific rate found)
        int fallbackDailyPrice = policyAggregate.getDailyRatePrice();

        // Create FestivalDay objects for each day from startDate to endDate
        List<FestivalDay> days = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayIndex = 0;

        while (!currentDate.isAfter(endDate)) {
            ScheduledItem teaching = teachingsByDate.get(currentDate);
            boolean isFestivalDay = teaching != null;

            // Get teaching title - prefer ScheduledItem's name/label over Item's name
            String title = null;
            Item teachingItem = null;
            if (teaching != null) {
                // First try ScheduledItem's own name
                title = teaching.getName();
                // If no name, try ScheduledItem's label
                if ((title == null || title.isEmpty()) && teaching.getLabel() != null) {
                    title = teaching.getLabel().getEn();
                }
                // Fall back to Item's name
                if ((title == null || title.isEmpty()) && teaching.getItem() != null) {
                    teachingItem = teaching.getItem();
                    title = teachingItem.getName();
                } else if (teaching.getItem() != null) {
                    teachingItem = teaching.getItem();
                }
            }

            // Calculate teaching price for this specific date
            // Matches rate by item AND date range (startDate <= date <= endDate)
            int teachingPrice = 0;
            if (isFestivalDay && teachingItem != null) {
                final Item finalItem = teachingItem;
                final LocalDate finalDate = currentDate;
                teachingPrice = policyAggregate.getDailyRatesStream()
                    .filter(r -> r.getItem() != null && r.getItem().getPrimaryKey() != null
                        && r.getItem().getPrimaryKey().equals(finalItem.getPrimaryKey()))
                    .filter(r -> {
                        LocalDate rateStartDate = r.getStartDate();
                        LocalDate rateEndDate = r.getEndDate();
                        // Rate applies if no date restriction or date falls within range
                        boolean startOk = rateStartDate == null || !finalDate.isBefore(rateStartDate);
                        boolean endOk = rateEndDate == null || !finalDate.isAfter(rateEndDate);
                        return startOk && endOk;
                    })
                    .findFirst()
                    .map(r -> r.getPrice() != null ? r.getPrice() : 0)
                    .orElse(fallbackDailyPrice);
            }

            FestivalDay day = new FestivalDay(dayIndex, currentDate, title, teachingPrice, isFestivalDay);
            days.add(day);

            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }

        // Set earliest/latest dates for early arrival / late departure
        setEarliestArrivalDate(startDate);
        setLatestDepartureDate(endDate);

        // Store default dates (used by reset() to restore to event dates)
        this.defaultArrivalDate = firstTeachingDate;
        this.defaultDepartureDate = lastTeachingDate;

        // IMPORTANT: Load meal times BEFORE setting festival days and dates
        // This populates mealsAvailableByDate map which is needed by time option filtering
        // when rebuildTimeSections() is called by the date change listeners
        loadMealTimesFromPolicyAggregate(policyAggregate);

        // Validate default times against actual meal availability
        // The defaults were computed from boundary times, but we need to ensure they're valid
        // for the specific dates (e.g., if no dinner on last day, can't default to EVENING)
        validateDefaultTimesAgainstMeals(firstTeachingDate, lastTeachingDate);

        // Set the festival days (this will trigger UI rebuild)
        setFestivalDays(days);

        // Set default arrival/departure to the first/last teaching days
        // NOTE: These trigger rebuildTimeSections() via listeners, which uses mealsAvailableByDate
        setArrivalDate(firstTeachingDate);
        setDepartureDate(lastTeachingDate);

        // Set initial arrival/departure times based on computed (and validated) defaults
        arrivalTimeProperty.set(defaultArrivalTime);
        departureTimeProperty.set(defaultDepartureTime);
    }

    /**
     * Loads meal times from MEALS ScheduledItems in PolicyAggregate.
     * Updates lunch and dinner start time fields based on API data.
     * These times are used to determine arrival/departure time slot boundaries.
     * Also populates mealsAvailableByDate map for dynamic meal note display.
     */
    protected void loadMealTimesFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) return;

        // Clear and rebuild the meals available by date map
        mealsAvailableByDate.clear();

        // Get all MEALS scheduled items
        List<ScheduledItem> mealItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);

        for (ScheduledItem mealSi : mealItems) {
            Item item = mealSi.getItem();
            if (item == null) continue;

            String itemName = item.getName();
            if (itemName == null) itemName = "";
            itemName = itemName.toLowerCase();

            // Try to get start time from ScheduledItem first, then from Timeline
            LocalTime startTime = mealSi.getStartTime();
            if (startTime == null && mealSi.getTimeline() != null) {
                startTime = mealSi.getTimeline().getStartTime();
            }

            // Track which meals are available on each date
            LocalDate mealDate = mealSi.getDate();
            if (mealDate != null) {
                java.util.Set<String> mealsOnDate = mealsAvailableByDate.computeIfAbsent(mealDate, k -> new java.util.HashSet<>());
                if (itemName.contains("breakfast") || itemName.contains("morning")) {
                    mealsOnDate.add("breakfast");
                } else if (itemName.contains("lunch") || itemName.contains("midday") || itemName.contains("noon")) {
                    mealsOnDate.add("lunch");
                } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                    mealsOnDate.add("dinner");
                }
            }

            // Match meal type and update corresponding time fields (only need start times)
            if (itemName.contains("lunch") || itemName.contains("midday") || itemName.contains("noon")) {
                if (startTime != null) {
                    lunchStartTime = startTime;
                }
            } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                if (startTime != null) {
                    dinnerStartTime = startTime;
                }
            }
        }

        // Note: rebuildTimeSections() not called here because this method is now called
        // BEFORE the UI is built. Time sections will be built/rebuilt when dates change.
    }

    /**
     * Validates and adjusts default times to ensure they INCLUDE the boundary meals.
     * The default selection should give users the first/last meals of the event.
     *
     * For arrival:
     * - If first meal is dinner → AFTERNOON (arrive before dinner)
     * - If first meal is lunch → MORNING (arrive before lunch)
     * - If first meal is breakfast → MORNING
     *
     * For departure:
     * - If last meal is lunch → AFTERNOON (stay through lunch)
     * - If last meal is dinner → EVENING (stay through dinner)
     * - If last meal is breakfast → MORNING
     */
    protected void validateDefaultTimesAgainstMeals(LocalDate arrivalDate, LocalDate departureDate) {
        // Set arrival default based on FIRST meal boundary - user should get the first meal
        if (arrivalDate != null) {
            String firstMeal = firstMealOnDate.get(arrivalDate);
            if (firstMeal != null) {
                // Select time that allows getting the first meal
                // dinner → AFTERNOON (arrive before dinner to get it)
                // lunch → MORNING (arrive before lunch to get it + dinner)
                // breakfast → MORNING (arrive before breakfast to get all)
                if ("dinner".equals(firstMeal)) {
                    defaultArrivalTime = ArrivalDepartureTime.AFTERNOON;
                } else if ("lunch".equals(firstMeal)) {
                    defaultArrivalTime = ArrivalDepartureTime.MORNING;
                } else if ("breakfast".equals(firstMeal)) {
                    defaultArrivalTime = ArrivalDepartureTime.MORNING;
                }
            }
        }

        // Set departure default based on LAST meal boundary - user should get the last meal
        if (departureDate != null) {
            String lastMeal = lastMealOnDate.get(departureDate);
            if (lastMeal != null) {
                // Select time that allows getting the last meal
                // lunch → AFTERNOON (stay through lunch)
                // dinner → EVENING (stay through dinner)
                // breakfast → MORNING (only breakfast available)
                if ("lunch".equals(lastMeal)) {
                    defaultDepartureTime = ArrivalDepartureTime.AFTERNOON;
                } else if ("dinner".equals(lastMeal)) {
                    defaultDepartureTime = ArrivalDepartureTime.EVENING;
                } else if ("breakfast".equals(lastMeal)) {
                    defaultDepartureTime = ArrivalDepartureTime.MORNING;
                }
            } else {
                // No boundary info (e.g., late departure covers this date) - default to AFTERNOON
                // AFTERNOON is a reasonable default as it includes lunch but not dinner
                defaultDepartureTime = ArrivalDepartureTime.AFTERNOON;
            }
        }
    }

    /**
     * Computes default arrival/departure time slots based on boundary meal times.
     * - Arrival: should be in the slot that includes the time BEFORE the first meal ends
     * - Departure: should be in the slot that includes the time AFTER the last meal starts
     */
    protected void computeDefaultTimesFromBoundaries() {
        // Determine default arrival time slot based on when guest must arrive
        if (arrivalDeadline != null) {
            if (arrivalDeadline.isBefore(LocalTime.of(12, 0))) {
                defaultArrivalTime = ArrivalDepartureTime.MORNING;
            } else if (arrivalDeadline.isBefore(LocalTime.of(17, 0))) {
                defaultArrivalTime = ArrivalDepartureTime.AFTERNOON;
            } else {
                defaultArrivalTime = ArrivalDepartureTime.EVENING;
            }
        }

        // Determine default departure time slot based on when guest can leave
        if (departureEarliest != null) {
            if (departureEarliest.isBefore(LocalTime.of(12, 0))) {
                defaultDepartureTime = ArrivalDepartureTime.MORNING;
            } else if (departureEarliest.isBefore(LocalTime.of(17, 0))) {
                defaultDepartureTime = ArrivalDepartureTime.AFTERNOON;
            } else {
                defaultDepartureTime = ArrivalDepartureTime.EVENING;
            }
        }
    }

    /**
     * Logs information about a ScheduledBoundary for debugging.
     */
    protected void logBoundaryInfo(String label, ScheduledBoundary boundary) {
        // Method kept for structure but logging removed
    }

    /**
     * Stores boundary meal information for a date.
     * This records the LAST meal available on a given date based on EventPart boundaries.
     *
     * @param date the date to record
     * @param scheduledItem the boundary scheduledItem (a meal)
     * @param isFirstMeal true if this is the first meal (arrival boundary), false if last meal (departure boundary)
     */
    protected void storeBoundaryMealInfo(LocalDate date, ScheduledItem scheduledItem, boolean isFirstMeal) {
        if (date == null || scheduledItem == null) return;

        Item item = scheduledItem.getItem();
        if (item == null) return;

        String itemName = item.getName();
        if (itemName == null) return;
        itemName = itemName.toLowerCase();

        // Determine meal type from item name
        String mealType = null;
        if (itemName.contains("breakfast") || itemName.contains("morning")) {
            mealType = "breakfast";
        } else if (itemName.contains("lunch") || itemName.contains("midday") || itemName.contains("noon")) {
            mealType = "lunch";
        } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
            mealType = "dinner";
        }

        if (mealType != null) {
            if (isFirstMeal) {
                // Store the first meal for this date - meals BEFORE this are not available
                firstMealOnDate.put(date, mealType);
            } else {
                // Store the last meal for this date - meals AFTER this are not available
                lastMealOnDate.put(date, mealType);
            }
        }
    }

    /**
     * Stores the first meal available for early arrivals on a specific date.
     * This is used for DISPLAY purposes to show what meals early arrivals can get,
     * separate from the main event's firstMealOnDate (used for booking logic).
     */
    protected void storeEarlyArrivalFirstMeal(LocalDate date, ScheduledItem scheduledItem) {
        if (date == null || scheduledItem == null) return;

        Item item = scheduledItem.getItem();
        if (item == null) return;

        String itemName = item.getName();
        if (itemName == null) return;
        itemName = itemName.toLowerCase();

        // Determine meal type from item name
        String mealType = null;
        if (itemName.contains("breakfast") || itemName.contains("morning")) {
            mealType = "breakfast";
        } else if (itemName.contains("lunch") || itemName.contains("midday") || itemName.contains("noon")) {
            mealType = "lunch";
        } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
            mealType = "dinner";
        }

        if (mealType != null) {
            earlyArrivalFirstMealOnDate.put(date, mealType);
        }
    }

    // === BOUNDARY INFO GETTERS ===

    /**
     * Gets the map of first meal available on each date (from boundary info).
     * Used to exclude meals BEFORE the first boundary meal on arrival days.
     */
    public Map<LocalDate, String> getFirstMealOnDate() {
        return firstMealOnDate;
    }

    /**
     * Gets the map of last meal available on each date (from boundary info).
     * Used to exclude meals AFTER the last boundary meal on departure days.
     */
    public Map<LocalDate, String> getLastMealOnDate() {
        return lastMealOnDate;
    }

    /**
     * Gets the date when late departure starts (usually same as main event end date).
     * On this date, late departures get additional meals beyond the main event.
     */
    public LocalDate getLateDepartureTransitionDate() {
        return lateDepartureTransitionDate;
    }

    /**
     * Gets the first meal available to late departures on the transition date.
     * This is the first ADDITIONAL meal beyond the main event (e.g., "dinner" if main event ends with lunch).
     */
    public String getLateDepartureFirstMeal() {
        return lateDepartureFirstMeal;
    }
}
