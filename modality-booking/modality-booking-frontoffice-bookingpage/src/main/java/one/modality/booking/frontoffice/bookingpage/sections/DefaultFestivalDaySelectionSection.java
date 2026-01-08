package one.modality.booking.frontoffice.bookingpage.sections;

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
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
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
    protected final ObjectProperty<ArrivalDepartureTime> departureTimeProperty = new SimpleObjectProperty<>(ArrivalDepartureTime.MORNING);

    // === CONSTRAINT ===
    protected int minNightsConstraint = 0; // 0 means no constraint
    protected String changingDateMode = null; // null, "arrival", or "departure"

    // === DATA ===
    protected List<FestivalDay> festivalDays = new ArrayList<>();
    protected LocalDate earliestArrivalDate;
    protected LocalDate latestDepartureDate;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox instructionBox;
    protected Label instructionLabel;
    protected FlowPane daysContainer;
    protected HBox changeDateButtonsBox;
    protected VBox arrivalTimeSection;
    protected VBox departureTimeSection;

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
        // Use the standard info box helper
        HBox box = BookingPageUIBuilder.createInfoBox(BookingPageI18nKeys.FestivalDaysInstructions, BookingPageUIBuilder.InfoBoxType.INFO);

        // Get the message label from the box (second child after icon) for dynamic updates
        if (box.getChildren().size() > 1 && box.getChildren().get(1) instanceof Label) {
            instructionLabel = (Label) box.getChildren().get(1);
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
        section.setPadding(new Insets(16));
        section.getStyleClass().add("bookingpage-time-section");

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label clockIcon = new Label("\u23F0"); // Clock icon
        clockIcon.getStyleClass().addAll("bookingpage-text-base");

        Label titleLabel = I18nControls.newLabel(isArrival ? BookingPageI18nKeys.ArrivalTime : BookingPageI18nKeys.DepartureTime);
        titleLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-bold", "bookingpage-text-dark");

        header.getChildren().addAll(clockIcon, titleLabel);

        // Time options row
        HBox optionsRow = new HBox(12);
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
        VBox card = new VBox(4);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setMinWidth(140);
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-time-option");

        // Time label
        Label timeLabel = new Label(getTimeLabel(time));
        timeLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-bold");

        // Time range
        Label rangeLabel = new Label(getTimeRange(time));
        rangeLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        // Meal note
        Label mealLabel = new Label(getMealNote(time, isArrival));
        mealLabel.getStyleClass().addAll("bookingpage-text-xs");

        card.getChildren().addAll(timeLabel, rangeLabel, mealLabel);

        // Selection handling
        if (timeProperty.get() == time) {
            card.getStyleClass().add("selected");
        }
        timeProperty.addListener((obs, old, newVal) -> {
            if (newVal == time) {
                card.getStyleClass().add("selected");
            } else {
                card.getStyleClass().remove("selected");
            }
        });

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

    protected String getTimeRange(ArrivalDepartureTime time) {
        return switch (time) {
            case MORNING -> "Before 11:00";
            case AFTERNOON -> "11:00 - 14:00";
            case EVENING -> "After 14:00";
        };
    }

    protected String getMealNote(ArrivalDepartureTime time, boolean isArrival) {
        if (isArrival) {
            return switch (time) {
                case MORNING -> "\u2713 All meals"; // Checkmark
                case AFTERNOON -> "\u2713 Lunch + Dinner";
                case EVENING -> "\u2713 Dinner only";
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
    }

    protected void updateSectionVisibility() {
        boolean hasArrival = arrivalDateProperty.get() != null;
        boolean hasDeparture = departureDateProperty.get() != null;
        boolean hasBothDates = hasArrival && hasDeparture;

        arrivalTimeSection.setVisible(hasArrival);
        arrivalTimeSection.setManaged(hasArrival);
        departureTimeSection.setVisible(hasDeparture);
        departureTimeSection.setManaged(hasDeparture);

        // Show change date buttons only when both dates are set and not in changing mode
        boolean showChangeButtons = hasBothDates && changingDateMode == null;
        changeDateButtonsBox.setVisible(showChangeButtons);
        changeDateButtonsBox.setManaged(showChangeButtons);

        // Update instruction text based on current state
        updateInstructionText();
    }

    protected void updateInstructionText() {
        if (instructionLabel == null || instructionBox == null) return;

        boolean hasArrival = arrivalDateProperty.get() != null;
        boolean hasDeparture = departureDateProperty.get() != null;

        Object i18nKey;
        if ("arrival".equals(changingDateMode)) {
            i18nKey = BookingPageI18nKeys.FestivalDaysChangingArrival;
        } else if ("departure".equals(changingDateMode)) {
            i18nKey = BookingPageI18nKeys.FestivalDaysChangingDeparture;
        } else if (hasArrival && !hasDeparture) {
            i18nKey = BookingPageI18nKeys.FestivalDaysSelectDeparture;
        } else {
            // Always show instructions (explains prices are for teachings)
            i18nKey = BookingPageI18nKeys.FestivalDaysInstructions;
        }

        I18nControls.bindI18nTextProperty(instructionLabel, i18nKey);

        // Always keep the instruction box visible
        instructionBox.setVisible(true);
        instructionBox.setManaged(true);
    }

    protected void updateValidity() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        boolean isValid = arrival != null && departure != null && !departure.isBefore(arrival);
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

        BookingFormColorScheme colors = colorScheme.get();
        String primaryHex = colorToHex(colors.getPrimary());
        String selectedBgHex = colorToHex(colors.getSelectedBg());
        String darkTextHex = colorToHex(colors.getDarkText());
        String hoverBorderHex = colorToHex(colors.getHoverBorder());

        // Determine if date is disabled based on constraints
        boolean isDisabled = isDateDisabled(date, arrival, departure);
        boolean isClickable = isDateClickable(date, arrival, departure);

        // Card container (VBox inside StackPane for badge positioning)
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 6, isFestival ? 8 : 10, 6));
        card.setMinWidth(100);
        card.setPrefWidth(100);
        card.setCursor(isDisabled ? Cursor.DEFAULT : (isClickable ? Cursor.HAND : Cursor.DEFAULT));

        // Determine border and background colors based on state
        String borderColor;
        String bgColor;
        if (isDisabled) {
            borderColor = "#D1D1D1";
            bgColor = "#F5F5F5";
        } else if (isArrival) {
            borderColor = primaryHex;
            bgColor = selectedBgHex;
        } else if (isDeparture) {
            borderColor = darkTextHex;
            bgColor = selectedBgHex;
        } else if (changingDateMode != null && isClickable) {
            borderColor = "#FF6B35";
            bgColor = "#FFF4ED";
        } else if (isInStay) {
            borderColor = hoverBorderHex;
            bgColor = selectedBgHex;
        } else if (isFestival) {
            borderColor = "#E6E7E7";
            bgColor = "#FAFBFC";
        } else {
            borderColor = "#E6E7E7";
            bgColor = "white";
        }

        card.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + borderColor + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            (isDisabled ? "-fx-opacity: 0.4;" : "")
        );

        // Text colors based on state
        String weekdayColor = isArrival ? primaryHex : (isDeparture ? darkTextHex : "#838788");
        String dayColor = isArrival ? primaryHex : (isDeparture ? darkTextHex : "#292A33");
        String monthColor = weekdayColor;

        // Weekday (10px, uppercase, semibold)
        Label weekdayLabel = new Label(day.getWeekday().toUpperCase());
        weekdayLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-text-fill: " + weekdayColor + ";");

        // Day number (22px, bold)
        Label dayLabel = new Label(String.valueOf(day.getDayOfMonth()));
        dayLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: " + dayColor + "; -fx-line-height: 1;");

        // Month (9px, uppercase, semibold)
        Label monthLabel = new Label(day.getMonthShort().toUpperCase());
        monthLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: 600; -fx-text-fill: " + monthColor + ";");

        card.getChildren().addAll(weekdayLabel, dayLabel, monthLabel);

        // Teaching info section (only for festival days)
        if (isFestival) {
            VBox.setMargin(monthLabel, new Insets(0, 0, 8, 0));

            // Separator line
            Region separator = new Region();
            separator.setMinHeight(1);
            separator.setMaxHeight(1);
            String separatorColor = (isInStay || isArrival || isDeparture) ? hoverBorderHex : "#E6E7E7";
            separator.setStyle("-fx-background-color: " + separatorColor + ";");
            VBox.setMargin(separator, new Insets(4, 0, 6, 0));

            // Teaching title (10px, semibold)
            String titleColor = (isInStay || isArrival || isDeparture) ? primaryHex : "#495057";
            Label titleLabel = new Label(day.getTitle() != null ? day.getTitle() : "");
            titleLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-text-fill: " + titleColor + "; -fx-line-height: 1.2;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(88);
            titleLabel.setMinHeight(24);
            titleLabel.setAlignment(Pos.CENTER);

            // Price (13px, bold)
            String priceColor = (isInStay || isArrival || isDeparture) ? primaryHex : "#2C3E50";
            Label priceLabel = new Label(formatPrice(day.getTeachingPrice()));
            priceLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + priceColor + ";");

            card.getChildren().addAll(separator, titleLabel, priceLabel);
        }

        // Wrap in StackPane for badge positioning
        StackPane wrapper = new StackPane(card);
        wrapper.setAlignment(Pos.TOP_CENTER);

        // Arrival/Departure badge
        if (isFestival && (isArrival || isDeparture)) {
            // For festival days, badge goes at top-right or top-left
            Label badge = new Label(isArrival ? "ARRIVAL" : "DEPARTURE");
            String badgeColor = isArrival ? primaryHex : darkTextHex;
            if (changingDateMode != null && changingDateMode.equals(isArrival ? "arrival" : "departure")) {
                badgeColor = "#FF6B35"; // Orange when changing
            }
            badge.setStyle(
                "-fx-background-color: " + badgeColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 2 6; " +
                "-fx-background-radius: 4; " +
                "-fx-font-size: 8px; " +
                "-fx-font-weight: 700;"
            );
            StackPane.setAlignment(badge, isArrival ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(-8, isArrival ? -4 : 0, 0, isDeparture ? -4 : 0));
            wrapper.getChildren().add(badge);
        } else if (!isFestival && (isArrival || isDeparture || isSameDay)) {
            // For non-festival days, badge at bottom
            String badgeText = isSameDay ? "DAY VISIT" : (isArrival ? "→ ARRIVAL" : "← DEPARTURE");
            Label badge = new Label(badgeText);
            String badgeColor = isSameDay ? primaryHex : (isArrival ? primaryHex : darkTextHex);
            badge.setStyle(
                "-fx-background-color: " + badgeColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 4 " + (isSameDay ? "10" : "8") + "; " +
                "-fx-background-radius: 6; " +
                "-fx-font-size: 9px; " +
                "-fx-font-weight: 700;"
            );
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

        if (changingDateMode.equals("arrival") && departure != null) {
            // Can't arrive after departure
            if (date.isAfter(departure)) return true;
            // Check min nights constraint
            if (minNightsConstraint > 0) {
                long nightsIfSelected = java.time.temporal.ChronoUnit.DAYS.between(date, departure);
                if (nightsIfSelected < minNightsConstraint) return true;
            }
        } else if (changingDateMode.equals("departure") && arrival != null) {
            // Can't depart before arrival
            if (date.isBefore(arrival)) return true;
            // Check min nights constraint
            if (minNightsConstraint > 0) {
                long nightsIfSelected = java.time.temporal.ChronoUnit.DAYS.between(arrival, date);
                if (nightsIfSelected < minNightsConstraint) return true;
            }
        }
        return false;
    }

    protected boolean isDateClickable(LocalDate date, LocalDate arrival, LocalDate departure) {
        if (arrival == null) return true;
        if (departure == null) return !date.isBefore(arrival);
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
            if (!date.isBefore(arrival)) {
                departureDateProperty.set(date);
                departureTimeProperty.set(ArrivalDepartureTime.MORNING);
            }
        }
    }

    protected String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    protected String colorToHex(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
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

        // Get daily teaching price from PolicyAggregate
        int dailyTeachingPrice = policyAggregate.getDailyRatePrice();

        // Create FestivalDay objects for each day from startDate to endDate
        List<FestivalDay> days = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayIndex = 0;

        while (!currentDate.isAfter(endDate)) {
            ScheduledItem teaching = teachingsByDate.get(currentDate);
            boolean isFestivalDay = teaching != null;

            // Get teaching title from ScheduledItem.getItem().getName() if available
            String title = null;
            if (teaching != null && teaching.getItem() != null) {
                title = teaching.getItem().getName();
            }

            // Teaching price is only for festival days (days with teachings)
            int teachingPrice = isFestivalDay ? dailyTeachingPrice : 0;

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

        // Set the festival days (this will trigger UI rebuild)
        setFestivalDays(days);

        // Set default arrival/departure to the first/last teaching days
        setArrivalDate(firstTeachingDate);
        setDepartureDate(lastTeachingDate);

        Console.log("DefaultFestivalDaySelectionSection: Populated " + days.size() + " festival days, default arrival: " + firstTeachingDate + ", departure: " + lastTeachingDate);
    }
}
