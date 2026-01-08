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

import java.util.List;
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
 *   <li>{@code .bookingpage-meal-toggle} - meal toggle card</li>
 *   <li>{@code .bookingpage-meal-toggle.selected} - selected state</li>
 *   <li>{@code .bookingpage-dietary-option} - dietary preference option</li>
 *   <li>{@code .bookingpage-dietary-option.selected} - selected dietary preference</li>
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

    // === MEAL SELECTION ===
    // Meals are selected by default since accommodation price includes meals
    protected final BooleanProperty wantsBreakfast = new SimpleBooleanProperty(true);  // Auto-included with accommodation
    protected final BooleanProperty wantsLunch = new SimpleBooleanProperty(true);
    protected final BooleanProperty wantsDinner = new SimpleBooleanProperty(true);
    protected final ObjectProperty<DietaryPreference> dietaryPreference = new SimpleObjectProperty<>(DietaryPreference.VEGETARIAN);

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
    protected VBox mealsContainer;

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

        // Info box
        infoLabel = I18nControls.newLabel(BookingPageI18nKeys.AllMealsVegetarian);
        HBox infoBox = BookingPageUIBuilder.createInfoBox(BookingPageI18nKeys.AllMealsVegetarian, BookingPageUIBuilder.InfoBoxType.INFO);

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

        container.getChildren().addAll(sectionHeader, infoBox, mealsContainer, dietarySection);
        VBox.setMargin(infoBox, new Insets(0, 0, 8, 0));
    }

    protected HBox createMealToggle(Object titleKey, Object subtitleKey, BooleanProperty selectedProperty, int pricePerDay) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-meal-toggle");

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Initial border style
        updateCardStyle(card, selectedProperty.get(), finalScheme);

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(selectedProperty, colorScheme);

        // SVG Icon (sun for lunch, moon for dinner)
        Node iconNode;
        boolean isLunch = titleKey == BookingPageI18nKeys.Lunch;
        if (isLunch) {
            // Sun icon for lunch (composite with rays and circle)
            iconNode = BookingPageUIBuilder.createSunIcon(Color.web("#f59e0b"), 0.85);
        } else {
            // Moon icon for dinner
            SVGPath moonIcon = new SVGPath();
            moonIcon.setContent(BookingPageUIBuilder.ICON_MOON);
            moonIcon.setStroke(Color.web("#6366f1"));
            moonIcon.setStrokeWidth(2);
            moonIcon.setFill(Color.web("#6366f1").deriveColor(0, 1, 1, 0.2));
            moonIcon.setScaleX(0.85);
            moonIcon.setScaleY(0.85);
            iconNode = moonIcon;
        }

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(titleKey);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        Label subtitle = I18nControls.newLabel(subtitleKey);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        textContent.getChildren().addAll(title, subtitle);

        // Price
        Label price = new Label(formatPrice(pricePerDay) + "/day");
        price.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        card.getChildren().addAll(checkbox, iconNode, textContent, price);

        // Selection handling - update styling and CSS class
        selectedProperty.addListener((obs, old, newVal) -> {
            updateCardStyle(card, newVal, finalScheme);
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
     * Updates the card style based on selection state.
     */
    private void updateCardStyle(HBox card, boolean selected, BookingFormColorScheme scheme) {
        if (selected) {
            card.setStyle("-fx-border-color: " + toHex(scheme.getPrimary()) + "; -fx-border-width: 2; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: " + toHex(scheme.getSelectedBg()) + ";");
        } else {
            card.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 2; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");
        }
    }

    /**
     * Creates the breakfast toggle card.
     * Breakfast is automatically included with accommodation and shows as "Included" rather than a price.
     */
    protected HBox createBreakfastToggle() {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("bookingpage-meal-toggle");

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Always selected style when accommodation is selected (breakfast is auto-included)
        updateCardStyle(card, wantsBreakfast.get(), finalScheme);

        // Checkbox indicator (always checked when has accommodation)
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(wantsBreakfast, colorScheme);

        // Coffee cup icon for breakfast (using simple path)
        SVGPath coffeeIcon = new SVGPath();
        // Coffee cup SVG path
        coffeeIcon.setContent("M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3");
        coffeeIcon.setStroke(Color.web("#92400e"));  // Amber/brown color for breakfast
        coffeeIcon.setStrokeWidth(2);
        coffeeIcon.setFill(Color.TRANSPARENT);
        coffeeIcon.setScaleX(0.85);
        coffeeIcon.setScaleY(0.85);

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label("Breakfast");  // Hardcoded for now, can be i18n later
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        Label subtitle = new Label("Included with accommodation");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        textContent.getChildren().addAll(title, subtitle);

        // Price/status label
        Label priceLabel = new Label("Included");
        priceLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #059669;");  // Green for "included"

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
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");

        // Vegetarian option
        HBox vegetarianOption = createDietaryOption(BookingPageI18nKeys.Vegetarian, DietaryPreference.VEGETARIAN);

        // Vegan option
        HBox veganOption = createDietaryOption(BookingPageI18nKeys.Vegan, DietaryPreference.VEGAN);

        section.getChildren().addAll(label, vegetarianOption, veganOption);
        return section;
    }

    protected HBox createDietaryOption(Object labelKey, DietaryPreference preference) {
        HBox option = new HBox(8);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(8, 16, 8, 16));
        option.setCursor(Cursor.HAND);
        option.getStyleClass().add("bookingpage-dietary-option");

        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");

        option.getChildren().add(label);

        // Selection handling
        if (dietaryPreference.get() == preference) {
            option.getStyleClass().add("selected");
        }
        dietaryPreference.addListener((obs, old, newVal) -> {
            if (newVal == preference) {
                option.getStyleClass().add("selected");
            } else {
                option.getStyleClass().remove("selected");
            }
        });

        option.setOnMouseClicked(e -> dietaryPreference.set(preference));

        return option;
    }

    protected void setupBindings() {
        // Show/hide dietary section based on meal selection
        wantsBreakfast.addListener((obs, old, newVal) -> updateDietarySectionVisibility());
        wantsLunch.addListener((obs, old, newVal) -> updateDietarySectionVisibility());
        wantsDinner.addListener((obs, old, newVal) -> updateDietarySectionVisibility());
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
    }

    @Override
    public boolean hasAccommodation() {
        return hasAccommodation;
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

        // Rebuild UI to reflect new prices
        rebuildMealCards();
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

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Converts a JavaFX Color to a CSS hex string.
     * @param color the Color to convert
     * @return hex string like "#RRGGBB"
     */
    protected static String toHex(Color color) {
        if (color == null) return "#000000";
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
}
