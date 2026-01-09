package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.console.Console;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the meals selection section.
 * Displays meal options (breakfast/lunch/dinner) with dietary preferences.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Checkbox toggles for Breakfast, Lunch and Dinner</li>
 *   <li>Price per day display</li>
 *   <li>Dietary preference radio buttons</li>
 *   <li>Info text about meals</li>
 *   <li>Breakfast auto-included with accommodation</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-meals-section} - section container</li>
 *   <li>{@code .bookingpage-checkbox-card} - meal toggle card</li>
 *   <li>{@code .bookingpage-checkbox-card.selected} - selected state</li>
 *   <li>{@code .bookingpage-pill-option} - dietary preference option</li>
 *   <li>{@code .bookingpage-pill-option.selected} - selected dietary preference</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasMealsSelectionSection
 */
public class DefaultMealsSelectionSection implements HasMealsSelectionSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === ACCOMMODATION STATE ===
    // When true, breakfast is included automatically
    protected boolean hasAccommodation = true;  // Default to true (assume staying overnight)
    // When true, shows a note about different meal pricing outside event dates
    protected boolean hasExtendedStay = false;

    // === MEAL SELECTION ===
    // Meals are selected by default since accommodation price includes meals
    protected final BooleanProperty wantsBreakfast = new SimpleBooleanProperty(true);  // Auto-included with accommodation
    protected final BooleanProperty wantsLunch = new SimpleBooleanProperty(true);
    protected final BooleanProperty wantsDinner = new SimpleBooleanProperty(true);
    @Deprecated
    protected final ObjectProperty<DietaryPreference> dietaryPreference = new SimpleObjectProperty<>(DietaryPreference.VEGETARIAN);
    protected final ObjectProperty<Item> selectedDietaryItem = new SimpleObjectProperty<>();  // API-driven dietary option

    // === PRICING ===
    protected int breakfastPricePerDay = 0;  // Included with accommodation
    protected int lunchPricePerDay = 700; // Default $7
    protected int dinnerPricePerDay = 700; // Default $7
    protected int daysCount = 1;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox breakfastCard;
    protected HBox lunchCard;
    protected HBox dinnerCard;
    protected HBox dietarySection;
    protected Label infoLabel;
    protected Label extendedStayLabel;  // Secondary label for extended stay note
    protected VBox infoBox;  // Combined info box for both messages
    protected VBox mealsContainer;

    // === DIETARY OPTIONS FROM API ===
    protected List<Item> dietaryOptions = new ArrayList<>();  // Dietary options loaded from API

    // === DATE/TIME CONTEXT ===
    // These are set from the festival day section to calculate meal counts
    protected LocalDate arrivalDate;
    protected LocalDate departureDate;
    protected HasFestivalDaySelectionSection.ArrivalDepartureTime arrivalTime = HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
    protected HasFestivalDaySelectionSection.ArrivalDepartureTime departureTime = HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;

    // === SUMMARY UI ===
    protected VBox summaryBox;  // Summary info box showing meal counts

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultMealsSelectionSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(16);
        container.getStyleClass().add("bookingpage-meals-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.Meals, StyledSectionHeader.ICON_UTENSILS);

        // Combined info box with main info text and optional extended stay note
        infoBox = createCombinedInfoBox();

        // Meal toggles container
        mealsContainer = new VBox(12);

        // Breakfast toggle (only visible if has accommodation)
        breakfastCard = createBreakfastToggle();

        // Lunch toggle
        lunchCard = createMealToggle(BookingPageI18nKeys.Lunch, BookingPageI18nKeys.MiddayMeal, wantsLunch, lunchPricePerDay);

        // Dinner toggle
        dinnerCard = createMealToggle(BookingPageI18nKeys.Dinner, BookingPageI18nKeys.EveningMeal, wantsDinner, dinnerPricePerDay);

        mealsContainer.getChildren().addAll(breakfastCard, lunchCard, dinnerCard);

        // Update breakfast visibility based on accommodation
        updateBreakfastVisibility();

        // Dietary preference section (visible by default since meals are selected by default)
        dietarySection = buildDietaryPreferenceSection();
        dietarySection.setVisible(true);
        dietarySection.setManaged(true);

        // Summary box showing meal counts (initially hidden until dates are set)
        summaryBox = createSummaryBox();
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);

        container.getChildren().addAll(sectionHeader, infoBox, mealsContainer, dietarySection, summaryBox);
        VBox.setMargin(infoBox, new Insets(0, 0, 8, 0));
        VBox.setMargin(summaryBox, new Insets(8, 0, 0, 0));
    }

    /**
     * Creates a combined info box that shows the main info text and optionally the extended stay note.
     * Uses BookingPageUIBuilder.createInfoBox() for consistent styling via CSS.
     */
    protected VBox createCombinedInfoBox() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        // Main info box using helper (CSS-styled, matches "Price includes" boxes)
        HBox mainInfoBox = BookingPageUIBuilder.createInfoBox(BookingPageI18nKeys.AllMealsVegetarian, BookingPageUIBuilder.InfoBoxType.NEUTRAL);

        // Get the label from the info box for potential updates (first child since NEUTRAL has no icon)
        if (!mainInfoBox.getChildren().isEmpty() && mainInfoBox.getChildren().get(0) instanceof Label) {
            infoLabel = (Label) mainInfoBox.getChildren().get(0);
        }

        // Extended stay label (hidden by default) - shown below main info box when needed
        extendedStayLabel = I18nControls.newLabel(BookingPageI18nKeys.ExtendedStayMealsNote);
        extendedStayLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-primary");
        extendedStayLabel.setPadding(new Insets(4, 0, 0, 28)); // Indent to align with text after icon
        extendedStayLabel.setVisible(false);
        extendedStayLabel.setManaged(false);

        box.getChildren().addAll(mainInfoBox, extendedStayLabel);
        return box;
    }

    /**
     * Creates the summary box that shows meal counts based on dates and selections.
     */
    protected VBox createSummaryBox() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12, 16, 12, 16));
        // Neutral style matching other info boxes - using CSS class
        box.getStyleClass().addAll("bookingpage-info-box", "bookingpage-info-box-neutral");
        return box;
    }

    /**
     * Updates the summary box content based on current meal selections and dates.
     */
    protected void updateSummary() {
        if (summaryBox == null) return;

        // Clear previous content
        summaryBox.getChildren().clear();

        // Check if we have valid dates
        if (arrivalDate == null || departureDate == null) {
            summaryBox.setVisible(false);
            summaryBox.setManaged(false);
            return;
        }

        // Check if any meals are selected
        boolean hasBreakfast = wantsBreakfast.get() && hasAccommodation;
        boolean hasLunch = wantsLunch.get();
        boolean hasDinner = wantsDinner.get();

        if (!hasBreakfast && !hasLunch && !hasDinner) {
            summaryBox.setVisible(false);
            summaryBox.setManaged(false);
            return;
        }

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.YourMeals);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        summaryBox.getChildren().add(titleLabel);

        // Calculate and display meal counts
        DateTimeFormatter dayMonthFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

        if (hasBreakfast) {
            int count = calculateBreakfastCount();
            if (count > 0) {
                String text = formatMealSummaryLine("Breakfast", count, getBreakfastDateRange(), dayMonthFormat);
                Label label = new Label(text);
                label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
                summaryBox.getChildren().add(label);
            }
        }

        if (hasLunch) {
            int count = calculateLunchCount();
            if (count > 0) {
                String text = formatMealSummaryLine("Lunch", count, getLunchDateRange(), dayMonthFormat);
                Label label = new Label(text);
                label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
                summaryBox.getChildren().add(label);
            }
        }

        if (hasDinner) {
            int count = calculateDinnerCount();
            if (count > 0) {
                String text = formatMealSummaryLine("Dinner", count, getDinnerDateRange(), dayMonthFormat);
                Label label = new Label(text);
                label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
                summaryBox.getChildren().add(label);
            }
        }

        // Only show if we have at least one meal line
        boolean hasContent = summaryBox.getChildren().size() > 1;  // More than just title
        summaryBox.setVisible(hasContent);
        summaryBox.setManaged(hasContent);
    }

    /**
     * Formats a meal summary line like "4 Breakfasts: Fri 25 Apr - Mon 28 Apr" or "1 Lunch: Fri 25 Apr"
     */
    protected String formatMealSummaryLine(String mealName, int count, LocalDate[] dateRange, DateTimeFormatter formatter) {
        String plural = count == 1 ? "" : "s";
        if (dateRange == null || dateRange[0] == null) {
            return count + " " + mealName + plural;
        }

        String fromDate = dateRange[0].format(formatter);
        if (dateRange[1] == null || dateRange[0].equals(dateRange[1])) {
            // Single day
            return count + " " + mealName + plural + ": " + fromDate;
        } else {
            // Date range
            String toDate = dateRange[1].format(formatter);
            return count + " " + mealName + plural + ": " + fromDate + " - " + toDate;
        }
    }

    /**
     * Calculates the number of breakfasts based on dates.
     * Breakfast is available each morning after an overnight stay.
     * First breakfast: Day after arrival
     * Last breakfast: Departure day (if departing Afternoon or Evening, not Morning)
     */
    protected int calculateBreakfastCount() {
        if (arrivalDate == null || departureDate == null || !hasAccommodation) return 0;

        // Day visitor (same day) = no breakfast
        if (arrivalDate.equals(departureDate)) return 0;

        long nights = java.time.temporal.ChronoUnit.DAYS.between(arrivalDate, departureDate);
        int count = (int) nights;

        // If departing in the morning (before breakfast), subtract one
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING) {
            count--;
        }

        return Math.max(0, count);
    }

    /**
     * Returns the date range for breakfasts [firstDate, lastDate].
     */
    protected LocalDate[] getBreakfastDateRange() {
        if (arrivalDate == null || departureDate == null || !hasAccommodation) return null;
        if (arrivalDate.equals(departureDate)) return null;

        LocalDate firstBreakfast = arrivalDate.plusDays(1);
        LocalDate lastBreakfast = departureDate;

        // If departing in the morning, last breakfast is day before departure
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING) {
            lastBreakfast = departureDate.minusDays(1);
        }

        if (firstBreakfast.isAfter(lastBreakfast)) return null;
        return new LocalDate[] { firstBreakfast, lastBreakfast };
    }

    /**
     * Calculates the number of lunches based on dates and times.
     * Arrival day: Lunch if arriving Morning (before lunch)
     * Middle days: All lunches
     * Departure day: Lunch if departing Afternoon or Evening (after lunch)
     */
    protected int calculateLunchCount() {
        if (arrivalDate == null || departureDate == null) return 0;

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(arrivalDate, departureDate) + 1;
        int count = (int) totalDays;

        // Arrival day: No lunch if arriving Afternoon or Evening (after lunch time)
        if (arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON ||
            arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING) {
            count--;
        }

        // Departure day: No lunch if departing Morning (before lunch)
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING) {
            count--;
        }

        return Math.max(0, count);
    }

    /**
     * Returns the date range for lunches [firstDate, lastDate].
     */
    protected LocalDate[] getLunchDateRange() {
        if (arrivalDate == null || departureDate == null) return null;

        LocalDate firstLunch = arrivalDate;
        LocalDate lastLunch = departureDate;

        // Arrival day: Skip if arriving Afternoon or Evening
        if (arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON ||
            arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING) {
            firstLunch = arrivalDate.plusDays(1);
        }

        // Departure day: Skip if departing Morning
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING) {
            lastLunch = departureDate.minusDays(1);
        }

        if (firstLunch.isAfter(lastLunch)) return null;
        return new LocalDate[] { firstLunch, lastLunch };
    }

    /**
     * Calculates the number of dinners based on dates and times.
     * Arrival day: Dinner if arriving Morning or Afternoon (before dinner)
     * Middle days: All dinners
     * Departure day: Dinner only if departing Evening (after dinner)
     */
    protected int calculateDinnerCount() {
        if (arrivalDate == null || departureDate == null) return 0;

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(arrivalDate, departureDate) + 1;
        int count = (int) totalDays;

        // Arrival day: No dinner if arriving Evening (after dinner time)
        if (arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING) {
            count--;
        }

        // Departure day: No dinner if departing Morning or Afternoon (before dinner)
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING ||
            departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON) {
            count--;
        }

        return Math.max(0, count);
    }

    /**
     * Returns the date range for dinners [firstDate, lastDate].
     */
    protected LocalDate[] getDinnerDateRange() {
        if (arrivalDate == null || departureDate == null) return null;

        LocalDate firstDinner = arrivalDate;
        LocalDate lastDinner = departureDate;

        // Arrival day: Skip if arriving Evening
        if (arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING) {
            firstDinner = arrivalDate.plusDays(1);
        }

        // Departure day: Skip if departing Morning or Afternoon
        if (departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING ||
            departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON) {
            lastDinner = departureDate.minusDays(1);
        }

        if (firstDinner.isAfter(lastDinner)) return null;
        return new LocalDate[] { firstDinner, lastDinner };
    }

    /**
     * Sets the arrival date and updates the summary.
     */
    public void setArrivalDate(LocalDate date) {
        this.arrivalDate = date;
        updateSummary();
    }

    /**
     * Sets the departure date and updates the summary.
     */
    public void setDepartureDate(LocalDate date) {
        this.departureDate = date;
        updateSummary();
    }

    /**
     * Sets the arrival time and updates the summary.
     */
    public void setArrivalTime(HasFestivalDaySelectionSection.ArrivalDepartureTime time) {
        this.arrivalTime = time != null ? time : HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        updateSummary();
    }

    /**
     * Sets the departure time and updates the summary.
     */
    public void setDepartureTime(HasFestivalDaySelectionSection.ArrivalDepartureTime time) {
        this.departureTime = time != null ? time : HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        updateSummary();
    }

    protected HBox createMealToggle(Object titleKey, Object subtitleKey, BooleanProperty selectedProperty, int pricePerDay) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-checkbox-card");

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Initial selection state - CSS handles styling via .selected class
        if (selectedProperty.get()) {
            card.getStyleClass().add("selected");
        }

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(selectedProperty, colorScheme);

        // SVG Icon (sun for lunch, moon for dinner) - flat gray non-colored style
        Node iconNode;
        boolean isLunch = titleKey == BookingPageI18nKeys.Lunch;
        if (isLunch) {
            // Sun icon for lunch (composite with rays and circle) - flat gray
            iconNode = BookingPageUIBuilder.createSunIcon(Color.web("#64748b"), 0.85);
        } else {
            // Moon icon for dinner - flat gray
            SVGPath moonIcon = new SVGPath();
            moonIcon.setContent(BookingPageUIBuilder.ICON_MOON);
            moonIcon.setStroke(Color.web("#64748b"));
            moonIcon.setStrokeWidth(2);
            moonIcon.setFill(Color.TRANSPARENT);
            moonIcon.setScaleX(0.85);
            moonIcon.setScaleY(0.85);
            iconNode = moonIcon;
        }

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(titleKey);
        title.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label subtitle = I18nControls.newLabel(subtitleKey);
        subtitle.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, subtitle);

        // Price
        Label price = new Label(formatPrice(pricePerDay) + "/day");
        price.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        card.getChildren().addAll(checkbox, iconNode, textContent, price);

        // Selection handling - CSS handles visual styling via .selected class
        selectedProperty.addListener((obs, old, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
        });

        card.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        return card;
    }

    /**
     * Creates the breakfast toggle card.
     * Breakfast is automatically included with accommodation and shows as "Included" rather than a price.
     */
    protected HBox createBreakfastToggle() {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("bookingpage-checkbox-card");

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Always selected style when accommodation is selected (breakfast is auto-included)
        // CSS handles styling via .selected class
        if (wantsBreakfast.get()) {
            card.getStyleClass().add("selected");
        }

        // Checkbox indicator (always checked when has accommodation)
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(wantsBreakfast, colorScheme);

        // Coffee cup icon for breakfast (using simple path) - flat gray non-colored style
        SVGPath coffeeIcon = new SVGPath();
        // Coffee cup SVG path
        coffeeIcon.setContent("M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3");
        coffeeIcon.setStroke(Color.web("#64748b"));  // Flat gray color
        coffeeIcon.setStrokeWidth(2);
        coffeeIcon.setFill(Color.TRANSPARENT);
        coffeeIcon.setScaleX(0.85);
        coffeeIcon.setScaleY(0.85);

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(BookingPageI18nKeys.Breakfast);
        title.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label subtitle = I18nControls.newLabel(BookingPageI18nKeys.IncludedWithAccommodation);
        subtitle.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, subtitle);

        // Price/status label
        Label priceLabel = I18nControls.newLabel(BookingPageI18nKeys.Included);
        priceLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        card.getChildren().addAll(checkbox, coffeeIcon, textContent, priceLabel);

        // Breakfast is not clickable - it's automatic with accommodation
        // Card styling updates handled by setHasAccommodation()

        return card;
    }

    /**
     * Updates breakfast visibility and selection based on accommodation state.
     */
    protected void updateBreakfastVisibility() {
        if (breakfastCard != null) {
            breakfastCard.setVisible(hasAccommodation);
            breakfastCard.setManaged(hasAccommodation);
        }
        // Breakfast is automatically selected when has accommodation, unselected when not
        wantsBreakfast.set(hasAccommodation);
    }

    protected HBox buildDietaryPreferenceSection() {
        HBox section = new HBox(16);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(16, 0, 0, 0));

        Label label = I18nControls.newLabel(BookingPageI18nKeys.DietaryPreference);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        section.getChildren().add(label);

        // If dietary options from API are loaded, use them; otherwise fallback to hardcoded
        if (!dietaryOptions.isEmpty()) {
            for (Item dietItem : dietaryOptions) {
                HBox option = createDietaryOptionFromItem(dietItem);
                section.getChildren().add(option);
            }
        } else {
            // Fallback to hardcoded options
            HBox vegetarianOption = createDietaryOption(BookingPageI18nKeys.Vegetarian, DietaryPreference.VEGETARIAN);
            HBox veganOption = createDietaryOption(BookingPageI18nKeys.Vegan, DietaryPreference.VEGAN);
            section.getChildren().addAll(vegetarianOption, veganOption);
        }

        return section;
    }

    /**
     * Creates a dietary option button from an Item loaded from API.
     */
    protected HBox createDietaryOptionFromItem(Item dietItem) {
        HBox option = new HBox(8);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(8, 16, 8, 16));
        option.setCursor(Cursor.HAND);
        option.getStyleClass().add("bookingpage-pill-option");

        // Use the item name for the label
        String itemName = dietItem.getName() != null ? dietItem.getName() : "Unknown";
        Label label = new Label(itemName);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");

        option.getChildren().add(label);

        // Initial selection state
        updateDietaryOptionStyle(option, dietItem.equals(selectedDietaryItem.get()));

        // Listen for selection changes
        selectedDietaryItem.addListener((obs, old, newVal) -> {
            updateDietaryOptionStyle(option, dietItem.equals(newVal));
        });

        option.setOnMouseClicked(e -> selectedDietaryItem.set(dietItem));

        return option;
    }

    /**
     * Updates the style of a dietary option based on selection state.
     * CSS handles visual styling via .selected class.
     */
    protected void updateDietaryOptionStyle(HBox option, boolean selected) {
        if (selected) {
            if (!option.getStyleClass().contains("selected")) {
                option.getStyleClass().add("selected");
            }
        } else {
            option.getStyleClass().remove("selected");
        }
    }

    @Deprecated
    protected HBox createDietaryOption(Object labelKey, DietaryPreference preference) {
        HBox option = new HBox(8);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(8, 16, 8, 16));
        option.setCursor(Cursor.HAND);
        option.getStyleClass().add("bookingpage-pill-option");

        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");

        option.getChildren().add(label);

        // Initial style
        updateDietaryOptionStyleLegacy(option, dietaryPreference.get() == preference);

        // Selection handling
        dietaryPreference.addListener((obs, old, newVal) -> {
            updateDietaryOptionStyleLegacy(option, newVal == preference);
        });

        option.setOnMouseClicked(e -> dietaryPreference.set(preference));

        return option;
    }

    @Deprecated
    private void updateDietaryOptionStyleLegacy(HBox option, boolean selected) {
        // CSS handles visual styling via .selected class
        if (selected) {
            if (!option.getStyleClass().contains("selected")) {
                option.getStyleClass().add("selected");
            }
        } else {
            option.getStyleClass().remove("selected");
        }
    }

    protected void setupBindings() {
        // Show/hide dietary section based on meal selection and update summary
        wantsBreakfast.addListener((obs, old, newVal) -> {
            updateDietarySectionVisibility();
            updateSummary();
        });
        wantsLunch.addListener((obs, old, newVal) -> {
            updateDietarySectionVisibility();
            updateSummary();
        });
        wantsDinner.addListener((obs, old, newVal) -> {
            updateDietarySectionVisibility();
            updateSummary();
        });
    }

    protected void updateDietarySectionVisibility() {
        boolean showDietary = wantsBreakfast.get() || wantsLunch.get() || wantsDinner.get();
        dietarySection.setVisible(showDietary);
        dietarySection.setManaged(showDietary);
    }

    protected String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Meals;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    // ========================================
    // HasMealsSelectionSection INTERFACE
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
    public void setBreakfastPricePerDay(int price) {
        this.breakfastPricePerDay = price;
    }

    @Override
    public void setLunchPricePerDay(int price) {
        this.lunchPricePerDay = price;
        // Rebuild lunch card to update price display
    }

    @Override
    public void setDinnerPricePerDay(int price) {
        this.dinnerPricePerDay = price;
        // Rebuild dinner card to update price display
    }

    @Override
    public void setDaysCount(int count) {
        this.daysCount = count;
    }

    @Override
    public void setInfoText(Object i18nKey) {
        I18nControls.bindI18nProperties(infoLabel, i18nKey);
    }

    @Override
    public void setHasAccommodation(boolean hasAccommodation) {
        this.hasAccommodation = hasAccommodation;
        updateBreakfastVisibility();
        updateSummary();
    }

    @Override
    public boolean hasAccommodation() {
        return hasAccommodation;
    }

    @Override
    public void setHasExtendedStay(boolean hasExtendedStay) {
        this.hasExtendedStay = hasExtendedStay;
        // Show/hide the extended stay label within the combined info box
        if (extendedStayLabel != null) {
            extendedStayLabel.setVisible(hasExtendedStay);
            extendedStayLabel.setManaged(hasExtendedStay);
        }
    }

    @Override
    public boolean hasExtendedStay() {
        return hasExtendedStay;
    }

    @Override
    public BooleanProperty wantsBreakfastProperty() {
        return wantsBreakfast;
    }

    @Override
    public BooleanProperty wantsLunchProperty() {
        return wantsLunch;
    }

    @Override
    public BooleanProperty wantsDinnerProperty() {
        return wantsDinner;
    }

    @Override
    public ObjectProperty<DietaryPreference> dietaryPreferenceProperty() {
        return dietaryPreference;
    }

    @Override
    public ObjectProperty<Item> selectedDietaryItemProperty() {
        return selectedDietaryItem;
    }

    @Override
    public int getTotalMealsCost() {
        int total = 0;
        if (wantsBreakfast.get()) {
            total += breakfastPricePerDay * daysCount;
        }
        if (wantsLunch.get()) {
            total += lunchPricePerDay * daysCount;
        }
        if (wantsDinner.get()) {
            total += dinnerPricePerDay * daysCount;
        }
        return total;
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    /**
     * Populates meal prices from PolicyAggregate data.
     * Looks for MEALS family items and extracts their daily rates.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultMealsSelectionSection: PolicyAggregate is null, using default prices");
            return;
        }

        // Get all MEALS scheduled items
        List<ScheduledItem> mealsItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
        Console.log("DefaultMealsSelectionSection: Found " + mealsItems.size() + " meals scheduled items");

        // Group by Item to get unique meal types
        Map<Item, List<ScheduledItem>> itemMap = mealsItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            String itemName = item.getName() != null ? item.getName().toLowerCase() : "";

            // Get rate for this meal item
            // Try daily rates first, then fallback to searching all rates (including fixed rates)
            int dailyRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .map(Rate::getPrice)
                .orElseGet(() -> {
                    // Fallback 1: search all daily rates for this item regardless of site
                    Integer rate = policyAggregate.getDailyRatesStream()
                        .filter(r -> r.getItem() != null && r.getItem().getPrimaryKey() != null
                            && r.getItem().getPrimaryKey().equals(item.getPrimaryKey()))
                        .findFirst()
                        .map(Rate::getPrice)
                        .orElse(null);
                    if (rate != null) return rate;

                    // Fallback 2: search ALL rates (including fixed rates) for this item
                    return policyAggregate.getRatesStream()
                        .filter(r -> r.getItem() != null && r.getItem().getPrimaryKey() != null
                            && r.getItem().getPrimaryKey().equals(item.getPrimaryKey()))
                        .findFirst()
                        .map(Rate::getPrice)
                        .orElse(0);
                });

            Console.log("DefaultMealsSelectionSection: Meal item '" + item.getName() + "' rate=" + dailyRate);

            // Determine if this is breakfast, lunch, or dinner based on item name
            if (itemName.contains("breakfast") || itemName.contains("morning")) {
                setBreakfastPricePerDay(dailyRate);
                Console.log("DefaultMealsSelectionSection: Set breakfast price to " + dailyRate);
            } else if (itemName.contains("lunch") || itemName.contains("midday")) {
                setLunchPricePerDay(dailyRate);
                Console.log("DefaultMealsSelectionSection: Set lunch price to " + dailyRate);
            } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                setDinnerPricePerDay(dailyRate);
                Console.log("DefaultMealsSelectionSection: Set dinner price to " + dailyRate);
            }
        }

        // Load dietary options from DIET item family
        loadDietaryOptions(policyAggregate);

        // Rebuild UI to reflect new prices and dietary options
        rebuildMealCards();
        rebuildDietarySection();
    }

    /**
     * Loads dietary options from the DIET item family in PolicyAggregate.
     * Auto-selects the first option.
     */
    protected void loadDietaryOptions(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) return;

        // Get all DIET scheduled items
        List<ScheduledItem> dietItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.DIET);
        Console.log("DefaultMealsSelectionSection: Found " + dietItems.size() + " diet scheduled items");

        // Extract unique Items
        dietaryOptions.clear();
        dietItems.stream()
            .map(ScheduledItem::getItem)
            .filter(item -> item != null)
            .distinct()
            .forEach(dietaryOptions::add);

        Console.log("DefaultMealsSelectionSection: Loaded " + dietaryOptions.size() + " unique dietary options");
        for (Item item : dietaryOptions) {
            Console.log("DefaultMealsSelectionSection: Dietary option: " + item.getName());
        }

        // Auto-select the first dietary option
        if (!dietaryOptions.isEmpty() && selectedDietaryItem.get() == null) {
            selectedDietaryItem.set(dietaryOptions.get(0));
            Console.log("DefaultMealsSelectionSection: Auto-selected dietary option: " + dietaryOptions.get(0).getName());
        }
    }

    /**
     * Rebuilds the dietary preference section with loaded options.
     */
    protected void rebuildDietarySection() {
        if (container == null || dietarySection == null) return;

        int index = container.getChildren().indexOf(dietarySection);
        if (index >= 0) {
            container.getChildren().remove(dietarySection);
            dietarySection = buildDietaryPreferenceSection();
            dietarySection.setVisible(wantsBreakfast.get() || wantsLunch.get() || wantsDinner.get());
            dietarySection.setManaged(dietarySection.isVisible());
            container.getChildren().add(index, dietarySection);
        }
    }

    /**
     * Rebuilds the meal toggle cards with current prices.
     */
    protected void rebuildMealCards() {
        // Use the mealsContainer field directly
        if (mealsContainer == null) return;

        mealsContainer.getChildren().clear();

        // Recreate breakfast, lunch and dinner cards with updated prices
        breakfastCard = createBreakfastToggle();
        lunchCard = createMealToggle(BookingPageI18nKeys.Lunch, BookingPageI18nKeys.MiddayMeal, wantsLunch, lunchPricePerDay);
        dinnerCard = createMealToggle(BookingPageI18nKeys.Dinner, BookingPageI18nKeys.EveningMeal, wantsDinner, dinnerPricePerDay);

        mealsContainer.getChildren().addAll(breakfastCard, lunchCard, dinnerCard);

        // Update breakfast visibility based on accommodation state
        updateBreakfastVisibility();
    }

}
