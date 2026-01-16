package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.platform.console.Console;
import javafx.beans.property.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the additional options section.
 * Dynamically displays additional services loaded from PolicyAggregate.
 *
 * <p>Options are loaded from the database based on Item families:</p>
 * <ul>
 *   <li>PARKING - Parking options</li>
 *   <li>TRANSPORT - Shuttle/transport options</li>
 *   <li>Any other additional service items configured for the event</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-additional-options-section} - section container</li>
 *   <li>{@code .bookingpage-checkbox-card} - option checkbox card</li>
 *   <li>{@code .bookingpage-checkbox-card.selected} - selected state</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasAdditionalOptionsSection
 */
public class DefaultAdditionalOptionsSection implements HasAdditionalOptionsSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === DYNAMIC OPTIONS ===
    protected final List<AdditionalOption> options = new ArrayList<>();

    // === LEGACY OPTIONS SELECTION (for backward compatibility) ===
    protected final BooleanProperty assistedListening = new SimpleBooleanProperty(false);
    protected final BooleanProperty needsParking = new SimpleBooleanProperty(false);
    protected final ObjectProperty<ParkingType> parkingType = new SimpleObjectProperty<>(ParkingType.STANDARD);
    protected final BooleanProperty shuttleOutbound = new SimpleBooleanProperty(false);
    protected final BooleanProperty shuttleReturn = new SimpleBooleanProperty(false);

    // === PRICING (legacy, kept for backward compatibility) ===
    protected int parkingPricePerDay = 0;
    protected int shuttlePrice = 0;
    protected int daysCount = 1;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox optionsContainer;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    // === CALLBACKS ===
    protected Runnable onSelectionChangedCallback;

    public DefaultAdditionalOptionsSection() {
        buildUI();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(12);
        container.getStyleClass().add("bookingpage-additional-options-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.AdditionalOptions, StyledSectionHeader.ICON_PLUS_CIRCLE);

        // Options container - will hold dynamically created cards
        optionsContainer = new VBox(12);
        optionsContainer.setAlignment(Pos.TOP_LEFT);

        // Build option cards from current options list
        buildOptionCards();

        container.getChildren().addAll(sectionHeader, optionsContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));
    }

    /**
     * Builds option cards from the current options list.
     * Called after options are loaded from PolicyAggregate.
     */
    protected void buildOptionCards() {
        optionsContainer.getChildren().clear();

        if (options.isEmpty()) {
            // No options to display
            return;
        }

        // Group options by family for visual organization
        Map<KnownItemFamily, List<AdditionalOption>> optionsByFamily = options.stream()
            .collect(Collectors.groupingBy(AdditionalOption::getItemFamily));

        // Add cards for each family
        for (Map.Entry<KnownItemFamily, List<AdditionalOption>> entry : optionsByFamily.entrySet()) {
            KnownItemFamily family = entry.getKey();
            List<AdditionalOption> familyOptions = entry.getValue();

            // Create cards for each option in this family
            for (AdditionalOption option : familyOptions) {
                VBox card = createOptionCard(option);
                optionsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Creates a card for a single additional option.
     */
    protected VBox createOptionCard(AdditionalOption option) {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-checkbox-card");

        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(16));
        mainRow.setCursor(Cursor.HAND);

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(option.selectedProperty(), colorScheme);

        // Icon (if available)
        Node iconNode = null;
        if (option.getIconSvg() != null && !option.getIconSvg().isEmpty()) {
            SVGPath icon = new SVGPath();
            icon.setContent(option.getIconSvg());
            icon.setStroke(Color.web("#64748b"));
            icon.setStrokeWidth(2);
            icon.setFill(Color.TRANSPARENT);
            icon.setScaleX(0.85);
            icon.setScaleY(0.85);
            iconNode = icon;
        }

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label(option.getName());
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");

        if (option.getDescription() != null && !option.getDescription().isEmpty()) {
            Label subtitle = new Label(option.getDescription());
            subtitle.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            textContent.getChildren().addAll(title, subtitle);
        } else {
            textContent.getChildren().add(title);
        }

        // Price label
        Label priceLabel = new Label(formatPrice(option));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Assemble the row
        if (iconNode != null) {
            mainRow.getChildren().addAll(checkbox, iconNode, textContent, priceLabel);
        } else {
            mainRow.getChildren().addAll(checkbox, textContent, priceLabel);
        }

        card.getChildren().add(mainRow);

        // Initial selection state
        if (option.isSelected()) {
            card.getStyleClass().add("selected");
        }

        // Selection handling - CSS handles styling via .selected class
        option.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
            // Sync with legacy properties for backward compatibility
            syncLegacyPropertiesFromOption(option);
            // Notify callback if registered
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        mainRow.setOnMouseClicked(e -> option.setSelected(!option.isSelected()));

        return card;
    }

    /**
     * Formats the price for display based on option properties.
     */
    protected String formatPrice(AdditionalOption option) {
        if (option.getPrice() == 0) {
            return "Free";
        }

        String priceStr = "$" + (option.getPrice() / 100);
        if (option.isPerDay()) {
            priceStr += "/day";
        }
        if (option.isPerPerson()) {
            priceStr += " pp";
        }
        return priceStr;
    }

    /**
     * Syncs legacy properties when a dynamic option selection changes.
     * This maintains backward compatibility with code using the old API.
     */
    protected void syncLegacyPropertiesFromOption(AdditionalOption option) {
        if (option.getItemFamily() == KnownItemFamily.PARKING) {
            needsParking.set(option.isSelected());
        } else if (option.getItemFamily() == KnownItemFamily.TRANSPORT) {
            String name = option.getName() != null ? option.getName().toLowerCase() : "";
            if (name.contains("outbound") || name.contains("arrival") || name.contains("to ")) {
                shuttleOutbound.set(option.isSelected());
            } else if (name.contains("return") || name.contains("departure") || name.contains("from ")) {
                shuttleReturn.set(option.isSelected());
            } else {
                // Generic transport - set both for legacy compatibility
                shuttleOutbound.set(option.isSelected());
                shuttleReturn.set(option.isSelected());
            }
        }
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AdditionalOptions;
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
    // HasAdditionalOptionsSection INTERFACE
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
    public void setParkingPricePerDay(int price) {
        this.parkingPricePerDay = price;
    }

    @Override
    public void setShuttlePrice(int price) {
        this.shuttlePrice = price;
    }

    @Override
    public void setDaysCount(int count) {
        this.daysCount = count;
    }

    @Override
    public BooleanProperty assistedListeningProperty() {
        return assistedListening;
    }

    @Override
    public BooleanProperty needsParkingProperty() {
        return needsParking;
    }

    @Override
    public ObjectProperty<ParkingType> parkingTypeProperty() {
        return parkingType;
    }

    @Override
    public BooleanProperty shuttleOutboundProperty() {
        return shuttleOutbound;
    }

    @Override
    public BooleanProperty shuttleReturnProperty() {
        return shuttleReturn;
    }

    @Override
    public int getTotalParkingCost() {
        // Calculate from dynamic options
        return options.stream()
            .filter(opt -> opt.getItemFamily() == KnownItemFamily.PARKING && opt.isSelected())
            .mapToInt(opt -> opt.isPerDay() ? opt.getPrice() * daysCount : opt.getPrice())
            .sum();
    }

    @Override
    public int getTotalShuttleCost() {
        // Calculate from dynamic options
        return options.stream()
            .filter(opt -> opt.getItemFamily() == KnownItemFamily.TRANSPORT && opt.isSelected())
            .mapToInt(AdditionalOption::getPrice)
            .sum();
    }

    // ========================================
    // DYNAMIC OPTIONS API
    // ========================================

    @Override
    public List<AdditionalOption> getOptions() {
        return options;
    }

    @Override
    public void clearOptions() {
        options.clear();
        if (optionsContainer != null) {
            optionsContainer.getChildren().clear();
        }
    }

    @Override
    public void addOption(AdditionalOption option) {
        options.add(option);
    }

    @Override
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChangedCallback = callback;
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    // Item families that are handled by other sections and should NOT appear as additional options
    private static final java.util.Set<KnownItemFamily> EXCLUDED_FAMILIES = java.util.Set.of(
        KnownItemFamily.TEACHING,      // Handled by teaching/event section
        KnownItemFamily.ACCOMMODATION, // Handled by accommodation section
        KnownItemFamily.MEALS,         // Handled by meals section
        KnownItemFamily.DIET,          // Handled by meals section (dietary options)
        KnownItemFamily.TAX,           // Not user-selectable
        KnownItemFamily.UNKNOWN        // Unknown items
    );

    // Dynamic exclusion for audio recording (when handled by dedicated phase section)
    private boolean excludeAudioRecording = false;

    // Dynamic exclusion for transport (when handled by dedicated transport section)
    private boolean excludeTransport = false;

    // Dynamic exclusion for parking (when handled by dedicated transport section)
    private boolean excludeParking = false;

    /**
     * Sets whether to exclude audio recording items from additional options.
     * Use this when audio recording is handled by a dedicated phase coverage section.
     *
     * @param exclude true to exclude audio recording items, false to include them
     */
    public void setExcludeAudioRecording(boolean exclude) {
        this.excludeAudioRecording = exclude;
    }

    /**
     * Returns whether audio recording items are excluded from additional options.
     */
    public boolean isExcludeAudioRecording() {
        return excludeAudioRecording;
    }

    /**
     * Sets whether to exclude transport items from additional options.
     * Use this when transport is handled by a dedicated transport section.
     *
     * @param exclude true to exclude transport items, false to include them
     */
    public void setExcludeTransport(boolean exclude) {
        this.excludeTransport = exclude;
    }

    /**
     * Returns whether transport items are excluded from additional options.
     */
    public boolean isExcludeTransport() {
        return excludeTransport;
    }

    /**
     * Sets whether to exclude parking items from additional options.
     * Use this when parking is handled by a dedicated transport section.
     *
     * @param exclude true to exclude parking items, false to include them
     */
    public void setExcludeParking(boolean exclude) {
        this.excludeParking = exclude;
    }

    /**
     * Returns whether parking items are excluded from additional options.
     */
    public boolean isExcludeParking() {
        return excludeParking;
    }

    /**
     * Sets whether to exclude both parking and transport items from additional options.
     * Convenience method to call setExcludeParking(true) and setExcludeTransport(true).
     *
     * @param exclude true to exclude parking and transport items
     */
    public void setExcludeParkingAndTransport(boolean exclude) {
        setExcludeParking(exclude);
        setExcludeTransport(exclude);
    }

    /**
     * Populates additional options from PolicyAggregate data.
     * Loads ALL items except those handled by other sections (teaching, accommodation, meals, diet, tax).
     * This allows any additional service type configured in the database to be displayed.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultAdditionalOptionsSection: PolicyAggregate is null");
            return;
        }

        // Clear existing options
        clearOptions();

        // Load all scheduled items and group by item family
        List<ScheduledItem> allScheduledItems = policyAggregate.getScheduledItems();
        if (allScheduledItems == null || allScheduledItems.isEmpty()) {
            Console.log("DefaultAdditionalOptionsSection: No scheduled items found");
            rebuildUI();
            return;
        }

        // Group by Item, filtering out null items
        Map<Item, List<ScheduledItem>> itemMap = allScheduledItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        // Sort by Item.ord
        List<Item> sortedItems = itemMap.keySet().stream()
            .sorted((a, b) -> {
                Integer ordA = a.getOrd() != null ? a.getOrd() : Integer.MAX_VALUE;
                Integer ordB = b.getOrd() != null ? b.getOrd() : Integer.MAX_VALUE;
                return ordA.compareTo(ordB);
            })
            .collect(Collectors.toList());

        // Process each item
        for (Item item : sortedItems) {
            KnownItemFamily family = item.getItemFamilyType();

            // Skip items from excluded families (handled by other sections)
            if (EXCLUDED_FAMILIES.contains(family)) {
                continue;
            }

            // Skip audio recording items if they're handled by dedicated phase section
            if (excludeAudioRecording && family == KnownItemFamily.AUDIO_RECORDING) {
                Console.log("DefaultAdditionalOptionsSection: Excluding audio recording item (handled by phase section)");
                continue;
            }

            // Skip transport items if they're handled by dedicated transport section
            if (excludeTransport && family == KnownItemFamily.TRANSPORT) {
                Console.log("DefaultAdditionalOptionsSection: Excluding transport item (handled by transport section)");
                continue;
            }

            // Skip parking items if they're handled by dedicated transport section
            if (excludeParking && family == KnownItemFamily.PARKING) {
                Console.log("DefaultAdditionalOptionsSection: Excluding parking item (handled by transport section)");
                continue;
            }

            // Get price from rate
            int price = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .map(Rate::getPrice)
                .orElse(0);

            // Determine if per-day based on family (parking is typically per-day)
            boolean perDay = (family == KnownItemFamily.PARKING);

            // Get per-person flag from rate
            boolean perPerson = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .map(r -> !Boolean.FALSE.equals(r.isPerPerson()))
                .orElse(false);

            // Get item name and description
            String name = item.getName() != null ? item.getName() : "";
            String description = "";
            if (item.getLabel() != null && item.getLabel().getEn() != null) {
                description = item.getLabel().getEn();
            }

            // Determine default icon based on family
            String iconSvg = getDefaultIconForFamily(family);
            if (item.getIcon() != null && !item.getIcon().isEmpty()) {
                iconSvg = item.getIcon();
            }

            AdditionalOption option = new AdditionalOption(
                item.getPrimaryKey(),
                item,
                name,
                description,
                price,
                family,
                perDay,
                perPerson,
                iconSvg
            );

            addOption(option);
            Console.log("DefaultAdditionalOptionsSection: Added option '" + name + "' (" + family.name() + ") price=" + price);
        }

        Console.log("DefaultAdditionalOptionsSection: Loaded " + options.size() + " options from PolicyAggregate");

        // Rebuild UI to show new options
        rebuildUI();
    }

    /**
     * Returns a default icon SVG path for a given item family.
     */
    protected String getDefaultIconForFamily(KnownItemFamily family) {
        if (family == null) return null;
        switch (family) {
            case TRANSPORT:
                return BookingPageUIBuilder.ICON_PLANE;
            case PARKING:
                return null; // No default icon for parking
            case VIDEO:
                return null; // Could add video icon
            case AUDIO_RECORDING:
                return null; // Could add audio icon
            case TRANSLATION:
                return null; // Could add translation icon
            default:
                return null;
        }
    }

    /**
     * Rebuilds the UI to reflect updated options.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }
}
