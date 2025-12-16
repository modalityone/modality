package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
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

import java.util.*;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Add Option Panel for the BookingTab.
 * <p>
 * Displays available booking options grouped by category (Accommodation, Meals, Diet, Program, Transport, Parking).
 * Each item chip shows availability status: available, limited, sold out, already added, or not at event.
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
    private final StringProperty selectedCategoryProperty = new SimpleStringProperty("accommodation");

    // Data - using generic Entity since Option is in kbsx module
    private final ObservableList<OptionWithAvailability> allOptions = FXCollections.observableArrayList();
    private final ObservableList<DocumentLine> existingLines = FXCollections.observableArrayList();

    // Cached availability data
    private Map<String, Integer> availabilityCache = new HashMap<>();

    // Callbacks
    private Runnable onItemAdded;

    // Category definitions
    private static final String[] CATEGORIES = {"accommodation", "meals", "diet", "program", "transport", "parking"};
    private static final String[] CATEGORY_LABELS = {"Accommodation", "Meals", "Diet", "Program", "Transport", "Parking"};
    private static final String[] CATEGORY_EMOJIS = {"🛏️", "🍽️", "🥗", "📚", "🚗", "🅿️"};

    public AddOptionPanel(ViewDomainActivityBase activity, RegistrationPresentationModel pm,
                          Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
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

        Label chevron = new Label("▼");
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
     * Creates the expanded panel content with category tabs and item chips.
     */
    private VBox createExpandedContent() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        panel.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Header with title and collapse button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Add Option");
        title.setFont(FONT_SUBTITLE);
        title.setTextFill(TEXT);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button collapseBtn = new Button("▲");
        collapseBtn.setFont(Font.font(10));
        collapseBtn.setBackground(Background.EMPTY);
        collapseBtn.setBorder(Border.EMPTY);
        collapseBtn.setCursor(Cursor.HAND);
        collapseBtn.setOnAction(e -> expandedProperty.set(false));

        header.getChildren().addAll(title, collapseBtn);

        // Category tabs
        HBox categoryTabs = createCategoryTabs();

        // Item chips container (will be populated based on selected category)
        FlowPane itemChips = new FlowPane();
        itemChips.setHgap(8);
        itemChips.setVgap(8);
        itemChips.setPadding(new Insets(8, 0, 0, 0));

        // Update item chips when category changes or options load
        FXProperties.runNowAndOnPropertiesChange(() -> {
            updateItemChips(itemChips);
        }, selectedCategoryProperty);

        // Also update when options list changes
        allOptions.addListener((javafx.collections.ListChangeListener<OptionWithAvailability>) c -> {
            updateItemChips(itemChips);
        });

        panel.getChildren().addAll(header, categoryTabs, itemChips);

        return panel;
    }

    /**
     * Creates the category tab chips.
     */
    private HBox createCategoryTabs() {
        HBox tabs = new HBox(6);
        tabs.setPadding(new Insets(0, 0, 8, 0));

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];
            String emoji = CATEGORY_EMOJIS[i];
            String label = CATEGORY_LABELS[i];

            Button chip = new Button(emoji + " " + label);
            chip.setFont(Font.font(12));
            chip.setPadding(new Insets(6, 12, 6, 12));
            chip.setCursor(Cursor.HAND);

            // Style based on selection
            final int index = i;
            FXProperties.runNowAndOnPropertiesChange(() -> {
                boolean selected = selectedCategoryProperty.get().equals(category);
                if (selected) {
                    chip.setBackground(createBackground(getCategoryBgColor(category), 8));
                    chip.setTextFill(getCategoryTextColor(category));
                    chip.setBorder(createBorder(getCategoryIconColor(category), 8));
                } else {
                    chip.setBackground(createBackground(Color.WHITE, 8));
                    chip.setTextFill(TEXT_MUTED);
                    chip.setBorder(createBorder(Color.web("#e5e7eb"), 8));
                }
            }, selectedCategoryProperty);

            chip.setOnAction(e -> selectedCategoryProperty.set(category));
            tabs.getChildren().add(chip);
        }

        return tabs;
    }

    /**
     * Updates the item chips based on the selected category.
     */
    private void updateItemChips(FlowPane container) {
        container.getChildren().clear();

        String selectedCategory = selectedCategoryProperty.get();

        // Filter options by category
        List<OptionWithAvailability> categoryOptions = allOptions.stream()
            .filter(opt -> matchesCategory(opt.option, selectedCategory))
            .sorted(Comparator.comparingInt(opt -> {
                Integer ord = Numbers.toInteger(opt.option.getFieldValue("ord"));
                return ord != null ? ord : 999;
            }))
            .collect(java.util.stream.Collectors.toList());

        if (categoryOptions.isEmpty()) {
            Label emptyLabel = new Label("No " + selectedCategory + " options available for this event");
            emptyLabel.setFont(FONT_SMALL);
            emptyLabel.setTextFill(TEXT_MUTED);
            emptyLabel.setPadding(new Insets(16));
            container.getChildren().add(emptyLabel);
            return;
        }

        for (OptionWithAvailability opt : categoryOptions) {
            Node chip = createItemChip(opt);
            container.getChildren().add(chip);
        }
    }

    /**
     * Creates an item chip with availability status.
     */
    private Node createItemChip(OptionWithAvailability opt) {
        Button chip = new Button();
        HBox content = new HBox(6);
        content.setAlignment(Pos.CENTER);

        // Determine availability status
        String status = getAvailabilityStatus(opt);
        String icon;
        Color bgColor, textColor;

        switch (status) {
            case "added":
                icon = "✓";
                bgColor = GREEN_LIGHT;
                textColor = GREEN;
                break;
            case "limited":
                icon = "⚠";
                bgColor = WARNING_BG;
                textColor = Color.web("#d97706");
                break;
            case "soldOut":
                icon = "✗";
                bgColor = RED_LIGHT;
                textColor = RED;
                break;
            case "notAtEvent":
                icon = "—";
                bgColor = Color.web("#f3f4f6");
                textColor = TEXT_MUTED;
                chip.setDisable(true);
                break;
            default: // available
                icon = "+";
                bgColor = Color.WHITE;
                textColor = TEXT;
        }

        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(textColor);

        // Get item name from option - try item.name first, then option.name
        Entity item = opt.option.getForeignEntity("item");
        String itemName = item != null ? item.getStringFieldValue("name") : opt.option.getStringFieldValue("name");
        Label nameLabel = new Label(itemName != null ? itemName : "Unknown Item");
        nameLabel.setTextFill(textColor);

        content.getChildren().addAll(iconLabel, nameLabel);

        // Add availability count badge for limited items
        if ("limited".equals(status) && opt.available != null && opt.available > 0) {
            Label countBadge = new Label(String.valueOf(opt.available));
            countBadge.setFont(Font.font(10));
            countBadge.setTextFill(Color.WHITE);
            countBadge.setBackground(createBackground(Color.web("#d97706"), 10));
            countBadge.setPadding(new Insets(1, 5, 1, 5));
            content.getChildren().add(countBadge);
        }

        chip.setGraphic(content);
        chip.setBackground(createBackground(bgColor, 8));
        chip.setBorder(createBorder(bgColor.darker(), 8));
        chip.setPadding(new Insets(6, 12, 6, 12));
        chip.setFont(Font.font(12));
        chip.setCursor("added".equals(status) || "notAtEvent".equals(status) ? Cursor.DEFAULT : Cursor.HAND);

        // Handle click to add item
        if (!"added".equals(status) && !"notAtEvent".equals(status)) {
            chip.setOnAction(e -> {
                if ("soldOut".equals(status)) {
                    // Show sold out confirmation dialog
                    showSoldOutConfirmation(opt, () -> addOptionToBooking(opt));
                } else {
                    addOptionToBooking(opt);
                }
            });
        }

        return chip;
    }

    /**
     * Determines the availability status for an option.
     */
    private String getAvailabilityStatus(OptionWithAvailability opt) {
        // Check if already added to this booking
        if (isOptionAlreadyAdded(opt.option)) {
            return "added";
        }

        // Check force sold out flag using generic field access
        Boolean forceSoldout = opt.option.getBooleanFieldValue("forceSoldout");
        if (Boolean.TRUE.equals(forceSoldout)) {
            return "soldOut";
        }

        // Check availability
        if (opt.available == null) {
            // No availability data - assume available (or not at event for some cases)
            return "available";
        }

        if (opt.available <= 0) {
            return "soldOut";
        }

        // Limited threshold (configurable, default 5)
        int limitedThreshold = 5;
        if (opt.available <= limitedThreshold) {
            return "limited";
        }

        return "available";
    }

    /**
     * Checks if an option is already added to the current booking.
     */
    private boolean isOptionAlreadyAdded(Entity option) {
        if (option == null) return false;

        Entity item = option.getForeignEntity("item");
        if (item == null) return false;

        Object itemId = item.getId();
        Entity site = option.getForeignEntity("site");
        Object siteId = site != null ? site.getId() : null;

        for (DocumentLine line : existingLines) {
            if (line.getItem() != null && line.getItem().getId().equals(itemId)) {
                // For site-specific items, also check site matches
                if (siteId != null && line.getSite() != null) {
                    if (line.getSite().getId().equals(siteId)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Matches an option to a category based on its item family.
     */
    private boolean matchesCategory(Entity option, String category) {
        if (option == null) return false;

        // First check item family from option (item_family field or item.family)
        Entity family = option.getForeignEntity("item_family");
        if (family == null) {
            Entity item = option.getForeignEntity("item");
            if (item != null) {
                family = item.getForeignEntity("family");
            }
        }

        if (family == null) return false;

        String familyName = family.getStringFieldValue("name");
        String familyCode = family.getStringFieldValue("code");

        if (familyName == null && familyCode == null) return false;

        String searchTerm = (familyName != null ? familyName : familyCode).toLowerCase();

        return switch (category.toLowerCase()) {
            case "accommodation" -> searchTerm.contains("accommodation") || searchTerm.contains("room") ||
                searchTerm.contains("bed") || searchTerm.contains("lodging");
            case "meals" -> searchTerm.contains("meal") || searchTerm.contains("breakfast") ||
                searchTerm.contains("lunch") || searchTerm.contains("dinner") || searchTerm.contains("food");
            case "diet" -> searchTerm.contains("diet") || searchTerm.contains("vegetarian") ||
                searchTerm.contains("vegan") || searchTerm.contains("special");
            case "program" -> searchTerm.contains("program") || searchTerm.contains("teaching") ||
                searchTerm.contains("course") || searchTerm.contains("class") || searchTerm.contains("session");
            case "transport" -> searchTerm.contains("transport") || searchTerm.contains("shuttle") ||
                searchTerm.contains("bus") || searchTerm.contains("transfer") || searchTerm.contains("pickup");
            case "parking" -> searchTerm.contains("parking") || searchTerm.contains("car");
            default -> false;
        };
    }

    /**
     * Loads available options from the database.
     * Uses direct DSQL query for options and native SQL for availability.
     */
    public void loadAvailableOptions() {
        if (document == null || document.getEvent() == null) return;

        Object eventId = document.getEvent().getId();
        EntityStore store = document.getStore();

        // First load availability data, then load options
        loadAvailabilityFromDatabase(eventId, () -> {
            // Then load options via DSQL
            String optionsQuery = """
                select site,item,item.family,item.family.name,item.family.code,item.name,name,
                       forceSoldout,online,folder,ord,parent
                from Option
                where event=? and online=true
                order by ord
                """;

            store.<Entity>executeQuery(optionsQuery, eventId)
                .onFailure(error -> System.err.println("Failed to load event options: " + error.getMessage()))
                .onSuccess(options -> {
                    updateOptionsWithAvailability(options, availabilityCache);
                });
        });
    }

    /**
     * Loads availability data from the database using native SQL.
     * Uses the resource_availability_by_event_items() SQL function.
     */
    private void loadAvailabilityFromDatabase(Object eventId, Runnable onComplete) {
        // SQL query based on EventAggregateImpl.onEventAvailabilities()
        // Simplified version that groups by site/item and returns minimum availability
        String availabilitySql = """
            with ra as (select * from resource_availability_by_event_items($1) where max > 0)
            select min(site_id) as site, min(item_id) as item, min(max - current) as available
            from ra
            group by site_id, item_id
            order by site, item
            """;

        QueryService.executeQuery(QueryArgument.builder()
                .setStatement(availabilitySql)
                .setParameters(eventId)
                .setDataSourceId(activity.getDataSourceId())
                .build())
            .onFailure(error -> {
                System.err.println("Failed to load availability: " + error.getMessage());
                // Continue without availability data
                availabilityCache.clear();
                onComplete.run();
            })
            .onSuccess(queryResult -> {
                availabilityCache = parseAvailabilityResult(queryResult);
                onComplete.run();
            });
    }

    /**
     * Parses the availability QueryResult into a map keyed by "siteId_itemId".
     * The QueryResult columns are: site(0), item(1), available(2)
     */
    private Map<String, Integer> parseAvailabilityResult(QueryResult queryResult) {
        Map<String, Integer> availabilityMap = new HashMap<>();

        if (queryResult == null) return availabilityMap;

        int rowCount = queryResult.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            // QueryResult columns: site(0), item(1), available(2)
            Object siteId = queryResult.getValue(row, 0);
            Object itemId = queryResult.getValue(row, 1);
            Object availableObj = queryResult.getValue(row, 2);

            if (siteId != null && itemId != null) {
                String key = siteId + "_" + itemId;
                Integer available = Numbers.toInteger(availableObj);

                // Keep the minimum availability across all entries
                Integer existing = availabilityMap.get(key);
                if (existing == null || (available != null && available < existing)) {
                    availabilityMap.put(key, available);
                }
            }
        }

        return availabilityMap;
    }

    /**
     * Updates the options list with availability data.
     * Only includes concrete options (not folders) that have site and item.
     */
    private void updateOptionsWithAvailability(EntityList<Entity> options, Map<String, Integer> availabilityMap) {
        List<OptionWithAvailability> optionsList = new ArrayList<>();

        for (Entity option : options) {
            // Only include concrete options (not folders, has site and item)
            Boolean isFolder = option.getBooleanFieldValue("folder");
            if (Boolean.TRUE.equals(isFolder)) {
                continue;
            }

            // Get site and item references
            Entity site = option.getForeignEntity("site");
            Entity item = option.getForeignEntity("item");

            // Skip if no item (not a concrete option)
            if (item == null) {
                continue;
            }

            Integer available = null;

            if (availabilityMap != null && site != null) {
                String key = site.getId() + "_" + item.getId();
                available = availabilityMap.get(key);

                // Also try with just the item ID if site-specific lookup fails
                if (available == null) {
                    // Try to find any availability for this item across all sites
                    for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
                        if (entry.getKey().endsWith("_" + item.getId())) {
                            Integer itemAvailable = entry.getValue();
                            if (available == null || (itemAvailable != null && itemAvailable < available)) {
                                available = itemAvailable;
                            }
                        }
                    }
                }
            }

            optionsList.add(new OptionWithAvailability(option, available));
        }

        // Update on JavaFX thread
        javafx.application.Platform.runLater(() -> {
            allOptions.setAll(optionsList);
        });
    }

    /**
     * Adds an option to the current booking.
     */
    private void addOptionToBooking(OptionWithAvailability opt) {
        if (opt.option == null) return;

        Entity item = opt.option.getForeignEntity("item");
        if (item == null) return;

        Entity site = opt.option.getForeignEntity("site");

        // Create new DocumentLine
        DocumentLine newLine = updateStore.createEntity(DocumentLine.class);
        newLine.setDocument(document);
        newLine.setItem((Item) item);
        if (site != null) {
            newLine.setSite((Site) site);
        }

        // Set dates from document or event
        if (document.getEvent() != null) {
            Event event = document.getEvent();
            // TODO: Set appropriate dates based on option type and event dates
        }

        // Add to existing lines
        existingLines.add(newLine);

        // Notify parent
        if (onItemAdded != null) {
            onItemAdded.run();
        }
    }

    /**
     * Shows a confirmation dialog for adding a sold-out item.
     */
    private void showSoldOutConfirmation(OptionWithAvailability opt, Runnable onConfirm) {
        // TODO: Implement confirmation dialog
        // For now, just add the item
        onConfirm.run();
    }

    /**
     * Sets the existing document lines (to mark items as "added").
     */
    public void setExistingLines(List<DocumentLine> lines) {
        existingLines.setAll(lines);
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
     * Helper record to hold an option (Entity) with its availability.
     * Using Entity instead of Option since Option is defined in kbsx module.
     */
    public record OptionWithAvailability(Entity option, Integer available) {}
}
