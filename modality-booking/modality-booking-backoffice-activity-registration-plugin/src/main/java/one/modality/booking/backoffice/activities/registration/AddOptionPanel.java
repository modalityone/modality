package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Add Option Panel for the BookingTab.
 * <p>
 * Displays available booking options grouped by ItemFamily.
 * Uses data from WorkingBooking/PolicyAggregate to show:
 * - ItemFamilies as category tabs
 * - Items belonging to selected family as chips
 * - Availability from ScheduledItem.getGuestsAvailability()
 * <p>
 * Based on RegistrationDashboardFull.jsx AddOptionPanel section (lines 5600-5815).
 *
 * @author Claude Code
 */
public class AddOptionPanel {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    // UI state
    private final BooleanProperty expandedProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<ItemFamily> selectedFamilyProperty = new SimpleObjectProperty<>();

    // Data from PolicyAggregate
    private final ObservableList<ItemFamily> availableFamilies = FXCollections.observableArrayList();
    private final ObservableList<ItemWithAvailability> availableItems = FXCollections.observableArrayList();
    private final ObservableList<DocumentLine> existingLines = FXCollections.observableArrayList();

    // WorkingBooking reference (set by BookingTab)
    private WorkingBooking workingBooking;

    // Callbacks
    private Runnable onItemAdded;

    // UI references for updating
    private FlowPane itemChipsContainer;

    public AddOptionPanel(ViewDomainActivityBase activity, RegistrationPresentationModel pm,
                          Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
    }

    /**
     * Sets the WorkingBooking to use for loading items from PolicyAggregate.
     */
    public void setWorkingBooking(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        if (expandedProperty.get()) {
            loadAvailableOptions();
        }
    }

    /**
     * Builds the Add Option panel UI.
     */
    public Node buildUi() {
        VBox container = new VBox();

        // Toggle button (collapsed state)
        Button toggleButton = createToggleButton();

        // Expanded panel content
        VBox expandedContent = createExpandedContent();

        // Bind visibility
        toggleButton.visibleProperty().bind(expandedProperty.not());
        toggleButton.managedProperty().bind(expandedProperty.not());
        expandedContent.visibleProperty().bind(expandedProperty);
        expandedContent.managedProperty().bind(expandedProperty);

        container.getChildren().addAll(toggleButton, expandedContent);

        return container;
    }

    /**
     * Creates the toggle button to expand the panel.
     */
    private Button createToggleButton() {
        Button btn = new Button();
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);

        Label plusIcon = new Label("+");
        plusIcon.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label text = new Label("Add Option");
        text.setFont(Font.font(13));

        Label chevron = new Label("\u25BC"); // ▼
        chevron.setFont(Font.font(10));
        chevron.setTextFill(TEXT_MUTED);

        content.getChildren().addAll(plusIcon, text, chevron);

        btn.setGraphic(content);
        btn.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_MEDIUM));
        btn.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setCursor(Cursor.HAND);
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnAction(e -> {
            expandedProperty.set(true);
            loadAvailableOptions();
        });

        return btn;
    }

    /**
     * Creates the expanded panel content with family tabs and item chips.
     */
    private VBox createExpandedContent() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setBackground(createBackground(SAND, BORDER_RADIUS_MEDIUM));
        panel.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Header with title and collapse button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Add to booking");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        title.setTextFill(WARM_BROWN);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button collapseBtn = new Button("\u2715"); // ✕
        collapseBtn.setFont(Font.font(14));
        collapseBtn.setTextFill(TEXT_MUTED);
        collapseBtn.setBackground(Background.EMPTY);
        collapseBtn.setBorder(Border.EMPTY);
        collapseBtn.setCursor(Cursor.HAND);
        collapseBtn.setPadding(new Insets(2));
        collapseBtn.setOnAction(e -> expandedProperty.set(false));

        header.getChildren().addAll(title, collapseBtn);

        // Family tabs container
        HBox familyTabs = new HBox(6);
        familyTabs.setAlignment(Pos.CENTER_LEFT);

        // Update family tabs when families change
        availableFamilies.addListener((javafx.collections.ListChangeListener<ItemFamily>) c -> {
            updateFamilyTabs(familyTabs);
        });

        // Item chips container (will be populated based on selected family)
        itemChipsContainer = new FlowPane();
        itemChipsContainer.setHgap(4);
        itemChipsContainer.setVgap(4);
        itemChipsContainer.setPadding(new Insets(8, 0, 0, 0));

        // Update item chips when family changes or items load
        FXProperties.runNowAndOnPropertiesChange(() -> {
            updateItemChips();
        }, selectedFamilyProperty);

        // Also update when items list changes
        availableItems.addListener((javafx.collections.ListChangeListener<ItemWithAvailability>) c -> {
            updateItemChips();
        });

        // Legend
        HBox legend = createLegend();

        panel.getChildren().addAll(header, familyTabs, itemChipsContainer, legend);

        return panel;
    }

    /**
     * Updates the family tabs based on available families.
     */
    private void updateFamilyTabs(HBox container) {
        container.getChildren().clear();

        for (ItemFamily family : availableFamilies) {
            Node tab = createFamilyTab(family);
            container.getChildren().add(tab);
        }

        // Select first family if none selected
        if (selectedFamilyProperty.get() == null && !availableFamilies.isEmpty()) {
            selectedFamilyProperty.set(availableFamilies.get(0));
        }
    }

    /**
     * Creates a family tab chip with emoji icon.
     */
    private Node createFamilyTab(ItemFamily family) {
        String emoji = getFamilyEmoji(family);

        Button chip = new Button(emoji);
        chip.setFont(Font.font(14));
        chip.setPadding(new Insets(4, 6, 4, 6));
        chip.setCursor(Cursor.HAND);

        // Style based on selection
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean selected = Entities.samePrimaryKey(selectedFamilyProperty.get(), family);
            if (selected) {
                Color familyColor = getFamilyColor(family);
                chip.setBackground(createBackground(familyColor.deriveColor(0, 0.3, 1.2, 0.3), 6));
                chip.setBorder(createBorder(familyColor, 6));
            } else {
                chip.setBackground(createBackground(Color.WHITE, 6));
                chip.setBorder(createBorder(Color.web("#e5e7eb"), 6));
            }
        }, selectedFamilyProperty);

        chip.setOnAction(e -> selectedFamilyProperty.set(family));

        return chip;
    }

    /**
     * Updates the item chips based on the selected family.
     */
    private void updateItemChips() {
        if (itemChipsContainer == null) return;
        itemChipsContainer.getChildren().clear();

        ItemFamily selectedFamily = selectedFamilyProperty.get();
        if (selectedFamily == null) {
            Label emptyLabel = new Label("Select a category above");
            emptyLabel.setFont(FONT_SMALL);
            emptyLabel.setTextFill(TEXT_MUTED);
            itemChipsContainer.getChildren().add(emptyLabel);
            return;
        }

        // Filter items by selected family
        List<ItemWithAvailability> familyItems = availableItems.stream()
            .filter(item -> item.item != null && item.item.getFamily() != null
                && Entities.samePrimaryKey(item.item.getFamily(), selectedFamily))
            .sorted(Comparator.comparing(item -> item.item.getName() != null ? item.item.getName() : ""))
            .collect(Collectors.toList());

        if (familyItems.isEmpty()) {
            String familyName = selectedFamily.getName() != null ? selectedFamily.getName() : "this category";
            Label emptyLabel = new Label("No " + familyName + " options available");
            emptyLabel.setFont(FONT_SMALL);
            emptyLabel.setTextFill(TEXT_MUTED);
            emptyLabel.setPadding(new Insets(8));
            itemChipsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (ItemWithAvailability item : familyItems) {
            Node chip = createItemChip(item);
            itemChipsContainer.getChildren().add(chip);
        }
    }

    /**
     * Creates an item chip with availability status.
     */
    private Node createItemChip(ItemWithAvailability itemData) {
        Button chip = new Button();
        HBox content = new HBox(3);
        content.setAlignment(Pos.CENTER);

        // Determine status
        String status = getItemStatus(itemData);
        int addedCount = countItemAlreadyAdded(itemData.item, itemData.site);

        // Get colors based on status
        Color bgColor, borderColor, textColor;
        String statusIcon = null;

        if (addedCount > 0) {
            bgColor = Color.web("#f0fdf4");
            borderColor = Color.web("#86efac");
            textColor = Color.web("#166534");
            statusIcon = addedCount > 1 ? "\u00D7" + addedCount : "\u2713"; // ×N or ✓
        } else if ("soldOut".equals(status)) {
            bgColor = Color.web("#fef2f2");
            borderColor = Color.web("#fecaca");
            textColor = Color.web("#991b1b");
        } else if ("limited".equals(status)) {
            bgColor = Color.web("#fffbeb");
            borderColor = Color.web("#fcd34d");
            textColor = TEXT;
        } else {
            bgColor = Color.WHITE;
            borderColor = Color.web("#e5e7eb");
            textColor = TEXT;
        }

        // Status badge (for already added items)
        if (statusIcon != null) {
            Label badgeLabel = new Label(statusIcon);
            badgeLabel.setFont(Font.font("System", FontWeight.BOLD, 9));
            badgeLabel.setTextFill(Color.web("#16a34a"));
            if (addedCount > 1) {
                badgeLabel.setBackground(createBackground(Color.web("#dcfce7"), 3));
                badgeLabel.setPadding(new Insets(1, 4, 1, 4));
            }
            content.getChildren().add(badgeLabel);
        }

        // Item name - show item name, with site name in parentheses if different
        String itemName = itemData.item.getName() != null ? itemData.item.getName() : "Unknown";
        if (itemData.site != null && itemData.site.getName() != null) {
            String siteName = itemData.site.getName();
            // Only append site name if it's different from item name
            if (!siteName.equals(itemName)) {
                itemName = itemName + " (" + siteName + ")";
            }
        }
        Label nameLabel = new Label(itemName);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        nameLabel.setTextFill(textColor);
        content.getChildren().add(nameLabel);

        // Availability suffix for limited items
        if ("limited".equals(status) && itemData.availability != null) {
            Label availLabel = new Label("(" + itemData.availability + ")");
            availLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));
            availLabel.setTextFill(Color.web("#d97706"));
            content.getChildren().add(availLabel);
        }

        // Sold out badge
        if ("soldOut".equals(status)) {
            Label soldOutLabel = new Label("SOLD OUT");
            soldOutLabel.setFont(Font.font("System", FontWeight.BOLD, 7));
            soldOutLabel.setTextFill(Color.web("#dc2626"));
            soldOutLabel.setBackground(createBackground(Color.web("#fee2e2"), 3));
            soldOutLabel.setPadding(new Insets(1, 4, 1, 4));
            content.getChildren().add(soldOutLabel);
        }

        // Price for available items
        if (!"soldOut".equals(status) && addedCount == 0 && itemData.price != null && itemData.price > 0) {
            String formattedPrice = formatPrice(itemData.price);
            Label priceLabel = new Label(formattedPrice);
            priceLabel.setFont(Font.font("System", 9));
            priceLabel.setTextFill(Color.web("#6b7280"));
            content.getChildren().add(priceLabel);
        }

        // Plus icon for addable items
        if (!"soldOut".equals(status)) {
            Label plusLabel = new Label("+");
            plusLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            plusLabel.setTextFill(getFamilyColor(itemData.item.getFamily()));
            content.getChildren().add(plusLabel);
        }

        chip.setGraphic(content);
        chip.setBackground(createBackground(bgColor, 4));
        chip.setBorder(createBorder(borderColor, 4));
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setCursor("soldOut".equals(status) ? Cursor.DEFAULT : Cursor.HAND);

        // Hover effect
        Color finalBgColor = bgColor;
        Color finalBorderColor = borderColor;
        Color familyColor = getFamilyColor(itemData.item.getFamily());
        chip.setOnMouseEntered(e -> {
            if (!"soldOut".equals(status)) {
                chip.setBorder(createBorder(familyColor, 4));
                chip.setBackground(createBackground(familyColor.deriveColor(0, 0.2, 1.2, 0.2), 4));
            }
        });
        chip.setOnMouseExited(e -> {
            chip.setBorder(createBorder(finalBorderColor, 4));
            chip.setBackground(createBackground(finalBgColor, 4));
        });

        // Handle click
        if (!"soldOut".equals(status)) {
            chip.setOnAction(e -> addItemToBooking(itemData));
        }

        return chip;
    }

    /**
     * Creates the legend showing status indicators.
     */
    private HBox createLegend() {
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(6, 0, 0, 0));

        // Available
        Label availLabel = new Label("(7) available");
        availLabel.setFont(Font.font("System", 9));
        availLabel.setTextFill(Color.web("#16a34a"));

        // Limited
        Label limitedLabel = new Label("(2) limited");
        limitedLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));
        limitedLabel.setTextFill(Color.web("#d97706"));

        // Sold out
        Label soldOutLabel = new Label("SOLD OUT");
        soldOutLabel.setFont(Font.font("System", FontWeight.BOLD, 7));
        soldOutLabel.setTextFill(Color.web("#dc2626"));
        soldOutLabel.setBackground(createBackground(Color.web("#fee2e2"), 2));
        soldOutLabel.setPadding(new Insets(1, 3, 1, 3));

        // Added
        HBox addedBox = new HBox(2);
        addedBox.setAlignment(Pos.CENTER);
        Label checkLabel = new Label("\u2713");
        checkLabel.setFont(Font.font("System", 8));
        checkLabel.setTextFill(Color.web("#16a34a"));
        Label addedText = new Label("added");
        addedText.setFont(Font.font("System", 9));
        addedText.setTextFill(TEXT_MUTED);
        addedBox.getChildren().addAll(checkLabel, addedText);

        legend.getChildren().addAll(availLabel, limitedLabel, soldOutLabel, addedBox);

        // Style
        Region topBorder = new Region();
        topBorder.setMinHeight(1);
        topBorder.setBackground(new Background(new BackgroundFill(BORDER, null, null)));

        VBox legendContainer = new VBox(6);
        legendContainer.getChildren().addAll(topBorder, legend);

        return legend;
    }

    /**
     * Loads available options from PolicyAggregate.
     */
    public void loadAvailableOptions() {
        if (workingBooking == null) {
            System.out.println("AddOptionPanel: WorkingBooking not set, cannot load options");
            return;
        }

        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        if (policyAggregate == null) {
            System.out.println("AddOptionPanel: PolicyAggregate not available");
            return;
        }

        List<ScheduledItem> scheduledItems = policyAggregate.getScheduledItems();
        if (scheduledItems == null || scheduledItems.isEmpty()) {
            System.out.println("AddOptionPanel: No scheduled items found");
            return;
        }

        System.out.println("AddOptionPanel: Loading from " + scheduledItems.size() + " scheduled items");

        // Extract unique ItemFamilies
        Set<ItemFamily> families = new LinkedHashSet<>();
        // Extract unique Items with availability
        Map<String, ItemWithAvailability> itemsMap = new LinkedHashMap<>();

        for (ScheduledItem si : scheduledItems) {
            Item item = si.getItem();
            if (item == null) continue;

            ItemFamily family = item.getFamily();
            if (family != null) {
                families.add(family);
            }

            // Create unique key for item+site combination
            Site site = si.getSite();
            String key = item.getPrimaryKey() + "_" + (site != null ? site.getPrimaryKey() : "null");

            // Get or create ItemWithAvailability
            ItemWithAvailability itemData = itemsMap.get(key);
            if (itemData == null) {
                // Get availability from ScheduledItem
                Integer availability = si.getGuestsAvailability();
                // Get price from rates if available
                Integer price = getItemPrice(item, site);

                itemData = new ItemWithAvailability(item, site, availability, price, si.getDate() != null);
                itemsMap.put(key, itemData);
            } else {
                // Update availability if this one is lower
                Integer newAvail = si.getGuestsAvailability();
                if (newAvail != null && (itemData.availability == null || newAvail < itemData.availability)) {
                    itemData = new ItemWithAvailability(item, itemData.site, newAvail, itemData.price, itemData.isTemporal);
                    itemsMap.put(key, itemData);
                }
            }
        }

        // Update on FX thread
        javafx.application.Platform.runLater(() -> {
            availableFamilies.setAll(families);
            availableItems.setAll(itemsMap.values());
            System.out.println("AddOptionPanel: Loaded " + families.size() + " families, " + itemsMap.size() + " items");
        });
    }

    /**
     * Gets the price for an item from PolicyAggregate rates.
     */
    private Integer getItemPrice(Item item, Site site) {
        if (workingBooking == null) return null;

        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        if (policyAggregate == null) return null;

        // Try to find a rate for this item/site
        return policyAggregate.filterDailyRatesStreamOfSiteAndItem(site, item)
            .findFirst()
            .map(Rate::getPrice)
            .orElse(null);
    }

    /**
     * Formats a price (in cents) using the event's currency.
     */
    private String formatPrice(int priceInCents) {
        Event event = workingBooking != null ? workingBooking.getEvent() : null;
        return EventPriceFormatter.formatWithCurrency(priceInCents, event);
    }

    /**
     * Gets the status of an item (available, limited, soldOut).
     */
    private String getItemStatus(ItemWithAvailability itemData) {
        if (itemData.availability == null) {
            return "available"; // No availability tracking = unlimited
        }
        if (itemData.availability <= 0) {
            return "soldOut";
        }
        if (itemData.availability <= 5) {
            return "limited";
        }
        return "available";
    }

    /**
     * Counts how many times an item is already added to the booking.
     */
    private int countItemAlreadyAdded(Item item, Site site) {
        if (item == null) return 0;

        int count = 0;
        for (DocumentLine line : existingLines) {
            if (Boolean.TRUE.equals(line.getFieldValue("removed"))) continue;
            if (Boolean.TRUE.equals(line.isCancelled())) continue;

            if (Entities.samePrimaryKey(line.getItem(), item)) {
                if (site != null) {
                    if (Entities.samePrimaryKey(line.getSite(), site)) {
                        count++;
                    }
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Adds an item to the current booking using WorkingBooking API.
     */
    private void addItemToBooking(ItemWithAvailability itemData) {
        if (workingBooking == null || itemData.item == null) return;

        System.out.println("AddOptionPanel: Adding item " + itemData.item.getName() + " to booking");

        if (itemData.isTemporal) {
            // For temporal items, book using ScheduledItems
            addTemporalItem(itemData);
        } else {
            // For non-temporal items, create a DocumentLine directly
            addNonTemporalItem(itemData);
        }

        // Notify parent
        if (onItemAdded != null) {
            onItemAdded.run();
        }

        // Refresh chips to show "added" status
        updateItemChips();
    }

    /**
     * Adds a temporal item by booking its ScheduledItems.
     */
    private void addTemporalItem(ItemWithAvailability itemData) {
        if (workingBooking == null) return;

        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        if (policyAggregate == null) return;

        // Find all ScheduledItems for this item/site
        List<ScheduledItem> scheduledItemsToBook = policyAggregate.getScheduledItems().stream()
            .filter(si -> Entities.samePrimaryKey(si.getItem(), itemData.item))
            .filter(si -> itemData.site == null || Entities.samePrimaryKey(si.getSite(), itemData.site))
            .collect(Collectors.toList());

        if (!scheduledItemsToBook.isEmpty()) {
            // Book all scheduled items for this item
            workingBooking.bookScheduledItems(scheduledItemsToBook, true);
            System.out.println("AddOptionPanel: Booked " + scheduledItemsToBook.size() + " scheduled items");
        }
    }

    /**
     * Adds a non-temporal item by creating a DocumentLine.
     */
    private void addNonTemporalItem(ItemWithAvailability itemData) {
        // Create new DocumentLine via UpdateStore
        DocumentLine newLine = updateStore.createEntity(DocumentLine.class);
        newLine.setDocument(document);
        newLine.setItem(itemData.item);
        if (itemData.site != null) {
            newLine.setSite(itemData.site);
        }

        // Add to existing lines list
        existingLines.add(newLine);

        System.out.println("AddOptionPanel: Created DocumentLine for non-temporal item");
    }

    /**
     * Gets the emoji icon for an ItemFamily.
     */
    private String getFamilyEmoji(ItemFamily family) {
        if (family == null) return "\u25CF"; // ●

        KnownItemFamily knownFamily = family.getItemFamilyType();
        if (knownFamily != null) {
            switch (knownFamily) {
                case ACCOMMODATION: return "\uD83D\uDECF\uFE0F"; // 🛏️
                case MEALS: return "\uD83C\uDF7D\uFE0F"; // 🍽️
                case DIET: return "\uD83E\uDD57"; // 🥗
                case TEACHING: return "\uD83D\uDCDA"; // 📚
                case TRANSPORT: return "\uD83D\uDE90"; // 🚐
                case PARKING: return "\uD83C\uDD7F\uFE0F"; // 🅿️
                case TAX: return "\uD83D\uDCB0"; // 💰
                case AUDIO_RECORDING: return "\uD83C\uDFA7"; // 🎧
                case VIDEO: return "\uD83C\uDFA5"; // 🎥
                default: break;
            }
        }

        // Fallback based on name
        String name = family.getName();
        if (name != null) {
            name = name.toLowerCase();
            if (name.contains("acco") || name.contains("room")) return "\uD83D\uDECF\uFE0F";
            if (name.contains("meal") || name.contains("food")) return "\uD83C\uDF7D\uFE0F";
            if (name.contains("diet")) return "\uD83E\uDD57";
            if (name.contains("teach") || name.contains("program")) return "\uD83D\uDCDA";
            if (name.contains("transp")) return "\uD83D\uDE90";
            if (name.contains("park")) return "\uD83C\uDD7F\uFE0F";
        }

        return "\u2605"; // ★
    }

    /**
     * Gets the color for an ItemFamily.
     */
    private Color getFamilyColor(ItemFamily family) {
        if (family == null) return Color.web("#6b7280");

        KnownItemFamily knownFamily = family.getItemFamilyType();
        if (knownFamily != null) {
            switch (knownFamily) {
                case ACCOMMODATION: return Color.web("#059669"); // Green
                case MEALS: return Color.web("#d97706"); // Orange
                case DIET: return Color.web("#7c3aed"); // Purple
                case TEACHING: return Color.web("#be185d"); // Pink
                case TRANSPORT: return Color.web("#0284c7"); // Blue
                case PARKING: return Color.web("#64748b"); // Slate
                case TAX: return Color.web("#dc2626"); // Red
                case AUDIO_RECORDING: return Color.web("#4f46e5"); // Indigo
                case VIDEO: return Color.web("#0891b2"); // Cyan
                default: break;
            }
        }

        return Color.web("#6b7280"); // Gray
    }

    /**
     * Sets the existing document lines (to mark items as "added").
     */
    public void setExistingLines(List<DocumentLine> lines) {
        existingLines.setAll(lines);
        updateItemChips();
    }

    /**
     * Sets the callback for when an item is added.
     */
    public void setOnItemAdded(Runnable callback) {
        this.onItemAdded = callback;
    }

    /**
     * Gets the expanded property.
     */
    public BooleanProperty expandedProperty() {
        return expandedProperty;
    }

    /**
     * Data class holding an Item with its availability and pricing info.
     */
    public static class ItemWithAvailability {
        public final Item item;
        public final Site site;
        public final Integer availability;
        public final Integer price;
        public final boolean isTemporal;

        public ItemWithAvailability(Item item, Site site, Integer availability, Integer price, boolean isTemporal) {
            this.item = item;
            this.site = site;
            this.availability = availability;
            this.price = price;
            this.isTemporal = isTemporal;
        }
    }
}
