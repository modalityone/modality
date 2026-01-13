package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.YearWeek;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.client.event.fx.FXEventId;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import java.util.HashMap;
import java.util.Map;

import static dev.webfx.stack.orm.dql.DqlStatement.fields;
import static dev.webfx.stack.orm.dql.DqlStatement.limit;
import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * The main list view component for the Registration Dashboard.
 * <p>
 * Features:
 * - Stats cards showing registration counts (Total, Confirmed, Pending, Cancelled)
 * - Search bar with text search functionality
 * - Quick filter chips for common filters
 * - Registration table using VisualGrid
 * - Row click opens the edit modal
 * <p>
 * Based on RegistrationDashboardFull.jsx (lines 1761-2180).
 *
 * @author Claude Code
 */
public class RegistrationListView {

    // Column definitions for the registration table with custom renderers
    // Exact styling from RegistrationDashboardFull.jsx
    private static final String REGISTRATION_COLUMNS = // language=JSON5
        """
            [
                {expression: 'this', label: 'Reference', renderer: 'regReference', prefWidth: 100},
                {expression: 'this', label: 'Guest', renderer: 'regGuest', prefWidth: 180},
                {expression: 'event.name', label: 'Event', renderer: 'regEvent', prefWidth: 150},
                {expression: 'this', label: 'Dates', renderer: 'regDates', prefWidth: 130},
                {expression: 'this', label: 'Status', renderer: 'regStatus', prefWidth: 100, textAlign: 'center'},
                {expression: 'this', label: 'Payment', renderer: 'regPayment', prefWidth: 120, textAlign: 'center'},
                {expression: 'this', label: 'Accommodation', renderer: 'regAccommodation', prefWidth: 150},
                {expression: 'this', label: 'Actions', renderer: 'regActions', prefWidth: 180, textAlign: 'center'},
                {expression: '!confirmed and !cancelled ? `registration-pending` : null', role: 'style'}
            ]""";

    // Fields required for the renderers
    private static final String REQUIRED_FIELDS =
        "ref,creationDate,read," +                                    // Reference column
        "person_firstName,person_lastName,person_male," +             // Guest column (name, gender)
        "person_age,person_email,person_lang," +                         // Guest column (age, email, language)
        "event.name,event.startDate,event.endDate," +                 // Event column + dates
        "dates," +                                                    // Document dates field
        "confirmed,cancelled," +                                      // Status column
        "price_deposit,price_net,price_minDeposit," +                 // Payment column
        "request";

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;

    // Stats counters (updated reactively when data loads)
    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty confirmedCountProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty pendingCountProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty cancelledCountProperty = new SimpleIntegerProperty(0);

    // UI Components
    private TextField searchField;
    private VisualGrid registrationGrid;
    private ReactiveVisualMapper<Document> masterVisualMapper;

    // Accommodation data - loaded in a single batch query for O(1) lookup per document
    // Key: document ID, Value: AccommodationInfo (type, room, systemAllocated)
    private final Map<Object, AccommodationInfo> accommodationByDocumentId = new HashMap<>();
    private final ObservableList<DocumentLine> accommodationLines = FXCollections.observableArrayList();
    private ReactiveEntitiesMapper<DocumentLine> accommodationMapper;

    // Quick filter IDs that are active
    private final String[] quickFilters = {
        "confirmed", "not-confirmed", "cancelled",
        "paid-full", "with-balance", "no-deposit",
        "arrived", "not-arrived", "unread", "with-special-needs"
    };

    public RegistrationListView(ViewDomainActivityBase activity, RegistrationPresentationModel pm) {
        this.activity = activity;
        this.pm = pm;
    }

    /**
     * Builds the complete list view UI.
     * Uses BorderPane for proper TOP_CENTER alignment with table filling available space.
     */
    public Node buildUi() {
        BorderPane container = new BorderPane();
        container.setPadding(PADDING_XLARGE);
        container.setBackground(createBackground(BG, 0));

        // Top section: Header + Stats + Search bar
        VBox topSection = new VBox(SPACING_LARGE);

        // Header with title and New Registration button
        HBox header = createHeader();

        // Stats cards row
        HBox statsRow = createStatsRow();

        // Search and filters bar
        VBox searchBar = createSearchBar();

        topSection.getChildren().addAll(header, statsRow, searchBar);

        // Center: Registration table (fills remaining space)
        Node tableSection = createTableSection();

        container.setTop(topSection);
        container.setCenter(tableSection);
        BorderPane.setAlignment(topSection, Pos.TOP_CENTER);
        BorderPane.setMargin(tableSection, new Insets(SPACING_LARGE, 0, 0, 0));

        return container;
    }

    /**
     * Creates the header with title and New Registration button.
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(SPACING_LARGE);

        // Title section
        VBox titleSection = new VBox(4);
        Label title = new Label("Registration Dashboard");
        title.setFont(FONT_TITLE);
        title.setTextFill(TEXT);

        Label subtitle = new Label("Manage event registrations, bookings, and guest communications");
        subtitle.setFont(FONT_SMALL);
        subtitle.setTextFill(TEXT_MUTED);

        titleSection.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleSection, Priority.ALWAYS);

        // New Registration button
        Button newRegButton = new Button("+ New Registration");
        applyPrimaryButtonStyle(newRegButton);
        newRegButton.setOnAction(e -> pm.openCreateModal());

        header.getChildren().addAll(titleSection, newRegButton);
        return header;
    }

    /**
     * Creates the stats cards row.
     */
    private HBox createStatsRow() {
        HBox row = new HBox(SPACING_LARGE);

        // Total registrations
        VBox totalCard = createStatsCard("TOTAL", totalCountProperty, PRIMARY, PRIMARY_LIGHT, PRIMARY_BORDER);

        // Confirmed
        VBox confirmedCard = createStatsCard("CONFIRMED", confirmedCountProperty, SUCCESS, SUCCESS_LIGHT, SUCCESS_BORDER);

        // Pending
        VBox pendingCard = createStatsCard("PENDING", pendingCountProperty, WARNING, WARNING_LIGHT, WARNING_BORDER);

        // Cancelled
        VBox cancelledCard = createStatsCard("CANCELLED", cancelledCountProperty, TEXT_MUTED, BG, BORDER);

        row.getChildren().addAll(totalCard, confirmedCard, pendingCard, cancelledCard);
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(confirmedCard, Priority.ALWAYS);
        HBox.setHgrow(pendingCard, Priority.ALWAYS);
        HBox.setHgrow(cancelledCard, Priority.ALWAYS);

        return row;
    }

    /**
     * Creates an individual stats card.
     */
    private VBox createStatsCard(String label, IntegerProperty countProperty, Color accentColor, Color bgColor, Color borderColor) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(PADDING_LARGE);
        card.setBackground(createBackground(bgColor, BORDER_RADIUS_LARGE));
        card.setBorder(createBorder(Color.color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 0.3), BORDER_RADIUS_LARGE));

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(40, 40);
        iconCircle.setMaxSize(40, 40);
        iconCircle.setBackground(createBackground(accentColor, 20)); // Circle radius
        Label iconLabel = new Label(getIconForLabel(label));
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setFont(FONT_SUBTITLE);
        iconCircle.getChildren().add(iconLabel);

        // Text section
        VBox textSection = new VBox(2);
        Label valueLabel = new Label();
        valueLabel.textProperty().bind(countProperty.asString());
        valueLabel.setFont(FONT_TITLE);
        valueLabel.setTextFill(accentColor);

        Label labelText = new Label(label);
        labelText.setFont(FONT_TINY);
        labelText.setTextFill(accentColor.darker());
        labelText.setStyle("-fx-letter-spacing: 0.5px;");

        textSection.getChildren().addAll(valueLabel, labelText);
        content.getChildren().addAll(iconCircle, textSection);
        card.getChildren().add(content);

        return card;
    }

    /**
     * Gets a simple icon character for the stats card.
     */
    private String getIconForLabel(String label) {
        return switch (label) {
            case "TOTAL" -> "\u2630"; // ☰ hamburger menu symbol for "all"
            case "CONFIRMED" -> "\u2713"; // ✓ checkmark
            case "PENDING" -> "\u231B"; // ⌛ hourglass
            case "CANCELLED" -> "\u2717"; // ✗ x mark
            default -> "?";
        };
    }

    /**
     * Creates the search and filters bar.
     */
    private VBox createSearchBar() {
        VBox searchBar = new VBox(12);
        searchBar.setPadding(new Insets(12, 16, 12, 16));
        searchBar.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        searchBar.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));

        // Row 1: Search input and filter button
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        // Search field with icon
        StackPane searchContainer = new StackPane();
        searchContainer.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by name, email, or reference...");
        applySearchFieldStyle(searchField);
        searchField.setPrefHeight(40);

        // Bind search field to presentation model
        searchField.textProperty().bindBidirectional(pm.searchTextProperty());

        // Search icon (simple text label as placeholder)
        Label searchIcon = new Label("\uD83D\uDD0D"); // 🔍
        searchIcon.setPadding(new Insets(0, 0, 0, 12));
        searchIcon.setTextFill(TEXT_MUTED);

        searchContainer.getChildren().addAll(searchField, searchIcon);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);

        // Filter button
        Button filterButton = new Button("Filters");
        applySecondaryButtonStyle(filterButton);
        filterButton.setOnAction(e -> {
            // TODO: Show filter dropdown panel
        });

        row1.getChildren().addAll(searchContainer, filterButton);

        // Row 2: Quick filter chips
        HBox row2 = createQuickFilterRow();

        searchBar.getChildren().addAll(row1, row2);
        return searchBar;
    }

    /**
     * Creates the quick filter chips row.
     */
    private HBox createQuickFilterRow() {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 0, 0));

        Label quickLabel = new Label("Quick:");
        quickLabel.setFont(FONT_TINY);
        quickLabel.setTextFill(TEXT_MUTED);
        quickLabel.setPadding(new Insets(0, 4, 0, 0));
        row.getChildren().add(quickLabel);

        // Quick filter chips
        String[][] chips = {
            {"confirmed", "Confirmed", "#198754"},
            {"not-confirmed", "Pending", "#fd7e14"},
            {"cancelled", "Cancelled", "#dc3545"},
            {"paid-full", "Paid", "#198754"},
            {"with-balance", "With balance", "#fd7e14"},
            {"unread", "Unread", "#0d6efd"},
        };

        for (String[] chip : chips) {
            Button chipBtn = createQuickFilterChip(chip[0], chip[1], Color.web(chip[2]));
            row.getChildren().add(chipBtn);
        }

        return row;
    }

    /**
     * Creates a quick filter chip button.
     */
    private Button createQuickFilterChip(String filterId, String label, Color color) {
        Button chip = new Button(label);
        chip.setFont(FONT_TINY);
        chip.setPadding(new Insets(4, 10, 4, 10));

        // Check if filter is active
        boolean isActive = pm.getActiveFilters().contains(filterId);
        updateChipStyle(chip, isActive, color);

        // Listen for filter changes
        pm.activeFiltersProperty().addListener((obs, old, newVal) -> {
            boolean active = newVal.contains(filterId);
            updateChipStyle(chip, active, color);
        });

        chip.setOnAction(e -> pm.toggleFilter(filterId));

        return chip;
    }

    /**
     * Updates the chip button style based on active state.
     */
    private void updateChipStyle(Button chip, boolean isActive, Color color) {
        if (isActive) {
            chip.setBackground(createBackground(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.15), BORDER_RADIUS_PILL));
            chip.setBorder(createBorder(color, 2, BORDER_RADIUS_PILL));
            chip.setTextFill(color);
        } else {
            chip.setBackground(createBackground(Color.TRANSPARENT, BORDER_RADIUS_PILL));
            chip.setBorder(createBorder(BORDER, BORDER_RADIUS_PILL));
            chip.setTextFill(TEXT_MUTED);
        }
    }

    /**
     * Creates the registration table section.
     * Uses ScrollPane with fixed height for scrolling through all registrations.
     */
    private Node createTableSection() {
        // Create the visual grid with table layout skin for proper sizing
        registrationGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        // Set ID for CSS styling (enables #registration selectors in CSS)
        registrationGrid.setId("registration");
        // Don't use setFullHeight as we want the grid to size to content for scrolling
        registrationGrid.setFullHeight(false);

        // Set row height for better readability (taller rows to fit multi-line content)
        registrationGrid.setMinRowHeight(60);
        registrationGrid.setPrefRowHeight(60);

        // Configure width to fill available space
        registrationGrid.setMinWidth(0);
        registrationGrid.setPrefWidth(Double.MAX_VALUE);
        registrationGrid.setMaxWidth(Double.MAX_VALUE);

        // Bind visual result to grid using proper binding (not manual listener to avoid conflicts)
        registrationGrid.visualResultProperty().bind(pm.masterVisualResultProperty());

        // Update stats when result changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            VisualResult result = pm.masterVisualResultProperty().get();
            Console.log("📋 Visual result changed: " + (result != null ? "rows=" + result.getRowCount() + ", cols=" + result.getColumnCount() : "null"));
            updateStatsCounts(result);
        }, pm.masterVisualResultProperty());

        // Handle row click to open edit modal
        FXProperties.runOnPropertyChange(() -> {
            Document selectedDoc = pm.getSelectedDocument();
            if (selectedDoc != null) {
                pm.openEditModal(selectedDoc);
            }
        }, pm.selectedDocumentProperty());

        // Wrap grid in a ScrollPane for scrolling through all registrations
        ScrollPane scrollPane = new ScrollPane(registrationGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Fixed height - will show scrollbar when content exceeds this height
        scrollPane.setPrefHeight(500);
        scrollPane.setMinHeight(300);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        // Wrap ScrollPane in a container with border and background
        VBox tableContainer = new VBox(scrollPane);
        tableContainer.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        tableContainer.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return tableContainer;
    }

    /**
     * Updates the stats counters based on the visual result.
     * Note: In production, these would be calculated from the query results or a separate stats query.
     */
    private void updateStatsCounts(VisualResult result) {
        if (result == null) {
            totalCountProperty.set(0);
            confirmedCountProperty.set(0);
            pendingCountProperty.set(0);
            cancelledCountProperty.set(0);
            return;
        }

        int total = result.getRowCount();
        totalCountProperty.set(total);

        // TODO: Calculate confirmed/pending/cancelled from actual data
        // For now, using placeholder values
        // In production, we'd either:
        // 1. Query the database for counts
        // 2. Iterate through the entities to count each status
        confirmedCountProperty.set((int) (total * 0.6)); // Placeholder
        pendingCountProperty.set((int) (total * 0.3));   // Placeholder
        cancelledCountProperty.set((int) (total * 0.1)); // Placeholder
    }

    /**
     * Sets up the reactive data mapper for the registration table.
     * This should be called from the activity's startLogic() method.
     * <p>
     * Uses optimized patterns from BookingsActivity:
     * - AbcNames for efficient name search
     * - Gantt selection filtering
     * - Quick filters that actually query the database
     */
    public ReactiveVisualMapper<Document> setupMasterVisualMapper() {
        Console.log("📋 RegistrationListView.setupMasterVisualMapper() called");

        // Register custom renderers for the table columns
        RegistrationRenderers.registerRenderers();
        RegistrationRenderers.setView(this);

        // Setup accommodation mapper FIRST - it loads data in parallel with master mapper
        setupAccommodationMapper();

        // Create and start master mapper
        masterVisualMapper = ReactiveVisualMapper.<Document>createPushReactiveChain(activity)
            // Base query with required fields for renderers
            .always("{class: 'Document', alias: 'd', orderBy: 'ref desc'}")
            .always(fields(REQUIRED_FIELDS))
            // Limit results to 100 records for performance
            .always(limit("100"))
            // Filter by organization (required)
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), orgId -> where("event.organization=?", orgId))
            // Optional event filter
            .ifNotNull(FXEventId.eventIdProperty(), eventId -> where("event=?", eventId))

            // Gantt selection filtering (like BookingsActivity)
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class, week -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week), TimeUtil.getLastDayOfWeek(week)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class, year -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year), TimeUtil.getLastDayOfYear(year)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("exists(select Attendance where documentLine.document=d and scheduledItem = ?)", si))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class, event -> where("event=?", event))

            // Optimized search filter using AbcNames (like BookingsActivity)
            .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                Character.isDigit(s.charAt(0)) ? where("ref = ?", Integer.parseInt(s.replaceAll("[^0-9]", "")))
                    : s.contains("@") ? where("lower(person_email) like ?", "%" + s.toLowerCase() + "%")
                    : where("person_abcNames like ?", AbcNames.evaluate(s, true)))

            // Quick filters - these actually query the database when active
            .ifTrue(createFilterActiveBinding("confirmed"), where("confirmed and !cancelled"))
            .ifTrue(createFilterActiveBinding("not-confirmed"), where("!confirmed and !cancelled"))
            .ifTrue(createFilterActiveBinding("cancelled"), where("cancelled"))
            .ifTrue(createFilterActiveBinding("paid-full"), where("price_deposit >= price_net and price_net > 0"))
            .ifTrue(createFilterActiveBinding("with-balance"), where("price_deposit > 0 and price_deposit < price_net"))
            .ifTrue(createFilterActiveBinding("unread"), where("!read"))

            // Set the columns for the visual grid
            .setEntityColumns(REGISTRATION_COLUMNS)
            // Explicit binding to presentation model
            .visualizeResultInto(pm.masterVisualResultProperty())
            // Note: Removed applyDomainModelRowStyle() to match JSX - cells have no backgrounds
            .autoSelectSingleRow()
            // Note: Removed setResultCacheEntry to allow re-visualization when accommodation data loads
            .addEntitiesHandler(entities -> {
                Console.log("📋 Entities received: " + (entities != null ? entities.size() : "null"));
                // Update stats counts from actual entities
                updateStatsCountsFromEntities(entities);
            })
            .start();

        Console.log("📋 Master mapper started");

        return masterVisualMapper;
    }

    /**
     * Creates a BooleanBinding that returns true when the specified filter is active.
     * This binding triggers a database query when the filter state changes.
     */
    private BooleanBinding createFilterActiveBinding(String filterId) {
        return Bindings.createBooleanBinding(
            () -> pm.getActiveFilters().contains(filterId),
            pm.activeFiltersProperty()
        );
    }

    /**
     * Updates stats counters from actual entities (not placeholder percentages).
     */
    private void updateStatsCountsFromEntities(java.util.List<Document> entities) {
        if (entities == null || entities.isEmpty()) {
            totalCountProperty.set(0);
            confirmedCountProperty.set(0);
            pendingCountProperty.set(0);
            cancelledCountProperty.set(0);
            return;
        }

        int total = entities.size();
        int confirmed = 0;
        int pending = 0;
        int cancelled = 0;

        for (Document doc : entities) {
            if (Boolean.TRUE.equals(doc.isCancelled())) {
                cancelled++;
            } else if (Boolean.TRUE.equals(doc.isConfirmed())) {
                confirmed++;
            } else {
                pending++;
            }
        }

        totalCountProperty.set(total);
        confirmedCountProperty.set(confirmed);
        pendingCountProperty.set(pending);
        cancelledCountProperty.set(cancelled);
    }

    /**
     * Sets up the batch query for accommodation data.
     * <p>
     * Performance optimization: Instead of N correlated subqueries (one per document),
     * we load ALL accommodation DocumentLines in a single query and build a HashMap.
     * This gives O(1) lookup per document during rendering.
     * <p>
     * Query loads: document.id, item.name, resourceConfiguration.name, systemAllocated
     * Filtered by: item.family.code='acco' (KnownItemFamily.ACCOMMODATION)
     */
    private void setupAccommodationMapper() {
        accommodationMapper = ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(activity)
            // Load accommodation lines with required fields - single batch query
            // Include 'document' to load the foreign entity, then access its fields
            .always("{class: 'DocumentLine', alias: 'dl', " +
                    "fields: 'document, item.name, resourceConfiguration.name, systemAllocated', " +
                    "where: '!cancelled and item.family.code=`acco`', " +
                    "orderBy: 'document.id'}")
            // Same filters as main query to ensure we only load relevant accommodation
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), orgId ->
                where("document.event.organization=?", orgId))
            .ifNotNull(FXEventId.eventIdProperty(), eventId ->
                where("document.event=?", eventId))
            // Store entities and rebuild the lookup map
            .storeEntitiesInto(accommodationLines)
            .addEntitiesHandler(lines -> {
                // Rebuild the HashMap - O(n) once, then O(1) per lookup
                accommodationByDocumentId.clear();
                if (lines != null) {
                    for (DocumentLine dl : lines) {
                        // Use primary key value for reliable HashMap lookup
                        Object docId = dl.getDocument() != null ? dl.getDocument().getPrimaryKey() : null;
                        if (docId != null && !accommodationByDocumentId.containsKey(docId)) {
                            // Take first accommodation per document (like LIMIT 1)
                            String itemName = dl.getItem() != null ? dl.getItem().getName() : null;
                            String roomName = dl.getResourceConfiguration() != null ?
                                dl.getResourceConfiguration().getName() : null;
                            // systemAllocated is not in interface, use getFieldValue
                            Object sysAlloc = dl.getFieldValue("systemAllocated");
                            accommodationByDocumentId.put(docId, new AccommodationInfo(
                                itemName, roomName, Boolean.TRUE.equals(sysAlloc)));
                        }
                    }
                }
                Console.log("📋 Accommodation data loaded: " + accommodationByDocumentId.size() + " documents");
                // Force master mapper to re-query and re-visualize
                // Now that cache is disabled, stop/start will trigger fresh query and cell rendering
                if (masterVisualMapper != null) {
                    Console.log("📋 Forcing master mapper re-query");
                    masterVisualMapper.stop();
                    masterVisualMapper.start();
                }
            })
            .start();
    }

    /**
     * Gets the master visual mapper.
     */
    public ReactiveVisualMapper<Document> getMasterVisualMapper() {
        return masterVisualMapper;
    }

    /**
     * Gets the search field for focus activation on resume.
     */
    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Refreshes the data when the activity becomes active.
     */
    public void refreshWhenActive() {
        if (masterVisualMapper != null) {
            masterVisualMapper.refreshWhenActive();
        }
    }

    /**
     * Opens the edit modal for the given document.
     */
    public void openEditModal(Document document) {
        if (document != null) {
            Console.log("📋 Opening edit modal for document: " + document.getRef());
            pm.openEditModal(document);
            RegistrationEditModal modal = new RegistrationEditModal(activity, pm, document);
            modal.show();
        }
    }

    /**
     * Gets accommodation info for a document by its primary key.
     * O(1) lookup from the pre-loaded batch data.
     */
    public AccommodationInfo getAccommodationInfo(Object primaryKey) {
        return accommodationByDocumentId.get(primaryKey);
    }

    /**
     * Accommodation information for a document.
     * Loaded in a single batch query for efficiency.
     */
    public record AccommodationInfo(String itemName, String roomName, boolean systemAllocated) {}
}
