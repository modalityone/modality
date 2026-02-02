package one.modality.booking.frontoffice.bookingpage.sections.meals;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasFestivalDaySelectionSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.AttendanceBill;
import one.modality.ecommerce.shared.pricecalculator.DocumentBill;
import one.modality.ecommerce.shared.pricecalculator.SiteItemBill;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

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
    // All prices come from the database via populateFromPolicyAggregate()
    protected int breakfastPricePerDay = 0;
    protected int lunchPricePerDay = 0;
    protected int dinnerPricePerDay = 0;
    protected int daysCount = 1;

    // === EARLY ARRIVAL / LATE DEPARTURE PRICING ===
    // These are shown stacked under the main price when set (> 0) and different from main price
    protected int breakfastEarlyArrivalPrice = 0;
    protected int lunchEarlyArrivalPrice = 0;
    protected int dinnerEarlyArrivalPrice = 0;
    protected int breakfastLateDeparturePrice = 0;
    protected int lunchLateDeparturePrice = 0;
    protected int dinnerLateDeparturePrice = 0;
    protected boolean showEarlyArrivalPricing = false;
    protected boolean showLateDeparturePricing = false;

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

    // === EVENT BOUNDARY DATES ===
    // These define when the event officially starts/ends - meals outside these dates are early arrival/late departure
    protected LocalDate eventBoundaryStartDate;
    protected LocalDate eventBoundaryEndDate;

    // === EVENT BOUNDARY MEALS ===
    // These define which meal marks the start/end of the main event on the boundary dates
    // For example, if main event starts at DINNER on Apr 24, then lunch on Apr 24 is early arrival
    public enum MealBoundary { BREAKFAST, LUNCH, DINNER }
    protected MealBoundary eventBoundaryStartMeal = MealBoundary.BREAKFAST; // Default: main event starts at breakfast
    protected MealBoundary eventBoundaryEndMeal = MealBoundary.DINNER;      // Default: main event ends at dinner

    // === MAIN EVENT PERIOD ===
    // The main event period (from EventPart) used for isInPeriod checks
    protected Period mainEventPeriod;

    // === MEAL SCHEDULED ITEMS ===
    // All meal ScheduledItems from PolicyAggregate, used for isInPeriod checks
    protected List<ScheduledItem> mealScheduledItems = new ArrayList<>();

    // === SUMMARY UI ===
    protected VBox summaryBox;  // Summary info box showing meal counts

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected WorkingBooking workingBooking;  // For DocumentBill-based pricing

    // === DOCUMENT BILL PRICING ===
    // Cached meal prices from DocumentBill for use in summary display
    protected Map<String, Integer> mealBillPrices = new java.util.HashMap<>();  // "lunch" -> total price, etc.
    protected Map<String, List<AttendanceBill>> mealAttendanceBills = new java.util.HashMap<>();  // Per-day breakdown

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
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.getStyleClass().addAll(bookingpage_info_box, bookingpage_info_box_neutral);

        // Main info text - vegetarian info
        infoLabel = I18nControls.newLabel(BookingPageI18nKeys.AllMealsVegetarian);
        infoLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        infoLabel.setWrapText(true);

        // Extended stay label (hidden by default) - shows pricing info
        extendedStayLabel = new Label();
        extendedStayLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_primary);
        extendedStayLabel.setWrapText(true);
        extendedStayLabel.setVisible(false);
        extendedStayLabel.setManaged(false);

        box.getChildren().addAll(infoLabel, extendedStayLabel);
        return box;
    }

    /**
     * Updates the info box with meal pricing information based on selected dates.
     * Shows prices during event and outside event (early arrival/late departure).
     */
    protected void updateInfoBoxPricing() {
        if (extendedStayLabel == null) return;

        // Check if we have extended stay (early arrival or late departure)
        boolean hasExtendedStay = showEarlyArrivalPricing || showLateDeparturePricing;

        if (!hasExtendedStay) {
            extendedStayLabel.setVisible(false);
            extendedStayLabel.setManaged(false);
            return;
        }

        // Get the main event price (use lunch as representative, or dinner if lunch is 0)
        int mainEventPrice = lunchPricePerDay > 0 ? lunchPricePerDay : dinnerPricePerDay;
        // Get the outside event price (early/late - use the higher of early arrival or late departure)
        int outsideEventPrice = Math.max(
            Math.max(lunchEarlyArrivalPrice, dinnerEarlyArrivalPrice),
            Math.max(lunchLateDeparturePrice, dinnerLateDeparturePrice)
        );

        // Build pricing text
        StringBuilder sb = new StringBuilder();
        if (mainEventPrice > 0 && outsideEventPrice > 0 && mainEventPrice != outsideEventPrice) {
            sb.append("During event: ").append(formatPrice(mainEventPrice)).append("/meal");
            sb.append(" • Outside event: ").append(formatPrice(outsideEventPrice)).append("/meal");
        } else if (outsideEventPrice > 0) {
            sb.append("Outside event dates: ").append(formatPrice(outsideEventPrice)).append("/meal");
        }

        if (sb.length() > 0) {
            extendedStayLabel.setText(sb.toString());
            extendedStayLabel.setVisible(true);
            extendedStayLabel.setManaged(true);
        } else {
            extendedStayLabel.setVisible(false);
            extendedStayLabel.setManaged(false);
        }
    }

    /**
     * Creates the summary box that shows meal counts based on dates and selections.
     */
    protected VBox createSummaryBox() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12, 16, 12, 16));
        // Neutral style matching other info boxes - using CSS class
        box.getStyleClass().addAll(bookingpage_info_box, bookingpage_info_box_neutral);
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
        titleLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);
        summaryBox.getChildren().add(titleLabel);

        // Calculate and display meal counts
        DateTimeFormatter dayMonthFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

        if (hasBreakfast) {
            int count = calculateBreakfastCount();
            if (count > 0) {
                String text = formatMealSummaryLine("Breakfast", count, getBreakfastDateRange(), dayMonthFormat);
                Label label = new Label(text);
                label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
                summaryBox.getChildren().add(label);
            }
        }

        if (hasLunch) {
            // Prefer DocumentBill pricing when available
            if (mealAttendanceBills.containsKey("lunch") && !mealAttendanceBills.get("lunch").isEmpty()) {
                addMealSummaryFromDocumentBill("Lunch", "lunch", dayMonthFormat, summaryBox);
            } else {
                addMealSummaryWithBreakdown("Lunch", calculateLunchCountBreakdown(),
                    lunchPricePerDay, lunchEarlyArrivalPrice, lunchLateDeparturePrice,
                    dayMonthFormat, summaryBox);
            }
        }

        if (hasDinner) {
            // Prefer DocumentBill pricing when available
            if (mealAttendanceBills.containsKey("dinner") && !mealAttendanceBills.get("dinner").isEmpty()) {
                addMealSummaryFromDocumentBill("Dinner", "dinner", dayMonthFormat, summaryBox);
            } else {
                addMealSummaryWithBreakdown("Dinner", calculateDinnerCountBreakdown(),
                    dinnerPricePerDay, dinnerEarlyArrivalPrice, dinnerLateDeparturePrice,
                    dayMonthFormat, summaryBox);
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
     * Adds meal summary lines with price breakdown to the summary box.
     * Shows separate lines for early arrival, regular, and late departure meals when prices differ.
     * Format: "3 Lunches: Mon 21 Apr - Wed 23 Apr - $30"
     */
    protected void addMealSummaryWithBreakdown(String mealName, MealCountBreakdown breakdown,
                                                int regularPrice, int earlyPrice, int latePrice,
                                                DateTimeFormatter formatter, VBox summaryBox) {
        if (breakdown.totalCount() == 0) return;

        // Check if we need to show breakdown (different prices exist)
        boolean hasEarlyWithDifferentPrice = breakdown.earlyCount > 0 && earlyPrice > 0 && earlyPrice != regularPrice;
        boolean hasLateWithDifferentPrice = breakdown.lateCount > 0 && latePrice > 0 && latePrice != regularPrice;
        boolean needsBreakdown = hasEarlyWithDifferentPrice || hasLateWithDifferentPrice;

        if (!needsBreakdown) {
            // Simple case: all meals have same price - show single line with total
            int totalCount = breakdown.totalCount();
            LocalDate[] dateRange = getOverallDateRange(breakdown);
            int totalPrice = totalCount * regularPrice;
            String text = formatMealSummaryLineWithPrice(mealName, totalCount, dateRange, regularPrice, totalPrice, formatter);
            Label label = new Label(text);
            label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
            summaryBox.getChildren().add(label);
        } else {
            // Complex case: show breakdown by price period
            // Early arrival meals
            if (breakdown.earlyCount > 0) {
                int price = earlyPrice > 0 ? earlyPrice : regularPrice;
                int totalPrice = breakdown.earlyCount * price;
                LocalDate[] range = new LocalDate[] { breakdown.earlyStart, breakdown.earlyEnd };
                String text = formatMealSummaryLineWithPrice(mealName + " (Early)", breakdown.earlyCount, range, price, totalPrice, formatter);
                Label label = new Label(text);
                label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
                summaryBox.getChildren().add(label);
            }

            // Regular event meals
            if (breakdown.regularCount > 0) {
                int totalPrice = breakdown.regularCount * regularPrice;
                LocalDate[] range = new LocalDate[] { breakdown.regularStart, breakdown.regularEnd };
                String text = formatMealSummaryLineWithPrice(mealName, breakdown.regularCount, range, regularPrice, totalPrice, formatter);
                Label label = new Label(text);
                label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
                summaryBox.getChildren().add(label);
            }

            // Late departure meals
            if (breakdown.lateCount > 0) {
                int price = latePrice > 0 ? latePrice : regularPrice;
                int totalPrice = breakdown.lateCount * price;
                LocalDate[] range = new LocalDate[] { breakdown.lateStart, breakdown.lateEnd };
                String text = formatMealSummaryLineWithPrice(mealName + " (Late)", breakdown.lateCount, range, price, totalPrice, formatter);
                Label label = new Label(text);
                label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
                summaryBox.getChildren().add(label);
            }
        }
    }

    /**
     * Adds meal summary using actual DocumentBill data.
     * This provides accurate pricing based on the booking's price calculator,
     * including any discounts, rate variations by date, etc.
     *
     * <p>Format examples:</p>
     * <ul>
     *   <li>"3 Lunches: Mon 21 Apr - Wed 23 Apr = $30"</li>
     *   <li>"1 Dinner: Fri 25 Apr = $12"</li>
     * </ul>
     *
     * @param mealName the display name (e.g., "Lunch", "Dinner")
     * @param mealType the key for cached data (e.g., "lunch", "dinner")
     * @param formatter date formatter for display
     * @param summaryBox the container to add labels to
     */
    protected void addMealSummaryFromDocumentBill(String mealName, String mealType,
                                                    DateTimeFormatter formatter, VBox summaryBox) {
        List<AttendanceBill> attendanceBills = mealAttendanceBills.get(mealType);
        if (attendanceBills == null || attendanceBills.isEmpty()) return;

        int count = attendanceBills.size();
        int totalPrice = mealBillPrices.getOrDefault(mealType, 0);

        // Get date range from attendance bills
        LocalDate firstDate = null;
        LocalDate lastDate = null;
        for (AttendanceBill ab : attendanceBills) {
            LocalDate date = ab.getDate();
            if (date == null) continue;
            if (firstDate == null || date.isBefore(firstDate)) firstDate = date;
            if (lastDate == null || date.isAfter(lastDate)) lastDate = date;
        }

        // Format the summary line
        StringBuilder sb = new StringBuilder();
        String plural = count == 1 ? "" : "s";
        sb.append(count).append(" ").append(mealName).append(plural);

        if (firstDate != null) {
            sb.append(": ");
            String fromDate = firstDate.format(formatter);
            if (lastDate == null || firstDate.equals(lastDate)) {
                sb.append(fromDate);
            } else {
                sb.append(fromDate).append(" - ").append(lastDate.format(formatter));
            }
        }

        // Add total price from DocumentBill
        sb.append(" = ").append(formatPrice(totalPrice));

        Label label = new Label(sb.toString());
        label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        summaryBox.getChildren().add(label);
    }

    /**
     * Gets the overall date range from a breakdown (earliest to latest date).
     */
    protected LocalDate[] getOverallDateRange(MealCountBreakdown breakdown) {
        LocalDate earliest = null;
        LocalDate latest = null;

        if (breakdown.earlyStart != null) {
            earliest = breakdown.earlyStart;
            latest = breakdown.earlyEnd;
        }
        if (breakdown.regularStart != null) {
            if (earliest == null || breakdown.regularStart.isBefore(earliest)) {
                earliest = breakdown.regularStart;
            }
            if (latest == null || breakdown.regularEnd.isAfter(latest)) {
                latest = breakdown.regularEnd;
            }
        }
        if (breakdown.lateStart != null) {
            if (earliest == null || breakdown.lateStart.isBefore(earliest)) {
                earliest = breakdown.lateStart;
            }
            if (latest == null || breakdown.lateEnd.isAfter(latest)) {
                latest = breakdown.lateEnd;
            }
        }

        return new LocalDate[] { earliest, latest };
    }

    /**
     * Formats a meal summary line with price.
     * Format: "3 Lunches: Mon 21 Apr - Wed 23 Apr @ $10/meal = $30"
     */
    protected String formatMealSummaryLineWithPrice(String mealName, int count, LocalDate[] dateRange,
                                                     int pricePerMeal, int totalPrice, DateTimeFormatter formatter) {
        String plural = count == 1 ? "" : "s";
        StringBuilder sb = new StringBuilder();
        sb.append(count).append(" ").append(mealName).append(plural);

        if (dateRange != null && dateRange[0] != null) {
            sb.append(": ");
            String fromDate = dateRange[0].format(formatter);
            if (dateRange[1] == null || dateRange[0].equals(dateRange[1])) {
                sb.append(fromDate);
            } else {
                sb.append(fromDate).append(" - ").append(dateRange[1].format(formatter));
            }
        }

        // Add price per meal and total
        sb.append(" @ ").append(formatPrice(pricePerMeal)).append("/meal");
        if (count > 1) {
            sb.append(" = ").append(formatPrice(totalPrice));
        }

        return sb.toString();
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

    // === MEAL COUNT BREAKDOWN BY PRICING PERIOD ===

    /**
     * Represents a breakdown of meals by pricing period (early arrival, regular, late departure).
     */
    protected static class MealCountBreakdown {
        final int earlyCount;
        final LocalDate earlyStart;
        final LocalDate earlyEnd;
        final int regularCount;
        final LocalDate regularStart;
        final LocalDate regularEnd;
        final int lateCount;
        final LocalDate lateStart;
        final LocalDate lateEnd;

        MealCountBreakdown(int earlyCount, LocalDate earlyStart, LocalDate earlyEnd,
                          int regularCount, LocalDate regularStart, LocalDate regularEnd,
                          int lateCount, LocalDate lateStart, LocalDate lateEnd) {
            this.earlyCount = earlyCount;
            this.earlyStart = earlyStart;
            this.earlyEnd = earlyEnd;
            this.regularCount = regularCount;
            this.regularStart = regularStart;
            this.regularEnd = regularEnd;
            this.lateCount = lateCount;
            this.lateStart = lateStart;
            this.lateEnd = lateEnd;
        }

        int totalCount() {
            return earlyCount + regularCount + lateCount;
        }

        boolean hasDifferentPrices() {
            return (earlyCount > 0 || lateCount > 0) && regularCount > 0;
        }
    }

    /**
     * Calculates the lunch count breakdown by early arrival, regular, and late departure periods.
     */
    protected MealCountBreakdown calculateLunchCountBreakdown() {
        LocalDate[] dateRange = getLunchDateRange();
        if (dateRange == null || dateRange[0] == null) {
            return new MealCountBreakdown(0, null, null, 0, null, null, 0, null, null);
        }

        LocalDate firstLunch = dateRange[0];
        LocalDate lastLunch = dateRange[1] != null ? dateRange[1] : firstLunch;

        return calculateMealBreakdown(firstLunch, lastLunch);
    }

    /**
     * Calculates the dinner count breakdown by early arrival, regular, and late departure periods.
     */
    protected MealCountBreakdown calculateDinnerCountBreakdown() {
        LocalDate[] dateRange = getDinnerDateRange();
        if (dateRange == null || dateRange[0] == null) {
            return new MealCountBreakdown(0, null, null, 0, null, null, 0, null, null);
        }

        LocalDate firstDinner = dateRange[0];
        LocalDate lastDinner = dateRange[1] != null ? dateRange[1] : firstDinner;

        return calculateMealBreakdown(firstDinner, lastDinner);
    }

    /**
     * Generic method to calculate meal breakdown between two dates against event boundaries.
     */
    protected MealCountBreakdown calculateMealBreakdown(LocalDate firstMeal, LocalDate lastMeal) {
        // If no event boundaries set, all meals are regular
        if (eventBoundaryStartDate == null || eventBoundaryEndDate == null) {
            int count = (int) java.time.temporal.ChronoUnit.DAYS.between(firstMeal, lastMeal) + 1;
            return new MealCountBreakdown(0, null, null, count, firstMeal, lastMeal, 0, null, null);
        }

        int earlyCount = 0;
        LocalDate earlyStart = null;
        LocalDate earlyEnd = null;
        int regularCount = 0;
        LocalDate regularStart = null;
        LocalDate regularEnd = null;
        int lateCount = 0;
        LocalDate lateStart = null;
        LocalDate lateEnd = null;

        // Early arrival meals: before event boundary start
        if (firstMeal.isBefore(eventBoundaryStartDate)) {
            earlyStart = firstMeal;
            earlyEnd = lastMeal.isBefore(eventBoundaryStartDate) ? lastMeal : eventBoundaryStartDate.minusDays(1);
            earlyCount = (int) java.time.temporal.ChronoUnit.DAYS.between(earlyStart, earlyEnd) + 1;
        }

        // Regular meals: within event boundaries
        LocalDate regStart = firstMeal.isBefore(eventBoundaryStartDate) ? eventBoundaryStartDate : firstMeal;
        LocalDate regEnd = lastMeal.isAfter(eventBoundaryEndDate) ? eventBoundaryEndDate : lastMeal;
        if (!regStart.isAfter(regEnd) && !regStart.isAfter(eventBoundaryEndDate) && !regEnd.isBefore(eventBoundaryStartDate)) {
            regularStart = regStart;
            regularEnd = regEnd;
            regularCount = (int) java.time.temporal.ChronoUnit.DAYS.between(regularStart, regularEnd) + 1;
        }

        // Late departure meals: after event boundary end
        if (lastMeal.isAfter(eventBoundaryEndDate)) {
            lateStart = firstMeal.isAfter(eventBoundaryEndDate) ? firstMeal : eventBoundaryEndDate.plusDays(1);
            lateEnd = lastMeal;
            lateCount = (int) java.time.temporal.ChronoUnit.DAYS.between(lateStart, lateEnd) + 1;
        }

        return new MealCountBreakdown(earlyCount, earlyStart, earlyEnd,
                                       regularCount, regularStart, regularEnd,
                                       lateCount, lateStart, lateEnd);
    }

    /**
     * Sets the arrival date and rebuilds meal cards to update pricing.
     * Changing arrival date affects which meals are in the early arrival period,
     * triggering a full rebuild to recalculate pricing flags.
     */
    public void setArrivalDate(LocalDate date) {
        this.arrivalDate = date;
        rebuildMealCards();
    }

    /**
     * Sets the departure date and rebuilds meal cards to update pricing.
     * Changing departure date affects which meals are in the late departure period,
     * triggering a full rebuild to recalculate pricing flags.
     */
    public void setDepartureDate(LocalDate date) {
        this.departureDate = date;
        rebuildMealCards();
    }

    /**
     * Sets the arrival time and rebuilds meal cards to update pricing.
     * Changing arrival time (e.g., afternoon to morning) affects which meals are included,
     * and if that meal is outside the main event period, different pricing may apply.
     */
    public void setArrivalTime(HasFestivalDaySelectionSection.ArrivalDepartureTime time) {
        this.arrivalTime = time != null ? time : HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        rebuildMealCards();
    }

    /**
     * Sets the departure time and rebuilds meal cards to update pricing.
     * Changing departure time affects which meals are included,
     * and if that meal is outside the main event period, different pricing may apply.
     */
    public void setDepartureTime(HasFestivalDaySelectionSection.ArrivalDepartureTime time) {
        this.departureTime = time != null ? time : HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        rebuildMealCards();
    }

    /**
     * Sets the event boundary start date (when the event officially begins).
     * Meals before this date are considered "early arrival" with potentially different pricing.
     */
    public void setEventBoundaryStartDate(LocalDate date) {
        this.eventBoundaryStartDate = date;
        // Don't call updateSummary() here - it will be called after prices are set via rebuildMealCards()
    }

    /**
     * Sets the event boundary end date (when the event officially ends).
     * Meals after this date are considered "late departure" with potentially different pricing.
     */
    public void setEventBoundaryEndDate(LocalDate date) {
        this.eventBoundaryEndDate = date;
        // Don't call updateSummary() here - it will be called after prices are set via rebuildMealCards()
    }

    /**
     * Sets the meal type that marks the start of the main event.
     * Meals before this on the start date are considered "early arrival".
     * For example, if main event starts at DINNER, then lunch on start date is early arrival.
     */
    public void setEventBoundaryStartMeal(MealBoundary meal) {
        this.eventBoundaryStartMeal = meal != null ? meal : MealBoundary.BREAKFAST;
    }

    /**
     * Sets the meal type that marks the end of the main event.
     * Meals after this on the end date are considered "late departure".
     * For example, if main event ends at LUNCH, then dinner on end date is late departure.
     */
    public void setEventBoundaryEndMeal(MealBoundary meal) {
        this.eventBoundaryEndMeal = meal != null ? meal : MealBoundary.DINNER;
    }

    /**
     * Sets the main event period (typically from EventPart) for isInPeriod checks.
     * This allows accurate determination of whether a meal ScheduledItem falls within the main event.
     *
     * @param period the main event period (e.g., EventPart which implements Period via BoundaryPeriod)
     */
    public void setMainEventPeriod(Period period) {
        this.mainEventPeriod = period;
    }

    /**
     * Sets the meal ScheduledItems for isInPeriod checks.
     * These are used to determine which meals fall outside the main event period.
     *
     * @param scheduledItems list of meal ScheduledItems from PolicyAggregate
     */
    public void setMealScheduledItems(List<ScheduledItem> scheduledItems) {
        this.mealScheduledItems = scheduledItems != null ? new ArrayList<>(scheduledItems) : new ArrayList<>();
    }

    protected HBox createMealToggle(Object titleKey, Object subtitleKey, BooleanProperty selectedProperty, int pricePerDay) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add(bookingpage_checkbox_card);

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Initial selection state - CSS handles styling via .selected class
        if (selectedProperty.get()) {
            card.getStyleClass().add(selected);
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
        title.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);

        Label subtitle = I18nControls.newLabel(subtitleKey);
        subtitle.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        subtitle.setWrapText(true);

        textContent.getChildren().addAll(title, subtitle);

        // Price display - supports stacked pricing for early arrival/late departure
        VBox priceContainer = createMealPriceDisplay(pricePerDay, titleKey);
        priceContainer.setMinWidth(Region.USE_PREF_SIZE);  // Prevent compression on narrow screens

        card.getChildren().addAll(checkbox, iconNode, textContent, priceContainer);

        // Selection handling - CSS handles visual styling via .selected class
        selectedProperty.addListener((obs, old, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains(selected)) {
                    card.getStyleClass().add(selected);
                }
            } else {
                card.getStyleClass().remove(selected);
            }
        });

        card.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        return card;
    }

    /**
     * Creates a price display VBox that shows the main price per meal and optionally
     * early arrival/late departure prices stacked below in a compact layout.
     *
     * <p>UX Design:</p>
     * <ul>
     *   <li>Main event price shown prominently at top</li>
     *   <li>Secondary price shown smaller below only when user has selected early/late dates</li>
     *   <li>If early and late prices are the same, show once as "Outside event"</li>
     * </ul>
     *
     * @param pricePerDay the main price per meal (during event)
     * @param titleKey the meal type key (used to determine which early/late prices to show)
     * @return a VBox with the price display
     */
    protected VBox createMealPriceDisplay(int pricePerDay, Object titleKey) {
        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER_RIGHT);

        // Main price label - this is the event price, shown prominently
        Label mainPrice = new Label(formatPrice(pricePerDay) + "/meal");
        mainPrice.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);
        priceBox.getChildren().add(mainPrice);

        // Determine which early/late prices to show based on meal type
        int earlyPrice = 0;
        int latePrice = 0;
        boolean isLunch = titleKey == BookingPageI18nKeys.Lunch;
        boolean isDinner = titleKey == BookingPageI18nKeys.Dinner;
        boolean hasEarlyMeal = false;
        boolean hasLateMeal = false;

        if (isLunch) {
            earlyPrice = lunchEarlyArrivalPrice;
            latePrice = lunchLateDeparturePrice;
            hasEarlyMeal = hasEarlyArrivalLunch();
            hasLateMeal = hasLateDepartureLunch();
        } else if (isDinner) {
            earlyPrice = dinnerEarlyArrivalPrice;
            latePrice = dinnerLateDeparturePrice;
            hasEarlyMeal = hasEarlyArrivalDinner();
            hasLateMeal = hasLateDepartureDinner();
        }

        // Only show secondary prices when this specific meal type has early/late meals
        // based on both the selected dates AND times
        boolean showEarly = hasEarlyMeal && earlyPrice > 0 && earlyPrice != pricePerDay;
        boolean showLate = hasLateMeal && latePrice > 0 && latePrice != pricePerDay;

        if (showEarly || showLate) {
            // If early and late prices are the same, show single line "Outside event: $X/meal"
            if (showEarly && showLate && earlyPrice == latePrice) {
                Label outsideLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.OutsideEventMealPrice, formatPrice(earlyPrice)));
                outsideLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);
                priceBox.getChildren().add(outsideLabel);
            } else {
                // Show separate lines for early and late if different
                if (showEarly) {
                    Label earlyLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.EarlyArrivalMealPrice, formatPrice(earlyPrice)));
                    earlyLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);
                    priceBox.getChildren().add(earlyLabel);
                }
                if (showLate) {
                    Label lateLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.LateDepartureMealPrice, formatPrice(latePrice)));
                    lateLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);
                    priceBox.getChildren().add(lateLabel);
                }
            }
        }

        return priceBox;
    }

    /**
     * Creates the breakfast toggle card.
     * Breakfast is automatically included with accommodation and shows as "Included" rather than a price.
     */
    protected HBox createBreakfastToggle() {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.getStyleClass().add(bookingpage_checkbox_card);

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Always selected style when accommodation is selected (breakfast is auto-included)
        // CSS handles styling via .selected class
        if (wantsBreakfast.get()) {
            card.getStyleClass().add(selected);
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
        title.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);

        Label subtitle = I18nControls.newLabel(BookingPageI18nKeys.IncludedWithAccommodation);
        subtitle.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        subtitle.setWrapText(true);

        textContent.getChildren().addAll(title, subtitle);

        // Price/status label
        Label priceLabel = I18nControls.newLabel(BookingPageI18nKeys.Included);
        priceLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);
        priceLabel.setMinWidth(Region.USE_PREF_SIZE);  // Prevent compression on narrow screens

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
        label.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);

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
        option.getStyleClass().add(bookingpage_pill_option);

        // Use the item name for the label
        String itemName = dietItem.getName() != null ? dietItem.getName() : "Unknown";
        Label label = new Label(itemName);
        label.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_medium, bookingpage_text_dark);

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
            if (!option.getStyleClass().contains(BookingPageCssSelectors.selected)) {
                option.getStyleClass().add(BookingPageCssSelectors.selected);
            }
        } else {
            option.getStyleClass().remove(BookingPageCssSelectors.selected);
        }
    }

    @Deprecated
    protected HBox createDietaryOption(Object labelKey, DietaryPreference preference) {
        HBox option = new HBox(8);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(8, 16, 8, 16));
        option.setCursor(Cursor.HAND);
        option.getStyleClass().add(bookingpage_pill_option);

        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_medium, bookingpage_text_dark);

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
            if (!option.getStyleClass().contains(BookingPageCssSelectors.selected)) {
                option.getStyleClass().add(BookingPageCssSelectors.selected);
            }
        } else {
            option.getStyleClass().remove(BookingPageCssSelectors.selected);
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

    /**
     * Sets the WorkingBooking for DocumentBill-based pricing.
     * This enables accurate price calculation using the booking's price calculator.
     */
    public void setWorkingBooking(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
    }

    /**
     * Populates meal prices from the DocumentBill API.
     * This method extracts pricing for MEALS family items from the computed DocumentBill
     * and caches them for display in the summary.
     *
     * <p>The DocumentBill provides accurate per-day pricing that accounts for:</p>
     * <ul>
     *   <li>Rate variations by date (early arrival, late departure)</li>
     *   <li>Person-specific discounts (age, unemployed, facility fee)</li>
     *   <li>Long stay discounts</li>
     * </ul>
     */
    public void populateFromDocumentBill() {
        if (workingBooking == null) {
            return;
        }

        // Clear cached prices
        mealBillPrices.clear();
        mealAttendanceBills.clear();

        // Get the DocumentBill from the latest booking state
        DocumentBill documentBill = workingBooking.getLatestBookingPriceCalculator().computeDocumentBill();
        if (documentBill == null) {
            return;
        }

        // IMPORTANT: Trigger price computation by calling getTotalPrice() first
        // This populates the totalPrice field on each SiteItemBill (lazy computation)
        documentBill.getTotalPrice();

        // Process each SiteItemBill to find MEALS items
        for (SiteItemBill siteItemBill : documentBill.getSiteItemBills()) {
            SiteItem siteItem = siteItemBill.getSiteItem();
            if (siteItem == null || siteItem.getItem() == null) continue;

            Item item = siteItem.getItem();
            KnownItemFamily family = item.getItemFamilyType();

            // Only process MEALS family items
            if (family != KnownItemFamily.MEALS) continue;

            String itemName = item.getName() != null ? item.getName().toLowerCase() : "";
            String mealType = determineMealType(itemName);

            if (mealType != null) {
                // Store per-day breakdown
                List<AttendanceBill> attendanceBills = siteItemBill.getAttendanceBills();
                mealAttendanceBills.put(mealType, attendanceBills);

                // Calculate total price by summing AttendanceBill prices
                // Note: siteItemBill.getTotalPrice() may return -1 in some code paths,
                // so we sum the individual attendance prices instead
                int totalPrice = 0;
                for (AttendanceBill ab : attendanceBills) {
                    totalPrice += ab.getPrice();
                }
                mealBillPrices.put(mealType, totalPrice);
            }
        }

        // Update the summary to reflect DocumentBill prices
        updateSummary();
    }

    /**
     * Determines the meal type from the item name.
     * @return "breakfast", "lunch", "dinner", or null if not recognized
     */
    protected String determineMealType(String itemName) {
        if (itemName.contains("breakfast") || itemName.contains("morning")) {
            return "breakfast";
        } else if (itemName.contains("lunch") || itemName.contains("midday")) {
            return "lunch";
        } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
            return "dinner";
        }
        return null;
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

    // === EARLY ARRIVAL / LATE DEPARTURE PRICING SETTERS ===

    /**
     * Sets the lunch price for early arrival days (before event start).
     */
    public void setLunchEarlyArrivalPrice(int price) {
        this.lunchEarlyArrivalPrice = price;
    }

    /**
     * Sets the dinner price for early arrival days (before event start).
     */
    public void setDinnerEarlyArrivalPrice(int price) {
        this.dinnerEarlyArrivalPrice = price;
    }

    /**
     * Sets the lunch price for late departure days (after event end).
     */
    public void setLunchLateDeparturePrice(int price) {
        this.lunchLateDeparturePrice = price;
    }

    /**
     * Sets the dinner price for late departure days (after event end).
     */
    public void setDinnerLateDeparturePrice(int price) {
        this.dinnerLateDeparturePrice = price;
    }

    /**
     * Enables/disables display of early arrival pricing in meal cards.
     * When enabled, shows a secondary price line for early arrival days.
     */
    public void setShowEarlyArrivalPricing(boolean show) {
        this.showEarlyArrivalPricing = show;
    }

    /**
     * Returns whether early arrival pricing is currently displayed.
     */
    public boolean isShowEarlyArrivalPricing() {
        return showEarlyArrivalPricing;
    }

    /**
     * Enables/disables display of late departure pricing in meal cards.
     * When enabled, shows a secondary price line for late departure days.
     */
    public void setShowLateDeparturePricing(boolean show) {
        this.showLateDeparturePricing = show;
    }

    /**
     * Returns whether late departure pricing is currently displayed.
     */
    public boolean isShowLateDeparturePricing() {
        return showLateDeparturePricing;
    }

    /**
     * Checks if there's at least one early arrival lunch.
     * Handles three cases:
     * 1. More than 1 day before: intermediate days have lunches (regardless of arrival time)
     * 2. Exactly 1 day before: YES if can eat lunch on Day -1 OR Day 0 lunch is early arrival
     * 3. On boundary date: lunch is early arrival if arrival time allows it AND lunch is before event start meal
     */
    public boolean hasEarlyArrivalLunch() {
        if (arrivalDate == null || eventBoundaryStartDate == null) {
            return false;
        }
        // Case 1: More than 1 day before - intermediate days have lunches
        if (arrivalDate.isBefore(eventBoundaryStartDate.minusDays(1))) {
            return true;
        }
        // Case 2: Exactly 1 day before - check BOTH Day -1 lunch AND Day 0 lunch
        if (arrivalDate.equals(eventBoundaryStartDate.minusDays(1))) {
            // Can eat lunch on Day -1?
            boolean canEatDay1Lunch = isEarlyArrivalMeal(arrivalTime, MealBoundary.LUNCH);
            // Day 0 lunch is early arrival if event starts at DINNER (lunch is before event)
            boolean day0LunchIsEarly = isMealBeforeEventStart(MealBoundary.LUNCH, eventBoundaryStartMeal);
            return canEatDay1Lunch || day0LunchIsEarly;
        }
        // Case 3: On boundary date - lunch must be before event start meal
        if (arrivalDate.equals(eventBoundaryStartDate)) {
            return isEarlyArrivalMeal(arrivalTime, MealBoundary.LUNCH)
                && isMealBeforeEventStart(MealBoundary.LUNCH, eventBoundaryStartMeal);
        }
        return false;
    }

    /**
     * Checks if there's at least one early arrival dinner.
     * Handles three cases:
     * 1. More than 1 day before: intermediate days have dinners (regardless of arrival time)
     * 2. Exactly 1 day before: dinner is early arrival if arrival time allows it
     * 3. On boundary date: NO (dinner is never early arrival since it's the last meal of the day)
     */
    public boolean hasEarlyArrivalDinner() {
        if (arrivalDate == null || eventBoundaryStartDate == null) {
            return false;
        }
        // Case 1: More than 1 day before - intermediate days have dinners
        if (arrivalDate.isBefore(eventBoundaryStartDate.minusDays(1))) {
            return true;
        }
        // Case 2: Exactly 1 day before - dinner is early arrival if arrival time allows it
        if (arrivalDate.equals(eventBoundaryStartDate.minusDays(1))) {
            return isEarlyArrivalMeal(arrivalTime, MealBoundary.DINNER);
        }
        // Case 3: On boundary date - dinner is never early arrival (it's the last meal of the day)
        return false;
    }

    /**
     * Checks if there's at least one late departure lunch.
     * Handles three cases:
     * 1. More than 1 day after: intermediate days have lunches (regardless of departure time)
     * 2. Exactly 1 day after: YES if can eat lunch on Day +1 OR Day 0 lunch is late departure
     * 3. On boundary date: lunch is late departure if departure time allows it AND lunch is after event end meal
     */
    public boolean hasLateDepartureLunch() {
        if (departureDate == null || eventBoundaryEndDate == null) {
            return false;
        }
        // Case 1: More than 1 day after - intermediate days have lunches
        if (departureDate.isAfter(eventBoundaryEndDate.plusDays(1))) {
            return true;
        }
        // Case 2: Exactly 1 day after - check BOTH Day +1 lunch AND Day 0 lunch
        if (departureDate.equals(eventBoundaryEndDate.plusDays(1))) {
            // Can eat lunch on Day +1?
            boolean canEatDay1Lunch = isLateDepartureMeal(departureTime, MealBoundary.LUNCH);
            // Day 0 lunch is late departure if event ends at BREAKFAST (lunch is after event)
            boolean day0LunchIsLate = isMealAfterEventEnd(MealBoundary.LUNCH, eventBoundaryEndMeal);
            return canEatDay1Lunch || day0LunchIsLate;
        }
        // Case 3: On boundary date - lunch must be after event end meal (only when event ends at BREAKFAST)
        if (departureDate.equals(eventBoundaryEndDate)) {
            return isLateDepartureMeal(departureTime, MealBoundary.LUNCH)
                && isMealAfterEventEnd(MealBoundary.LUNCH, eventBoundaryEndMeal);
        }
        return false;
    }

    /**
     * Checks if there's at least one late departure dinner.
     * Handles three cases:
     * 1. More than 1 day after: intermediate days have dinners (regardless of departure time)
     * 2. Exactly 1 day after: YES if can eat dinner on Day +1 OR Day 0 dinner is late departure
     * 3. On boundary date: dinner is late departure if departure time allows it AND dinner is after event end meal
     */
    public boolean hasLateDepartureDinner() {
        if (departureDate == null || eventBoundaryEndDate == null) {
            return false;
        }
        // Case 1: More than 1 day after - intermediate days have dinners
        if (departureDate.isAfter(eventBoundaryEndDate.plusDays(1))) {
            return true;
        }
        // Case 2: Exactly 1 day after - check BOTH Day +1 dinner AND Day 0 dinner
        if (departureDate.equals(eventBoundaryEndDate.plusDays(1))) {
            // Can eat dinner on Day +1?
            boolean canEatDay1Dinner = isLateDepartureMeal(departureTime, MealBoundary.DINNER);
            // Day 0 dinner is late departure if event ends at BREAKFAST or LUNCH (dinner is after event)
            boolean day0DinnerIsLate = isMealAfterEventEnd(MealBoundary.DINNER, eventBoundaryEndMeal);
            return canEatDay1Dinner || day0DinnerIsLate;
        }
        // Case 3: On boundary date - dinner must be after event end meal (when event ends at BREAKFAST or LUNCH)
        if (departureDate.equals(eventBoundaryEndDate)) {
            return isLateDepartureMeal(departureTime, MealBoundary.DINNER)
                && isMealAfterEventEnd(MealBoundary.DINNER, eventBoundaryEndMeal);
        }
        return false;
    }

    /**
     * Returns the order of a meal in the day (BREAKFAST=0, LUNCH=1, DINNER=2).
     */
    private int mealOrder(MealBoundary meal) {
        if (meal == null) return 0;
        switch (meal) {
            case BREAKFAST: return 0;
            case LUNCH: return 1;
            case DINNER: return 2;
            default: return 0;
        }
    }

    /**
     * Checks if the specified meal is an early arrival meal based on the arrival time.
     * MORNING arrival: LUNCH and DINNER are early arrival meals.
     * AFTERNOON arrival: only DINNER is an early arrival meal.
     * EVENING arrival: no meals are early arrival meals.
     */
    private boolean isEarlyArrivalMeal(HasFestivalDaySelectionSection.ArrivalDepartureTime arrival, MealBoundary meal) {
        if (arrival == null || meal == null) return false;
        switch (meal) {
            case BREAKFAST:
                return false; // Breakfast can never be early arrival (need to arrive day before)
            case LUNCH:
                return arrival == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING;
            case DINNER:
                return arrival != HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING;
            default:
                return false;
        }
    }

    /**
     * Checks if the specified meal is a late departure meal based on the departure time.
     * MORNING departure: no meals are late departure meals.
     * AFTERNOON departure: BREAKFAST and LUNCH are late departure meals.
     * EVENING departure: BREAKFAST, LUNCH and DINNER are late departure meals.
     */
    private boolean isLateDepartureMeal(HasFestivalDaySelectionSection.ArrivalDepartureTime departure, MealBoundary meal) {
        if (departure == null || meal == null) return false;
        switch (meal) {
            case BREAKFAST:
                return departure != HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING;
            case LUNCH:
                return departure != HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING;
            case DINNER:
                return departure == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING;
            default:
                return false;
        }
    }

    /**
     * Checks if meal is before the event's first included meal (used for early arrival on boundary date).
     */
    private boolean isMealBeforeEventStart(MealBoundary meal, MealBoundary eventStartMeal) {
        return mealOrder(meal) < mealOrder(eventStartMeal);
    }

    /**
     * Checks if meal is after the event's last included meal (used for late departure on boundary date).
     */
    private boolean isMealAfterEventEnd(MealBoundary meal, MealBoundary eventEndMeal) {
        return mealOrder(meal) > mealOrder(eventEndMeal);
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
        // Prefer DocumentBill prices when available (more accurate)
        if (!mealBillPrices.isEmpty()) {
            int total = 0;
            if (wantsBreakfast.get() && mealBillPrices.containsKey("breakfast")) {
                total += mealBillPrices.get("breakfast");
            }
            if (wantsLunch.get() && mealBillPrices.containsKey("lunch")) {
                total += mealBillPrices.get("lunch");
            }
            if (wantsDinner.get() && mealBillPrices.containsKey("dinner")) {
                total += mealBillPrices.get("dinner");
            }
            return total;
        }

        // Fallback to manual calculation
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
     * Also extracts early arrival and late departure rates when event boundaries are set.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            return;
        }

        // Get all MEALS scheduled items
        List<ScheduledItem> mealsItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);

        // Group by Item to get unique meal types
        Map<Item, List<ScheduledItem>> itemMap = mealsItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            String itemName = item.getName() != null ? item.getName().toLowerCase() : "";

            // Get all daily rates for this meal item
            List<Rate> itemRates = policyAggregate.getDailyRatesStream()
                .filter(r -> r.getItem() != null && r.getItem().getPrimaryKey() != null
                    && r.getItem().getPrimaryKey().equals(item.getPrimaryKey()))
                .collect(Collectors.toList());

            // Find main event rate and early/late rates
            // Strategy: The LOWEST price is the main event rate, higher prices are for early/late
            // This is because early arrival/late departure typically costs more than the main event
            int mainRate = 0;
            int earlyRate = 0;
            int lateRate = 0;

            if (itemRates.isEmpty()) {
                // No rates found
            } else if (itemRates.size() == 1) {
                // Only one rate - use it as the main rate
                mainRate = itemRates.get(0).getPrice();
            } else {
                // Multiple rates - find the lowest price as main, others as early/late
                // First, try to classify by date boundaries if available
                Rate mainEventRate = null;
                Rate earlyArrivalRate = null;
                Rate lateDepartureRate = null;

                for (Rate rate : itemRates) {
                    LocalDate rateStart = rate.getStartDate();
                    LocalDate rateEnd = rate.getEndDate();
                    int price = rate.getPrice();

                    // Try to classify by date boundaries
                    boolean isEarlyRate = eventBoundaryStartDate != null && rateEnd != null
                        && !rateEnd.isAfter(eventBoundaryStartDate.minusDays(1));
                    boolean isLateRate = eventBoundaryEndDate != null && rateStart != null
                        && !rateStart.isBefore(eventBoundaryEndDate.plusDays(1));

                    if (isEarlyRate) {
                        earlyArrivalRate = rate;
                    } else if (isLateRate) {
                        lateDepartureRate = rate;
                    } else {
                        // Could be main rate - keep track for later selection
                        if (mainEventRate == null || price < mainEventRate.getPrice()) {
                            mainEventRate = rate;
                        }
                    }
                }

                // If we couldn't classify clearly by dates, use price-based heuristic:
                // The LOWEST price among unclassified rates is the main event rate
                if (mainEventRate != null) {
                    mainRate = mainEventRate.getPrice();
                } else {
                    // No rate matched as main by date criteria - use lowest price as main
                    int lowestPrice = Integer.MAX_VALUE;
                    for (Rate rate : itemRates) {
                        if (rate.getPrice() < lowestPrice) {
                            lowestPrice = rate.getPrice();
                        }
                    }
                    mainRate = lowestPrice;
                }

                // Set early/late rates
                if (earlyArrivalRate != null) {
                    earlyRate = earlyArrivalRate.getPrice();
                }
                if (lateDepartureRate != null) {
                    lateRate = lateDepartureRate.getPrice();
                }

                // If no early/late rates found by date, but we have multiple rates,
                // use the higher price as the early/late rate (since they typically cost more)
                if (earlyRate == 0 && lateRate == 0 && itemRates.size() > 1) {
                    for (Rate rate : itemRates) {
                        if (rate.getPrice() > mainRate) {
                            // This higher price is the early/late rate
                            earlyRate = rate.getPrice();
                            lateRate = rate.getPrice();
                            break;
                        }
                    }
                }
            }

            // Determine if this is breakfast, lunch, or dinner based on item name
            if (itemName.contains("breakfast") || itemName.contains("morning")) {
                setBreakfastPricePerDay(mainRate);
                breakfastEarlyArrivalPrice = earlyRate;
                breakfastLateDeparturePrice = lateRate;
            } else if (itemName.contains("lunch") || itemName.contains("midday")) {
                setLunchPricePerDay(mainRate);
                setLunchEarlyArrivalPrice(earlyRate);
                setLunchLateDeparturePrice(lateRate);
            } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                setDinnerPricePerDay(mainRate);
                setDinnerEarlyArrivalPrice(earlyRate);
                setDinnerLateDeparturePrice(lateRate);
            }
        }

        // Load dietary options from DIET item family
        loadDietaryOptions(policyAggregate);

        // Rebuild UI to reflect new prices and dietary options
        rebuildMealCards();
        rebuildDietarySection();
    }

    /**
     * Loads dietary options from getDietItemPolicies() in PolicyAggregate.
     * Uses ItemPolicy.isDefault() to find the default dietary option.
     * Falls back to first option if no default is specified.
     */
    protected void loadDietaryOptions(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) return;

        // Get all DIET item policies directly from PolicyAggregate
        List<ItemPolicy> dietPolicies = policyAggregate.getDietItemPolicies();

        // Extract Items from policies and find default
        dietaryOptions.clear();
        Item defaultDiet = null;

        for (ItemPolicy policy : dietPolicies) {
            Item item = policy.getItem();
            if (item != null) {
                dietaryOptions.add(item);

                // Check if this is the default
                if (Boolean.TRUE.equals(policy.isDefault())) {
                    defaultDiet = item;
                }
            }
        }

        // Auto-select: prefer default from ItemPolicy, fallback to first option
        if (selectedDietaryItem.get() == null) {
            if (defaultDiet != null) {
                selectedDietaryItem.set(defaultDiet);
            } else if (!dietaryOptions.isEmpty()) {
                selectedDietaryItem.set(dietaryOptions.get(0));
            }
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
     * Call this after updating early arrival/late departure prices to refresh the display.
     */
    public void rebuildMealCards() {
        // Recalculate early arrival / late departure pricing flags based on current dates, times, and boundaries
        recalculatePricingFlags();

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

        // Update the info box with pricing information
        updateInfoBoxPricing();

        // Update the summary to reflect new prices (including early/late breakdown)
        updateSummary();
    }

    /**
     * Recalculates showEarlyArrivalPricing and showLateDeparturePricing based on current state.
     * Uses ScheduledItems.isInPeriod to check if meals fall within the user's stay and main event periods.
     *
     * Arrival/departure time meanings:
     * - MORNING = before lunch (user arrives/departs before lunch starts)
     * - AFTERNOON = before dinner (user arrives/departs after lunch but before dinner)
     * - EVENING = after dinner (user arrives/departs after dinner ends)
     *
     * These flags indicate whether early/late pricing should be displayed:
     * - Early arrival: meals that are within the user's stay but outside the main event period (before it starts)
     * - Late departure: meals that are within the user's stay but outside the main event period (after it ends)
     */
    protected void recalculatePricingFlags() {
        boolean hasEarlyPrices = lunchEarlyArrivalPrice > 0 || dinnerEarlyArrivalPrice > 0;
        boolean hasLatePrices = lunchLateDeparturePrice > 0 || dinnerLateDeparturePrice > 0;

        // Reset flags
        showEarlyArrivalPricing = false;
        showLateDeparturePricing = false;

        // If we have the main event period and meal ScheduledItems, use isInPeriod for accurate check
        if (mainEventPeriod != null && !mealScheduledItems.isEmpty()) {
            // Create a Period representing the user's stay
            // The start/end times are derived from the meal ScheduledItems based on arrival/departure selection
            Period userStayPeriod = createUserStayPeriod();

            // Check each meal ScheduledItem
            for (ScheduledItem mealItem : mealScheduledItems) {
                LocalDate mealDate = mealItem.getDate();
                if (mealDate == null) continue;

                // Check if this meal is within the user's stay period using isInPeriod
                boolean isInUserStay = userStayPeriod != null && ScheduledItems.isInPeriod(mealItem, userStayPeriod);
                if (!isInUserStay) continue; // User doesn't get this meal

                // Get meal name for logging
                String itemName = mealItem.getName();
                if (itemName == null && mealItem.getItem() != null) {
                    itemName = mealItem.getItem().getName();
                }

                // Use isInPeriod to check if this meal is within the main event period
                boolean isInMainEvent = ScheduledItems.isInPeriod(mealItem, mainEventPeriod);

                if (!isInMainEvent) {
                    // This meal is within user's stay but outside the main event period
                    // Determine if it's early arrival or late departure based on date
                    LocalDate periodStart = mainEventPeriod.getStartDate();
                    LocalDate periodEnd = mainEventPeriod.getEndDate();

                    if (periodStart != null && (mealDate.isBefore(periodStart) || mealDate.equals(periodStart))) {
                        // Before or on start date - early arrival
                        if (hasEarlyPrices) {
                            showEarlyArrivalPricing = true;
                        }
                    }
                    if (periodEnd != null && (mealDate.isAfter(periodEnd) || mealDate.equals(periodEnd))) {
                        // After or on end date - late departure
                        if (hasLatePrices) {
                            showLateDeparturePricing = true;
                        }
                    }
                }
            }
        } else {
            // Fallback: use date-based heuristics if we don't have the Period/ScheduledItems
            if (hasEarlyPrices && arrivalDate != null && eventBoundaryStartDate != null) {
                if (arrivalDate.isBefore(eventBoundaryStartDate)) {
                    showEarlyArrivalPricing = true;
                }
            }
            if (hasLatePrices && departureDate != null && eventBoundaryEndDate != null) {
                if (departureDate.isAfter(eventBoundaryEndDate)) {
                    showLateDeparturePricing = true;
                }
            }
        }
    }

    /**
     * Creates a Period representing the user's stay based on arrival/departure dates and times.
     * The start/end times are derived from the actual meal ScheduledItems:
     * - MORNING = before lunch → use lunch start time as boundary
     * - AFTERNOON = before dinner → use dinner start time as boundary
     * - EVENING = after dinner → use dinner end time as boundary
     *
     * @return a Period representing the user's stay, or null if dates are not set
     */
    protected Period createUserStayPeriod() {
        if (arrivalDate == null || departureDate == null) {
            return null;
        }

        // Find the lunch and dinner ScheduledItems for boundary times
        ScheduledItem arrivalLunch = findMealOnDate(arrivalDate, "lunch");
        ScheduledItem arrivalDinner = findMealOnDate(arrivalDate, "dinner");
        ScheduledItem departureLunch = findMealOnDate(departureDate, "lunch");
        ScheduledItem departureDinner = findMealOnDate(departureDate, "dinner");

        // Determine arrival time based on arrival selection
        // MORNING = before lunch → arrival time is before lunch starts (use MIN to include lunch)
        // AFTERNOON = before dinner → arrival time is after lunch ends, before dinner (use lunch end time)
        // EVENING = after dinner → arrival time is after dinner ends (use dinner end time)
        final LocalTime arrivalStartTime;
        switch (arrivalTime) {
            case MORNING:
                // User arrives before lunch - they get lunch and dinner
                arrivalStartTime = LocalTime.MIN;
                break;
            case AFTERNOON:
                // User arrives after lunch, before dinner - they get dinner only
                // Use lunch end time as the arrival time
                arrivalStartTime = arrivalLunch != null ? ScheduledItems.getSessionEndTimeOrMax(arrivalLunch) : LocalTime.of(14, 0);
                break;
            case EVENING:
            default:
                // User arrives after dinner - they get no meals on arrival day
                // Use dinner end time as the arrival time
                arrivalStartTime = arrivalDinner != null ? ScheduledItems.getSessionEndTimeOrMax(arrivalDinner) : LocalTime.of(20, 0);
                break;
        }

        // Determine departure time based on departure selection
        // MORNING = before lunch → departure time is before lunch starts (use lunch start time)
        // AFTERNOON = before dinner → departure time is after lunch ends, before dinner (use dinner start time)
        // EVENING = after dinner → departure time is after dinner ends (use MAX to include dinner)
        final LocalTime departureEndTime;
        switch (departureTime) {
            case MORNING:
                // User departs before lunch - they don't get lunch or dinner
                // Use lunch start time as the departure boundary
                departureEndTime = departureLunch != null ? ScheduledItems.getSessionStartTimeOrMin(departureLunch) : LocalTime.of(12, 0);
                break;
            case AFTERNOON:
                // User departs after lunch, before dinner - they get lunch only
                // Use dinner start time as the departure boundary
                departureEndTime = departureDinner != null ? ScheduledItems.getSessionStartTimeOrMin(departureDinner) : LocalTime.of(18, 0);
                break;
            case EVENING:
            default:
                // User departs after dinner - they get all meals
                departureEndTime = LocalTime.MAX;
                break;
        }

        // Create a simple Period implementation
        final LocalDate startDate = arrivalDate;
        final LocalDate endDate = departureDate;
        final LocalTime startTime = arrivalStartTime;
        final LocalTime endTime = departureEndTime;

        return new Period() {
            @Override public LocalDate getStartDate() { return startDate; }
            @Override public LocalTime getStartTime() { return startTime; }
            @Override public LocalDate getEndDate() { return endDate; }
            @Override public LocalTime getEndTime() { return endTime; }
        };
    }

    /**
     * Finds a meal ScheduledItem on a specific date.
     *
     * @param date the date to search for
     * @param mealType "lunch" or "dinner"
     * @return the ScheduledItem for the meal on that date, or null if not found
     */
    protected ScheduledItem findMealOnDate(LocalDate date, String mealType) {
        if (date == null || mealScheduledItems == null) return null;

        for (ScheduledItem item : mealScheduledItems) {
            if (!date.equals(item.getDate())) continue;

            String itemName = item.getName();
            if (itemName == null && item.getItem() != null) {
                itemName = item.getItem().getName();
            }
            if (itemName == null) continue;

            String lowerName = itemName.toLowerCase();
            if ("lunch".equals(mealType) && (lowerName.contains("lunch") || lowerName.contains("midday"))) {
                return item;
            }
            if ("dinner".equals(mealType) && (lowerName.contains("dinner") || lowerName.contains("supper") || lowerName.contains("evening"))) {
                return item;
            }
        }
        return null;
    }

}
