package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.console.Console;
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
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.knownitems.KnownItemFamily;
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

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox instructionBox;
    protected Label instructionLabel;
    protected FlowPane daysContainer;
    protected HBox changeDateButtonsBox;
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

        // Arrival time section
        arrivalTimeSection = buildTimeSelectionSection(true);
        arrivalTimeSection.setVisible(false);
        arrivalTimeSection.setManaged(false);

        // Departure time section
        departureTimeSection = buildTimeSelectionSection(false);
        departureTimeSection.setVisible(false);
        departureTimeSection.setManaged(false);

        container.getChildren().addAll(sectionHeader, instructionBox, daysContainer, changeDateButtonsBox, arrivalTimeSection, departureTimeSection);
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

        for (ArrivalDepartureTime time : ArrivalDepartureTime.values()) {
            VBox optionCard = createTimeOptionCard(time, timeProperty, isArrival);
            optionsRow.getChildren().add(optionCard);
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
        if (isArrival) {
            // Arrival times based on meal schedule - uses API meal times
            // Morning: arrive before lunch starts → get lunch + dinner
            // Afternoon: arrive after lunch starts but before dinner → get dinner only
            // Evening: arrive after dinner starts → no meals that day
            return switch (time) {
                case MORNING -> formatBefore(lunchStartTime);
                case AFTERNOON -> formatRange(lunchStartTime, dinnerStartTime);
                case EVENING -> formatAfter(dinnerStartTime);
            };
        } else {
            // Departure times based on meal schedule - uses API meal times
            // Morning: leave before lunch starts → breakfast only
            // Afternoon: leave after lunch starts but before dinner → breakfast + lunch
            // Evening: leave after dinner starts → all meals
            return switch (time) {
                case MORNING -> formatBefore(lunchStartTime);
                case AFTERNOON -> formatRange(lunchStartTime, dinnerStartTime);
                case EVENING -> formatAfter(dinnerStartTime);
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
        if (isArrival) {
            return switch (time) {
                case MORNING -> "\u2713 Lunch + Dinner"; // Checkmark
                case AFTERNOON -> "\u2713 Dinner only";
                case EVENING -> "\u2717 No meals"; // X mark
            };
        } else {
            return switch (time) {
                case MORNING -> "\u2713 Breakfast only";
                case AFTERNOON -> "\u2713 Breakfast + Lunch";
                case EVENING -> "\u2713 All meals";
            };
        }
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

        // Rebuild time sections when color scheme changes to apply new colors
        colorScheme.addListener((obs, old, newVal) -> rebuildTimeSections());
    }

    /**
     * Rebuilds the time selection sections with current color scheme.
     * Called when color scheme changes to apply new theme colors.
     */
    protected void rebuildTimeSections() {
        if (container == null || arrivalTimeSection == null || departureTimeSection == null) return;

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
            // Check min nights constraint
            long nightsIfSelected = java.time.temporal.ChronoUnit.DAYS.between(date, departure);
            if (nightsIfSelected < effectiveMinNights) return true;
        } else if (changingDateMode.equals("departure") && arrival != null) {
            // Can't depart before arrival
            if (date.isBefore(arrival)) return true;
            // Check min nights constraint
            long nightsIfSelected = java.time.temporal.ChronoUnit.DAYS.between(arrival, date);
            if (nightsIfSelected < effectiveMinNights) return true;
        }
        return false;
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
            arrivalTimeProperty.set(ArrivalDepartureTime.AFTERNOON);
        } else if (departure == null) {
            // Day visitors can depart same day as arrival (0 nights)
            // All others must depart at least 1 day after arrival
            boolean validDeparture = isDayVisitor ? !date.isBefore(arrival) : date.isAfter(arrival);
            if (validDeparture) {
                departureDateProperty.set(date);
                departureTimeProperty.set(ArrivalDepartureTime.AFTERNOON);  // Default to afternoon per user request
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
        // Reset times to defaults
        arrivalTimeProperty.set(ArrivalDepartureTime.AFTERNOON);
        departureTimeProperty.set(ArrivalDepartureTime.AFTERNOON);
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
            Console.log("DefaultFestivalDaySelectionSection: PolicyAggregate is null, skipping population");
            return;
        }

        Event event = policyAggregate.getEvent();
        if (event == null) {
            Console.log("DefaultFestivalDaySelectionSection: Event not available");
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
            Console.log("DefaultFestivalDaySelectionSection: No teaching dates found");
            return;
        }

        Console.log("DefaultFestivalDaySelectionSection: Found " + teachingItems.size() + " teaching items");
        Console.log("DefaultFestivalDaySelectionSection: First teaching: " + firstTeachingDate + ", Last teaching: " + lastTeachingDate);

        // Add 2 days before first teaching for early arrival and 2 days after for late departure
        LocalDate startDate = firstTeachingDate.minusDays(2);
        LocalDate endDate = lastTeachingDate.plusDays(2);

        Console.log("DefaultFestivalDaySelectionSection: Populating days from " + startDate + " to " + endDate);

        // Get fallback daily teaching price from PolicyAggregate (used when no date-specific rate found)
        int fallbackDailyPrice = policyAggregate.getDailyRatePrice();

        // Create FestivalDay objects for each day from startDate to endDate
        List<FestivalDay> days = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayIndex = 0;

        while (!currentDate.isAfter(endDate)) {
            ScheduledItem teaching = teachingsByDate.get(currentDate);
            boolean isFestivalDay = teaching != null;

            // Get teaching title from ScheduledItem.getItem().getName() if available
            String title = null;
            Item teachingItem = null;
            if (teaching != null && teaching.getItem() != null) {
                teachingItem = teaching.getItem();
                title = teachingItem.getName();
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

            Console.log("DefaultFestivalDaySelectionSection: Day " + dayIndex + " - " + currentDate +
                " isFestival=" + isFestivalDay + " title=" + title + " price=" + teachingPrice);

            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }

        // Set earliest/latest dates for early arrival / late departure
        setEarliestArrivalDate(startDate);
        setLatestDepartureDate(endDate);

        // Store default dates (used by reset() to restore to event dates)
        this.defaultArrivalDate = firstTeachingDate;
        this.defaultDepartureDate = lastTeachingDate;

        // Set the festival days (this will trigger UI rebuild)
        setFestivalDays(days);

        // Set default arrival/departure to the first/last teaching days
        setArrivalDate(firstTeachingDate);
        setDepartureDate(lastTeachingDate);

        // Load meal times from API to update time selection ranges
        loadMealTimesFromPolicyAggregate(policyAggregate);

        Console.log("DefaultFestivalDaySelectionSection: Populated " + days.size() + " festival days, default arrival: " + firstTeachingDate + ", departure: " + lastTeachingDate);
    }

    /**
     * Loads meal times from MEALS ScheduledItems in PolicyAggregate.
     * Updates lunch and dinner start time fields based on API data.
     * These times are used to determine arrival/departure time slot boundaries.
     */
    protected void loadMealTimesFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) return;

        // Get all MEALS scheduled items
        List<ScheduledItem> mealItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
        Console.log("DefaultFestivalDaySelectionSection: Found " + mealItems.size() + " meal scheduled items for time lookup");

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

            Console.log("DefaultFestivalDaySelectionSection: Meal '" + itemName + "' startTime=" + startTime);

            // Match meal type and update corresponding time fields (only need start times)
            if (itemName.contains("lunch") || itemName.contains("midday") || itemName.contains("noon")) {
                if (startTime != null) {
                    lunchStartTime = startTime;
                    Console.log("DefaultFestivalDaySelectionSection: Set lunchStartTime to " + lunchStartTime);
                }
            } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                if (startTime != null) {
                    dinnerStartTime = startTime;
                    Console.log("DefaultFestivalDaySelectionSection: Set dinnerStartTime to " + dinnerStartTime);
                }
            }
        }

        // Rebuild time sections to reflect new time values
        rebuildTimeSections();
    }
}
