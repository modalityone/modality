package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.visual.*;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.MediaConsumption;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.backoffice.activities.medias.MediaDashboardPresentationModel.ConsumptionTypeFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Media Dashboard tab view for monitoring video streaming consumption in backoffice.
 * Displays current livestream viewers and session-based viewing statistics.
 *
 * @author Claude Code
 */
final class MediaDashboardTabView {

    static {
        // Register custom renderers for media consumption display
        MediaConsumptionRenderers.registerRenderers();
    }

    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final VBox container;
    private final ScrollPane scrollPane;
    private final MediaDashboardPresentationModel pm = new MediaDashboardPresentationModel();
    private TextField searchTextField;

    // Data feeds
    private final ObservableList<MediaConsumption> selectedSessionViewers = FXCollections.observableArrayList();

    // Grouped data for display (one row per user)
    private final ObservableList<UserConsumptionData> displayedSessionViewers = FXCollections.observableArrayList();

    // UI Components
    private Label eventTitleLabel;
    private EntityButtonSelector<ScheduledItem> sessionSelector;
    private Button sessionButton;
    private HBox sessionSelectorRow;
    private Label totalViewersLabel;
    private Label livestreamCountLabel;
    private Label recordedCountLabel;
    private VisualGrid sessionViewersGrid;

    private EntityStore entityStore;
    private DataSourceModel dataSourceModel;

    // Reactive mapper for push-based updates
    private ReactiveEntitiesMapper<MediaConsumption> sessionViewersMapper;

    MediaDashboardTabView() {
        this.container = new VBox();
        this.container.setSpacing(20);
        this.container.setFillWidth(true);
        this.container.setMaxWidth(1200);
        this.scrollPane = Controls.createVerticalScrollPaneWithPadding(10, container);
        buildUI();
    }

    Node buildContainer() {
        return scrollPane;
    }

    void startLogic(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        this.entityStore = EntityStore.create(dataSourceModel);

        // Listen to event changes to update session selector
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                updateSessionSelector(event);
            }
        }, FXEvent.eventProperty());

        // Set up grid update listener (following OperationsView pattern)
        Runnable updateSessionViewersGrid = this::updateSessionViewersGrid;
        ObservableLists.runNowAndOnListChange(change -> updateSessionViewersGrid.run(), displayedSessionViewers);
    }

    void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            onResume();
        }
        // No need to manually set active on the mappers since they are bound to activeProperty
    }

    void onResume() {
        // Set focus on search field when tab becomes active
        if (searchTextField != null && searchTextField.getScene() != null) {
            searchTextField.requestFocus();
        }
    }

    private void buildUI() {
        // Create centered header with title and SVG icon (like LiveStreamingTabView)
        BorderPane headerFrame = new BorderPane();
        headerFrame.setPadding(new Insets(0, 0, 20, 0));

        Label title = I18nControls.newLabel(MediasI18nKeys.MediaDashboardTitle);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        headerFrame.setTop(title);

        // Session Details Section
        VBox sessionDetailsSection = createSessionDetailsSection();

        container.getChildren().addAll(headerFrame, sessionDetailsSection);
    }

    private HBox createSectionTitle(Object i18nKey, SVGPath icon) {
        HBox titleBox = new HBox();
        titleBox.setSpacing(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getStyleClass().add("section-title");

        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(20, 20);
        iconContainer.setPrefSize(20, 20);
        iconContainer.setMaxSize(20, 20);
        icon.getStyleClass().add("section-title-icon");
        iconContainer.getChildren().add(icon);

        Label titleLabel = I18nControls.newLabel(i18nKey);
        titleLabel.getStyleClass().add("section-title-text");

        titleBox.getChildren().addAll(iconContainer, titleLabel);
        return titleBox;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        // Create search field with icon
        searchTextField = new TextField();
        searchTextField.setPromptText("Search by name or email...");
        searchTextField.setPrefWidth(300);
        searchTextField.setMaxWidth(400);
        searchTextField.setPadding(new Insets(8, 35, 8, 12));

        // Bind search text to presentation model
        pm.searchTextProperty().bind(searchTextField.textProperty());

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchTextField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        searchBar.getChildren().add(searchContainer);

        // Add filter buttons
        HBox filterButtons = createTypeFilterButtons();
        searchBar.getChildren().add(filterButtons);

        return searchBar;
    }

    private HBox createTypeFilterButtons() {
        HBox filterBox = new HBox(8);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 0, 10));

        Label filterLabel = I18nControls.newLabel(MediasI18nKeys.Type);
        filterLabel.getStyleClass().add("filter-label");

        // Create toggle buttons for each filter type
        Button allButton = createFilterButton(MediasI18nKeys.All, ConsumptionTypeFilter.ALL);
        Button liveButton = createFilterButton(MediasI18nKeys.Live, ConsumptionTypeFilter.LIVE);
        Button recordingButton = createFilterButton(MediasI18nKeys.Recording, ConsumptionTypeFilter.RECORDING);
        Button bothButton = createFilterButton(MediasI18nKeys.Both, ConsumptionTypeFilter.BOTH);

        // Set the initial active button
        updateFilterButtonStyle(allButton, true);

        filterBox.getChildren().addAll(filterLabel, allButton, liveButton, recordingButton, bothButton);
        return filterBox;
    }

    private Button createFilterButton(Object i18nKey, ConsumptionTypeFilter filterType) {
        Button button = I18nControls.newButton(i18nKey);
        button.getStyleClass().addAll("filter-button");
        button.setMinWidth(75);
        button.setPadding(new Insets(5, 10, 5, 10));

        // Apply inactive style by default
        updateFilterButtonStyle(button, false);

        button.setOnAction(e -> {
            // Update the filter in the presentation model
            pm.setTypeFilter(filterType);

            // Update button styles - all buttons should be inactive except this one
            ((HBox)button.getParent()).getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button)node)
                .forEach(btn -> updateFilterButtonStyle(btn, btn == button));
        });

        return button;
    }

    private void updateFilterButtonStyle(Button button, boolean isActive) {
        if (isActive) {
            button.getStyleClass().removeAll("filter-button-inactive");
            button.getStyleClass().add("filter-button-active");
        } else {
            button.getStyleClass().removeAll("filter-button-active");
            button.getStyleClass().add("filter-button-inactive");
        }
    }

    private VBox createSessionDetailsSection() {
        VBox section = new VBox();
        section.setSpacing(20);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("session-details-section");
        section.setMinHeight(350);

        // Section Title with event name
        VBox titleAndEventBox = new VBox(10);

        HBox sectionTitle = createSectionTitle(MediasI18nKeys.SessionDetails, SvgIcons.createCalendarSVGPath());

        // Event name - more discrete
        eventTitleLabel = new Label();
        eventTitleLabel.getStyleClass().add("event-subtitle");

        // Bind to event property
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                String eventName = event.getName() != null ? event.getName() : "Unknown Event";
                eventTitleLabel.setText(eventName);
            }
        }, FXEvent.eventProperty());

        titleAndEventBox.getChildren().addAll(sectionTitle, eventTitleLabel);

        // Session Selector
        HBox sessionSelectorRow = createSessionSelectorRow();

        // Stats Row
        HBox statsRow = createSessionStatsRow();

        // Viewer Details Title Row with Search Bar on the right
        HBox viewerDetailsTitleRow = new HBox();
        viewerDetailsTitleRow.setAlignment(Pos.CENTER_LEFT);
        viewerDetailsTitleRow.setPadding(new Insets(15, 0, 10, 0));

        HBox viewerDetailsTitle = createSectionTitle(MediasI18nKeys.ViewerDetails, SvgIcons.createUsersSVGPath());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search Bar (without padding as it's now inline)
        HBox searchBar = createSearchBar();

        viewerDetailsTitleRow.getChildren().addAll(viewerDetailsTitle, spacer, searchBar);

        // Viewer Details Grid
        sessionViewersGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        sessionViewersGrid.setMinRowHeight(38);
        sessionViewersGrid.setPrefRowHeight(38);
        sessionViewersGrid.setPrefHeight(350);
        sessionViewersGrid.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(sessionViewersGrid, Priority.ALWAYS);

        section.getChildren().addAll(titleAndEventBox, sessionSelectorRow, statsRow, viewerDetailsTitleRow, sessionViewersGrid);
        return section;
    }

    private HBox createSessionSelectorRow() {
        sessionSelectorRow = new HBox();
        sessionSelectorRow.setSpacing(12);
        sessionSelectorRow.setAlignment(Pos.CENTER_LEFT);

        Label sessionLabel = I18nControls.newLabel(MediasI18nKeys.SelectSession);
        sessionLabel.getStyleClass().add("session-selector-label");

        // Create placeholder button (will be replaced when event loads)
        sessionButton = new Button();
        sessionButton.setText(I18n.getI18nText(MediasI18nKeys.SelectSession));
        sessionButton.setPrefWidth(400);
        sessionButton.setMaxWidth(Double.MAX_VALUE);
        sessionButton.setDisable(true);
        HBox.setHgrow(sessionButton, Priority.ALWAYS);

        sessionSelectorRow.getChildren().addAll(sessionLabel, sessionButton);
        return sessionSelectorRow;
    }

    private HBox createSessionStatsRow() {
        HBox statsRow = new HBox();
        statsRow.setSpacing(12);
        statsRow.setAlignment(Pos.CENTER);

        // Total Viewers Box
        totalViewersLabel = new Label("0");
        VBox totalBox = createStatBox(MediasI18nKeys.TotalViewers, totalViewersLabel, "#9b59b6");

        // Livestream Box
        livestreamCountLabel = new Label("0");
        VBox livestreamBox = createStatBox(MediasI18nKeys.Livestream, livestreamCountLabel, "#e74c3c");

        // Recordings Box
        recordedCountLabel = new Label("0");
        VBox recordedBox = createStatBox(MediasI18nKeys.Recordings, recordedCountLabel, "#2ecc71");

        statsRow.getChildren().addAll(totalBox, livestreamBox, recordedBox);
        return statsRow;
    }

    private VBox createStatBox(Object i18nKey, Label valueLabel, String color) {
        VBox box = new VBox();
        box.setSpacing(6);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stat-box");
        // Add color-specific class based on the color parameter
        if (color.equals("#9b59b6")) {
            box.getStyleClass().add("stat-box-purple");
        } else if (color.equals("#e74c3c")) {
            box.getStyleClass().add("stat-box-red");
        } else if (color.equals("#2ecc71")) {
            box.getStyleClass().add("stat-box-green");
        }
        box.setMinHeight(90);
        HBox.setHgrow(box, Priority.ALWAYS);

        Label statLabel = I18nControls.newLabel(i18nKey);
        statLabel.getStyleClass().add("stat-box-label");

        valueLabel.getStyleClass().add("stat-box-value");

        box.getChildren().addAll(statLabel, valueLabel);
        return box;
    }

    /**
     * Updates the session selector when the event changes.
     * Creates a new EntityButtonSelector configured for the current event.
     */
    private void updateSessionSelector(Event event) {
        // Remove old button if exists
        if (sessionButton != null) {
            sessionSelectorRow.getChildren().remove(sessionButton);
        }

        // Create button selector parameters with button factory
        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        // Create new selector for this event
        sessionSelector = createSessionButtonSelector(event, dataSourceModel, buttonSelectorParameters);
        sessionButton = sessionSelector.getButton();
        sessionButton.setPrefWidth(400);
        sessionButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(sessionButton, Priority.ALWAYS);

        // Update button text when selection changes
        sessionSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sessionButton.setText(formatSessionDisplay(newVal));
                loadSelectedSessionViewers(newVal);
            } else {
                sessionButton.setText(I18n.getI18nText(MediasI18nKeys.SelectSession));
            }
        });

        // Set initial text
        sessionButton.setText(I18n.getI18nText(MediasI18nKeys.SelectSession));

        // Add to row (after the label)
        sessionSelectorRow.getChildren().add(sessionButton);
    }

    /**
     * Creates an EntityButtonSelector for ScheduledItem entities filtered by event.
     */
    private EntityButtonSelector<ScheduledItem> createSessionButtonSelector(
        Event event,
        DataSourceModel dataSourceModel,
        ButtonSelectorParameters buttonSelectorParameters
    ) {
        Object eventId = event.getPrimaryKey();
        String itemCode = KnownItem.VIDEO.getCode();
        String familyCode = KnownItemFamily.TEACHING.getCode();

        // Build JSON query for video sessions
        // Include all fields needed for formatSessionDisplay() and getSessionTimeRange()
        String sessionJson = // language=JSON5
            "{class: 'ScheduledItem', alias: 'si', " +
            "where: 'programScheduledItem.event=" + eventId + " and item.code=\"" + itemCode + "\" and programScheduledItem.item.family.code=\"" + familyCode + "\"', " +
            "orderBy: 'date', " +
            "columns: 'name,programScheduledItem.(name,startTime,endTime,timeline.(name,startTime,endTime)),date'}";

        return new EntityButtonSelector<>(sessionJson, dataSourceModel, buttonSelectorParameters);
    }


    private String formatSessionDisplay(ScheduledItem session) {
        // Get session name - try multiple sources depending on event type
        String name = session.getName();

        // Try via typed getter method
        ScheduledItem programScheduledItem = session.getProgramScheduledItem();

        // Try programScheduledItem.name via the entity reference
        if ((name == null || name.isEmpty()) && programScheduledItem != null) {
            name = programScheduledItem.getName();
        }

        // If still null, try programScheduledItem.timeline.name (for festival events)
        if ((name == null || name.isEmpty()) && programScheduledItem != null) {
            Object timeline = programScheduledItem.getFieldValue("timeline");
            if (timeline instanceof Entity) {
                name = (String) ((Entity) timeline).getFieldValue("name");
            }
        }

        // Fallback to dot notation access
        if (name == null || name.isEmpty()) {
            name = (String) session.getFieldValue("programScheduledItem.name");
        }

        if (name == null || name.isEmpty()) {
            name = (String) session.getFieldValue("programScheduledItem.timeline.name");
        }

        if (name == null || name.isEmpty()) {
            name = "Unnamed Session";
        }

        // Format date
        String date = "";
        LocalDate sessionDate = session.getDate();
        if (sessionDate != null) {
            date = sessionDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }

        // Get start and end times
        String timeRange = getSessionTimeRange(session);

        // Build display string: "Name - Date (HH:mm - HH:mm)"
        StringBuilder display = new StringBuilder(name);
        if (!date.isEmpty()) {
            display.append(" - ").append(date);
        }
        if (!timeRange.isEmpty()) {
            display.append(" (").append(timeRange).append(")");
        }

        return display.toString();
    }

    private String getSessionTimeRange(ScheduledItem session) {
        // Access times using dot notation as loaded by the query (same as VideoTabView line 279)
        // For some event types, times are on the timeline; for others, directly on programScheduledItem

        // Try timeline times first (programScheduledItem.timeline.startTime/endTime)
        LocalTime startTime = (LocalTime) session.getFieldValue("programScheduledItem.timeline.startTime");
        LocalTime endTime = (LocalTime) session.getFieldValue("programScheduledItem.timeline.endTime");

        // Fallback to programScheduledItem times if timeline times are null
        // These were loaded via programScheduledItem.(startTime, endTime) in the query
        if (startTime == null || endTime == null) {
            Object programScheduledItem = session.getFieldValue("programScheduledItem");
            if (programScheduledItem instanceof Entity) {
                Entity psi = (Entity) programScheduledItem;
                if (startTime == null) {
                    startTime = (LocalTime) psi.getFieldValue("startTime");
                }
                if (endTime == null) {
                    endTime = (LocalTime) psi.getFieldValue("endTime");
                }
            }
        }

        if (startTime != null && endTime != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return startTime.format(timeFormatter) + " - " + endTime.format(timeFormatter);
        }

        return "";
    }


    private void loadSelectedSessionViewers(ScheduledItem session) {
        // Stop previous mapper if exists
        if (sessionViewersMapper != null) {
            sessionViewersMapper.stop();
        }

        if (session == null) {
            selectedSessionViewers.clear();
            displayedSessionViewers.clear();
            updateSessionStats(new ArrayList<>());
            return;
        }

        // Create reactive mapper with push updates for session viewers
        sessionViewersMapper = ReactiveEntitiesMapper.<MediaConsumption>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'MediaConsumption', alias: 'mc', fields: 'date, durationMillis, livestreamed, attendance.documentLine.document.person, attendance.documentLine.document.person.firstName, attendance.documentLine.document.person.lastName, attendance.documentLine.document.person.email', where: 'scheduledItem=" + session.getPrimaryKey() + "', orderBy: 'date desc'}")
            .storeEntitiesInto(selectedSessionViewers)
            .start();

        // React to changes in the viewers list
        ObservableLists.runNowAndOnListChange(change -> {
            // Group by user and update displayed list
            List<UserConsumptionData> groupedData = groupConsumptionsByUser(selectedSessionViewers);

            // Update stats
            updateSessionStats(groupedData);

            // Apply search filter and update displayed list
            applySearchFilter(groupedData);
        }, selectedSessionViewers);

        // Also react to search text changes
        pm.searchTextProperty().addListener((obs, oldVal, newVal) -> {
            // Re-filter the current data
            List<UserConsumptionData> groupedData = groupConsumptionsByUser(selectedSessionViewers);
            applySearchFilter(groupedData);
        });

        // React to type filter changes
        pm.typeFilterProperty().addListener((obs, oldVal, newVal) -> {
            // Re-filter the current data
            List<UserConsumptionData> groupedData = groupConsumptionsByUser(selectedSessionViewers);
            applySearchFilter(groupedData);
        });
    }

    private void applySearchFilter(List<UserConsumptionData> groupedData) {
        String searchText = pm.getSearchText();
        ConsumptionTypeFilter typeFilter = pm.getTypeFilter();

        // Filter based on search text and type filter
        List<UserConsumptionData> filtered = groupedData.stream()
            .filter(userData -> {
                // Apply type filter first
                boolean matchesTypeFilter = matchesTypeFilter(userData, typeFilter);
                if (!matchesTypeFilter) {
                    return false;
                }

                // If no search text, show all that match the type filter
                if (searchText == null || searchText.trim().isEmpty()) {
                    return true;
                }

                String searchLower = searchText.toLowerCase().trim();

                // Search by name
                if (userData.getUserName() != null && userData.getUserName().toLowerCase().contains(searchLower)) {
                    return true;
                }

                // Search by email
                if (userData.getUserEmail() != null && userData.getUserEmail().toLowerCase().contains(searchLower)) {
                    return true;
                }

                return false;
            })
            .collect(Collectors.toList());

        displayedSessionViewers.setAll(filtered);
    }

    private boolean matchesTypeFilter(UserConsumptionData userData, ConsumptionTypeFilter filter) {
        if (filter == null || filter == ConsumptionTypeFilter.ALL) {
            return true;
        }

        switch (filter) {
            case LIVE:
                return userData.hasLivestreamed && !userData.hasRecorded;
            case RECORDING:
                return userData.hasRecorded && !userData.hasLivestreamed;
            case BOTH:
                return userData.hasLivestreamed && userData.hasRecorded;
            default:
                return true;
        }
    }

    private void updateSessionStats(List<UserConsumptionData> groupedData) {
        int totalCount = groupedData.size();  // Unique users
        int livestreamCount = (int) groupedData.stream()
            .filter(ud -> ud.hasLivestreamed)
            .count();
        int recordedCount = (int) groupedData.stream()
            .filter(ud -> ud.hasRecorded && !ud.hasLivestreamed)
            .count();

        // Only update labels if they exist
        if (totalViewersLabel != null) {
            totalViewersLabel.setText(String.valueOf(totalCount));
        }
        if (livestreamCountLabel != null) {
            livestreamCountLabel.setText(String.valueOf(livestreamCount));
        }
        if (recordedCountLabel != null) {
            recordedCountLabel.setText(String.valueOf(recordedCount));
        }
    }

    private void updateSessionViewersGrid() {
        // Only update if grid exists
        if (sessionViewersGrid == null) {
            return;
        }

        // Build visual result with custom renderers
        int rowCount = displayedSessionViewers.size();

        VisualResultBuilder rsb = new VisualResultBuilder(rowCount,
            VisualColumnBuilder.create(MediasI18nKeys.User, null).build(),
            VisualColumnBuilder.create(MediasI18nKeys.Email, null).build(),
            VisualColumnBuilder.create(MediasI18nKeys.Type, null)
                .setValueRenderer(ValueRendererRegistry.getValueRenderer("consumptionType"))
                .build(),
            VisualColumnBuilder.create(MediasI18nKeys.TotalWatchTime, null)
                .setValueRenderer(ValueRendererRegistry.getValueRenderer("durationMinutes"))
                .build()
        );

        for (int i = 0; i < rowCount; i++) {
            UserConsumptionData userData = displayedSessionViewers.get(i);
            rsb.setValue(i, 0, userData.getUserName());
            rsb.setValue(i, 1, userData.getUserEmail());
            rsb.setValue(i, 2, userData);  // Pass UserConsumptionData object to renderer
            rsb.setValue(i, 3, userData);  // Pass UserConsumptionData object to renderer
        }

        sessionViewersGrid.setVisualResult(rsb.build());
    }

    /**
     * Represents aggregated media consumption data for a single user.
     * Groups multiple consumption entries by user and cumulates watch times.
     */
    static class UserConsumptionData {
        private final String userName;
        private final String userEmail;
        private long totalDurationMillis;
        public boolean hasLivestreamed;
        public boolean hasRecorded;
        private LocalDateTime lastWatchDateTime;

        public UserConsumptionData(String userName, String userEmail) {
            this.userName = userName;
            this.userEmail = userEmail;
        }

        public void addConsumption(MediaConsumption mc) {
            Long duration = mc.getDurationMillis();
            if (duration != null) {
                totalDurationMillis += duration;
            }

            Boolean livestreamed = mc.isLivestreamed();
            if (livestreamed != null && livestreamed) {
                hasLivestreamed = true;
            } else {
                hasRecorded = true;
            }

            LocalDateTime dateTime = mc.getDate();
            if (dateTime != null && (lastWatchDateTime == null || dateTime.isAfter(lastWatchDateTime))) {
                lastWatchDateTime = dateTime;
            }
        }

        public String getUserName() {
            return userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public long getTotalDurationMillis() {
            return totalDurationMillis;
        }

        public String getFormattedDuration() {
            long minutes = totalDurationMillis / 60000;
            return minutes + " min";
        }

        public String getTypeLabel() {
            if (hasLivestreamed && hasRecorded) {
                return "BOTH";
            } else if (hasLivestreamed) {
                return "LIVE";
            } else {
                return "RECORDING";
            }
        }

        public LocalDateTime getLastWatchDateTime() {
            return lastWatchDateTime;
        }
    }

    private List<UserConsumptionData> groupConsumptionsByUser(ObservableList<MediaConsumption> consumptions) {
        Map<String, UserConsumptionData> userMap = new HashMap<>();

        for (MediaConsumption mc : consumptions) {
            String userName = null;
            String userEmail = null;

            // Navigate through the entity relationships using proper typed methods
            Attendance attendance = mc.getAttendance();
            if (attendance != null) {
                DocumentLine documentLine = attendance.getDocumentLine();
                if (documentLine != null) {
                    Document document = documentLine.getDocument();
                    if (document != null) {
                        Person person = document.getPerson();
                        if (person != null) {
                            // Build full name from firstName and lastName
                            String firstName = person.getFirstName();
                            String lastName = person.getLastName();
                            if (firstName != null && lastName != null) {
                                userName = firstName + " " + lastName;
                            } else if (firstName != null) {
                                userName = firstName;
                            } else if (lastName != null) {
                                userName = lastName;
                            } else {
                                // Fallback to person.name if firstName/lastName are null
                                userName = person.getName();
                            }
                            userEmail = person.getEmail();
                        }
                    }
                }
            }

            if (userName == null || userName.isEmpty()) {
                userName = "Unknown User";
            }

            String key = userName + "|" + (userEmail != null ? userEmail : "");

            UserConsumptionData userData = userMap.get(key);
            if (userData == null) {
                userData = new UserConsumptionData(userName, userEmail);
                userMap.put(key, userData);
            }

            userData.addConsumption(mc);
        }

        return new ArrayList<>(userMap.values());
    }
}
