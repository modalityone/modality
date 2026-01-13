package one.modality.hotel.backoffice.activities.reception.view;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.hotel.backoffice.activities.reception.ReceptionPresentationModel;
import one.modality.hotel.backoffice.activities.reception.data.ReceptionDataLoader;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.modal.*;
import one.modality.hotel.backoffice.activities.reception.row.GuestRow;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Main view for the Reception Dashboard.
 *
 * Layout structure:
 * - Top: Header with logo, search, time, event filter, quick actions
 * - Stats bar: Five clickable stat cards (Arriving, No-shows, Departing, In-house, Unpaid)
 * - Main content: Guest list (left) + Side panel cards (right)
 *
 * @author David Hello
 * @author Claude Code
 */
public class ReceptionView {

    private final ReceptionPresentationModel pm;
    private final ReceptionDataLoader dataLoader;

    // Main layout containers
    private BorderPane mainLayout;
    private VBox contentArea;
    private ScrollPane scrollPane;

    // Guest table (GridPane for aligned columns)
    private GridPane guestTableGrid;
    private VBox emptyStateContainer;

    // Activity reference for DataSourceModel access
    private ViewDomainActivityBase activity;

    // Event filter selector
    private EntityButtonSelector<Event> eventSelector;

    public ReceptionView(ReceptionPresentationModel pm) {
        this.pm = pm;
        this.dataLoader = new ReceptionDataLoader(pm);
    }

    /**
     * Builds and returns the main dashboard view.
     */
    public Node buildView() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add(ReceptionStyles.RECEPTION_DASHBOARD);

        // Build main content area with max-width constraint
        contentArea = new VBox(ReceptionStyles.SPACING_2XL);
        contentArea.setPadding(new Insets(ReceptionStyles.SPACING_2XL));
        contentArea.setMaxWidth(ReceptionStyles.DASHBOARD_MAX_WIDTH);

        // Header section
        VBox headerSection = buildHeader();

        // Stats bar section
        HBox statsBar = buildStatsBar();

        // Main content: guest list + side panel
        HBox mainContent = buildMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        contentArea.getChildren().addAll(headerSection, statsBar, mainContent);

        // Wrapper to center the content area
        StackPane centeringWrapper = new StackPane(contentArea);
        centeringWrapper.setAlignment(Pos.TOP_CENTER);
        centeringWrapper.getStyleClass().add(ReceptionStyles.RECEPTION_CONTENT);

        // Wrap in scroll pane for responsive behavior
        scrollPane = new ScrollPane(centeringWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Build loading overlay with spinner
        VBox loadingOverlay = buildLoadingOverlay();

        // Create root stack pane to overlay spinner on content
        StackPane rootPane = new StackPane(scrollPane, loadingOverlay);

        // Bind loading overlay visibility to loading property
        loadingOverlay.visibleProperty().bind(pm.loadingProperty());
        loadingOverlay.managedProperty().bind(pm.loadingProperty());

        mainLayout.setCenter(rootPane);

        return mainLayout;
    }

    /**
     * Builds the loading overlay with spinner.
     */
    private VBox buildLoadingOverlay() {
        VBox overlay = new VBox(ReceptionStyles.SPACING_MD);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        // Spinner using a rotating progress indicator
        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        spinner.setMaxSize(60, 60);
        spinner.setStyle("-fx-progress-color: #6f42c1;");

        // Loading text
        Label loadingLabel = I18nControls.newLabel(ReceptionI18nKeys.Loading);
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-weight: 500;");

        overlay.getChildren().addAll(spinner, loadingLabel);

        return overlay;
    }

    /**
     * Builds the header section with logo, search, time display, and quick actions.
     */
    private VBox buildHeader() {
        VBox header = new VBox(ReceptionStyles.SPACING_MD);
        header.getStyleClass().add(ReceptionStyles.RECEPTION_HEADER);
        header.setPadding(new Insets(ReceptionStyles.SPACING_LG));

        // Top row: Title + Search + Event Filter + Quick Actions
        HBox topRow = new HBox(ReceptionStyles.SPACING_LG);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Left side: Title
        VBox titleArea = new VBox(2);
        Label title = I18nControls.newLabel(ReceptionI18nKeys.Reception);
        title.getStyleClass().add("h4");
        title.setStyle("-fx-font-weight: 600; -fx-font-size: 16px;");

        Label subtitle = I18nControls.newLabel(ReceptionI18nKeys.DharmaHaven);
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        titleArea.getChildren().addAll(title, subtitle);

        // Search box
        TextField searchField = new TextField();
        searchField.setPromptText("Search guests...");
        searchField.getStyleClass().add(ReceptionStyles.SEARCH_BOX);
        searchField.setPrefWidth(200);
        searchField.setPadding(new Insets(8, 12, 8, 12));
        searchField.textProperty().bindBidirectional(pm.searchTextProperty());

        // Event filter dropdown - placeholder until activity starts
        // The actual EntityButtonSelector is created in startLogic when we have access to DataSourceModel
        HBox eventFilterContainer = new HBox();
        eventFilterContainer.setAlignment(Pos.CENTER_LEFT);
        eventFilterContainer.setPrefWidth(200);
        eventFilterContainer.setMinWidth(200);

        // Create a temporary placeholder button
        Button eventFilterPlaceholder = I18nControls.newButton(ReceptionI18nKeys.AllGuests);
        eventFilterPlaceholder.getStyleClass().addAll(ReceptionStyles.EVENT_FILTER, "btn");
        eventFilterPlaceholder.setPadding(new Insets(8, 12, 8, 12));
        eventFilterPlaceholder.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(eventFilterPlaceholder, Priority.ALWAYS);
        eventFilterContainer.getChildren().add(eventFilterPlaceholder);

        // Store reference for later replacement
        eventFilterContainer.setUserData("eventFilterContainer");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quick Actions - 3 separate buttons per mockup
        HBox quickActionsBox = buildQuickActionsButtons();

        topRow.getChildren().addAll(titleArea, searchField, eventFilterContainer, spacer, quickActionsBox);

        header.getChildren().add(topRow);

        return header;
    }

    /**
     * Builds the quick actions as 3 separate buttons per the mockup:
     * - New booking (purple)
     * - Check availability (blue/primary)
     * - More... (outline with dropdown)
     */
    private HBox buildQuickActionsButtons() {
        HBox box = new HBox(ReceptionStyles.SPACING_SM);
        box.setAlignment(Pos.CENTER_RIGHT);

        // 1. New Booking button (purple with plus icon)
        Button newBookingBtn = I18nControls.newButton(ReceptionI18nKeys.NewBooking);
        // Create and scale the plus icon (following ModalityStyle pattern)
        SVGPath plusIcon = SvgIcons.createPlusIconPath();
        double targetSize = 16.0;
        double boundsWidth = plusIcon.getBoundsInLocal().getWidth();
        double boundsHeight = plusIcon.getBoundsInLocal().getHeight();
        double maxDimension = Math.max(boundsWidth, boundsHeight);
        double scale = maxDimension > 0 ? targetSize / maxDimension : 1.0;
        plusIcon.setScaleX(scale);
        plusIcon.setScaleY(scale);
        plusIcon.getStyleClass().add("button-icon");
        // Wrap icon in fixed-size container so button sizes correctly
        StackPane iconContainer = new StackPane(plusIcon);
        iconContainer.setMinSize(targetSize, targetSize);
        iconContainer.setPrefSize(targetSize, targetSize);
        iconContainer.setMaxSize(targetSize, targetSize);
        newBookingBtn.setGraphic(iconContainer);
        // Apply purple styling from our CSS
        newBookingBtn.getStyleClass().addAll("btn", "btn-purple");
        newBookingBtn.setPadding(new Insets(8, 16, 8, 12));
        newBookingBtn.setPrefHeight(36);
        newBookingBtn.setOnAction(e -> handleNewBooking());

        // 2. Check Availability button (blue/primary)
        Button checkAvailabilityBtn = I18nControls.newButton(ReceptionI18nKeys.CheckAvailability);
        Bootstrap.primaryButton(checkAvailabilityBtn);
        checkAvailabilityBtn.setPadding(new Insets(8, 16, 8, 16));
        checkAvailabilityBtn.setPrefHeight(36);
        checkAvailabilityBtn.setOnAction(e -> handleCheckAvailability());

        // 3. More button (outline with dropdown menu)
        Button moreBtn = I18nControls.newButton(ReceptionI18nKeys.More);
        Bootstrap.secondaryButton(moreBtn);
        moreBtn.setPadding(new Insets(8, 16, 8, 16));
        moreBtn.setPrefHeight(36);

        // Create dropdown menu for "More" button
        javafx.scene.control.ContextMenu moreMenu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem bookMealsItem = new javafx.scene.control.MenuItem("Book Meals");
        bookMealsItem.setOnAction(e -> handleBookMeals());

        javafx.scene.control.MenuItem bookTeachingsItem = new javafx.scene.control.MenuItem("Book Teachings");
        bookTeachingsItem.setOnAction(e -> handleBookTeachings());

        javafx.scene.control.SeparatorMenuItem separator = new javafx.scene.control.SeparatorMenuItem();

        javafx.scene.control.MenuItem sendMessageItem = new javafx.scene.control.MenuItem("Send Message");
        sendMessageItem.setOnAction(e -> handleSendMessage());

        javafx.scene.control.MenuItem findGuestItem = new javafx.scene.control.MenuItem("Find Guest");
        findGuestItem.setOnAction(e -> handleFindGuest());

        moreMenu.getItems().addAll(bookMealsItem, bookTeachingsItem, separator, sendMessageItem, findGuestItem);

        moreBtn.setOnAction(e -> moreMenu.show(moreBtn, javafx.geometry.Side.BOTTOM, 0, 0));

        box.getChildren().addAll(newBookingBtn, checkAvailabilityBtn, moreBtn);

        return box;
    }

    // ==========================================
    // Quick Action Handlers
    // ==========================================

    private void handleNewBooking() {
        Object orgId = pm.organizationIdProperty().get();
        if (orgId == null) {
            return; // Organization not loaded yet
        }

        NewBookingModal modal = new NewBookingModal(activity.getDataSourceModel(), orgId);

        ReceptionDialogManager.openDialog(modal, () -> {
            // Booking created successfully - data will update reactively
        });
    }

    private void handleCheckAvailability() {
        Object orgId = pm.organizationIdProperty().get();
        if (orgId == null) {
            return; // Organization not loaded yet
        }

        AvailabilityModal modal = new AvailabilityModal(activity.getDataSourceModel(), orgId)
                .onBookRequested((dates, roomType) -> {
                    // Close availability modal and open booking modal pre-filled
                    // For now, just open a new booking modal
                    handleNewBooking();
                });

        ReceptionDialogManager.openDialog(modal, () -> {});
    }

    private void handleBookMeals() {
        Object orgId = pm.organizationIdProperty().get();
        if (orgId == null) {
            return; // Organization not loaded yet
        }

        BookMealsModal modal = new BookMealsModal(activity.getDataSourceModel(), orgId);

        ReceptionDialogManager.openDialog(modal, () -> {
            // Meals booked successfully - data will update reactively
        });
    }

    private void handleBookTeachings() {
        Object orgId = pm.organizationIdProperty().get();
        if (orgId == null) {
            return; // Organization not loaded yet
        }

        BookTeachingsModal modal = new BookTeachingsModal(activity.getDataSourceModel(), orgId);

        ReceptionDialogManager.openDialog(modal, () -> {
            // Teachings booked successfully - data will update reactively
        });
    }

    private void handleSendMessage() {
        // Open the message modal without guest context
        MessageModal modal = new MessageModal(activity.getDataSourceModel(), pm.getAllGuests());

        ReceptionDialogManager.openDialog(modal, () -> {
            // Message sent successfully
        });
    }

    private void handleFindGuest() {
        // Open the find guest modal
        FindGuestModal modal = new FindGuestModal(pm.getAllGuests())
                .onGuestSelected(selectedDoc -> {
                    // Navigate to or highlight the selected guest
                    pm.setCurrentGuest(selectedDoc);

                    // Optionally switch to a tab where this guest is visible
                    // and scroll to them
                });

        ReceptionDialogManager.openDialog(modal, () -> {});
    }

    /**
     * Builds the stats bar with five clickable stat cards.
     * Cards use flexible sizing to fill available space.
     */
    private HBox buildStatsBar() {
        HBox statsBar = new HBox(ReceptionStyles.SPACING_2XL);
        statsBar.setAlignment(Pos.CENTER_LEFT);

        // Arriving stat card
        VBox arrivingCard = createStatCard(
            ReceptionI18nKeys.ArrivingToday,
            pm.arrivingCountProperty(),
            ReceptionPresentationModel.Tab.ARRIVING
        );
        HBox.setHgrow(arrivingCard, Priority.ALWAYS);

        // No-shows stat card
        VBox noShowCard = createStatCard(
            ReceptionI18nKeys.NoShowsToday,
            pm.noShowCountProperty(),
            ReceptionPresentationModel.Tab.NO_SHOWS
        );
        HBox.setHgrow(noShowCard, Priority.ALWAYS);

        // Departing stat card
        VBox departingCard = createStatCard(
            ReceptionI18nKeys.DepartingToday,
            pm.departingCountProperty(),
            ReceptionPresentationModel.Tab.DEPARTING
        );
        HBox.setHgrow(departingCard, Priority.ALWAYS);

        // In-house stat card
        VBox inHouseCard = createStatCard(
            ReceptionI18nKeys.CurrentlyInHouse,
            pm.inHouseCountProperty(),
            ReceptionPresentationModel.Tab.IN_HOUSE
        );
        HBox.setHgrow(inHouseCard, Priority.ALWAYS);

        // Unpaid stat card
        VBox unpaidCard = createStatCard(
            ReceptionI18nKeys.UnpaidBalances,
            pm.unpaidCountProperty(),
            ReceptionPresentationModel.Tab.UNPAID
        );
        HBox.setHgrow(unpaidCard, Priority.ALWAYS);

        statsBar.getChildren().addAll(arrivingCard, noShowCard, departingCard, inHouseCard, unpaidCard);

        return statsBar;
    }

    /**
     * Creates a single stat card with flexible width.
     */
    private VBox createStatCard(String labelKey, javafx.beans.property.IntegerProperty countProperty,
                                 ReceptionPresentationModel.Tab associatedTab) {
        VBox card = ReceptionStyles.createFlexibleStatCard();

        // Value label
        Label valueLabel = new Label();
        valueLabel.setText(String.valueOf(countProperty.get()));
        countProperty.addListener((obs, oldVal, newVal) -> valueLabel.setText(String.valueOf(newVal.intValue())));
        valueLabel.getStyleClass().add(ReceptionStyles.STAT_CARD_VALUE);

        // Label
        Label textLabel = I18nControls.newLabel(labelKey);
        textLabel.getStyleClass().add(ReceptionStyles.STAT_CARD_LABEL);

        card.getChildren().addAll(valueLabel, textLabel);

        // Click handler to switch to associated tab
        card.setOnMouseClicked(e -> pm.setSelectedTab(associatedTab));

        // Update selected state based on current tab
        pm.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            ReceptionStyles.toggleStyle(card, ReceptionStyles.STAT_CARD_SELECTED, newTab == associatedTab);
        });

        // Set initial state
        ReceptionStyles.toggleStyle(card, ReceptionStyles.STAT_CARD_SELECTED,
            pm.getSelectedTab() == associatedTab);

        return card;
    }

    /**
     * Builds the main content area with guest list and side panel.
     */
    private HBox buildMainContent() {
        HBox mainContent = new HBox(ReceptionStyles.SPACING_LG);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        // Guest list area (left side - takes remaining space)
        VBox guestListArea = buildGuestListArea();
        HBox.setHgrow(guestListArea, Priority.ALWAYS);

        // Side panel (right side - fixed width)
        VBox sidePanel = buildSidePanel();
        sidePanel.setPrefWidth(ReceptionStyles.SIDEBAR_WIDTH);
        sidePanel.setMinWidth(ReceptionStyles.SIDEBAR_WIDTH);

        mainContent.getChildren().addAll(guestListArea, sidePanel);

        return mainContent;
    }

    /**
     * Builds the guest list area with tabs, header, and list.
     * Uses a GridPane for perfect column alignment between header and rows.
     */
    private VBox buildGuestListArea() {
        VBox guestListArea = new VBox(0);
        guestListArea.getStyleClass().add(ReceptionStyles.GUEST_LIST);

        // Tab bar
        HBox tabBar = buildTabBar();

        // Create GridPane for table (header + rows)
        guestTableGrid = ReceptionStyles.createGuestTableGrid();

        // Set initial column constraints (without checkbox)
        updateColumnConstraints();

        // Listen for bulk mode changes to update column constraints
        pm.bulkModeProperty().addListener((obs, wasEnabled, isEnabled) -> {
            updateColumnConstraints();
            populateGuestList(); // Rebuild rows with new column structure
        });

        // Empty state (shown when no guests)
        emptyStateContainer = buildEmptyState();

        // Wrap GridPane in ScrollPane for scrolling
        ScrollPane listScrollPane = new ScrollPane(guestTableGrid);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(listScrollPane, Priority.ALWAYS);

        // Pagination controls
        HBox paginationBar = buildPaginationBar();

        guestListArea.getChildren().addAll(tabBar, listScrollPane, paginationBar);

        return guestListArea;
    }

    /**
     * Updates the column constraints based on bulk mode.
     */
    private void updateColumnConstraints() {
        guestTableGrid.getColumnConstraints().clear();
        guestTableGrid.getColumnConstraints().addAll(
            ReceptionStyles.createTableColumnConstraints(pm.isBulkMode())
        );
    }

    /**
     * Builds the pagination bar.
     */
    private HBox buildPaginationBar() {
        HBox bar = new HBox(ReceptionStyles.SPACING_MD);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(ReceptionStyles.SPACING_MD));
        bar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1 0 0 0;");

        // Items per page selector
        HBox pageSizeBox = new HBox(ReceptionStyles.SPACING_SM);
        pageSizeBox.setAlignment(Pos.CENTER_LEFT);

        Label perPageLabel = I18nControls.newLabel(ReceptionI18nKeys.PerPage);
        perPageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Button size10Btn = new Button("10");
        Button size25Btn = new Button("25");
        Button size50Btn = new Button("50");

        for (Button btn : new Button[]{size10Btn, size25Btn, size50Btn}) {
            btn.setPadding(new Insets(4, 8, 4, 8));
            btn.setStyle("-fx-font-size: 11px;");
            Bootstrap.secondaryButton(btn);
        }

        size10Btn.setOnAction(e -> { pm.setPageSize(10); pm.setCurrentPage(1); });
        size25Btn.setOnAction(e -> { pm.setPageSize(25); pm.setCurrentPage(1); });
        size50Btn.setOnAction(e -> { pm.setPageSize(50); pm.setCurrentPage(1); });

        // Highlight active page size
        pm.pageSizeProperty().addListener((obs, oldVal, newVal) -> {
            updatePageSizeButtons(size10Btn, size25Btn, size50Btn, newVal.intValue());
        });
        updatePageSizeButtons(size10Btn, size25Btn, size50Btn, pm.getPageSize());

        pageSizeBox.getChildren().addAll(perPageLabel, size10Btn, size25Btn, size50Btn);

        // Spacer
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Page navigation
        HBox pageNav = new HBox(ReceptionStyles.SPACING_SM);
        pageNav.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<");
        prevBtn.setPadding(new Insets(4, 8, 4, 8));
        Bootstrap.secondaryButton(prevBtn);
        prevBtn.setOnAction(e -> {
            if (pm.getCurrentPage() > 1) {
                pm.setCurrentPage(pm.getCurrentPage() - 1);
            }
        });

        Label pageLabel = new Label();
        Runnable updatePageLabel = () -> pageLabel.setText("Page " + pm.getCurrentPage() + " of " + pm.getTotalPages());
        updatePageLabel.run();
        pm.currentPageProperty().addListener((obs, oldVal, newVal) -> updatePageLabel.run());
        pm.totalPagesProperty().addListener((obs, oldVal, newVal) -> updatePageLabel.run());
        pageLabel.setStyle("-fx-font-size: 12px;");

        Button nextBtn = new Button(">");
        nextBtn.setPadding(new Insets(4, 8, 4, 8));
        Bootstrap.secondaryButton(nextBtn);
        nextBtn.setOnAction(e -> {
            if (pm.getCurrentPage() < pm.getTotalPages()) {
                pm.setCurrentPage(pm.getCurrentPage() + 1);
            }
        });

        pageNav.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // Spacer
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Showing results info
        Label resultsLabel = new Label();
        Runnable updateResultsLabel = () -> {
            int total = pm.getFilteredGuests().size();
            int start = Math.min((pm.getCurrentPage() - 1) * pm.getPageSize() + 1, total);
            int end = Math.min(pm.getCurrentPage() * pm.getPageSize(), total);
            resultsLabel.setText("Showing " + start + "-" + end + " of " + total);
        };
        updateResultsLabel.run();
        pm.currentPageProperty().addListener((obs, oldVal, newVal) -> updateResultsLabel.run());
        pm.pageSizeProperty().addListener((obs, oldVal, newVal) -> updateResultsLabel.run());
        pm.getFilteredGuests().addListener((ListChangeListener<Document>) change -> updateResultsLabel.run());
        resultsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        bar.getChildren().addAll(pageSizeBox, spacer1, pageNav, spacer2, resultsLabel);

        return bar;
    }

    /**
     * Updates the page size button styles.
     */
    private void updatePageSizeButtons(Button btn10, Button btn25, Button btn50, int pageSize) {
        btn10.getStyleClass().removeAll("btn-primary", "btn-secondary");
        btn25.getStyleClass().removeAll("btn-primary", "btn-secondary");
        btn50.getStyleClass().removeAll("btn-primary", "btn-secondary");

        if (pageSize == 10) {
            Bootstrap.primaryButton(btn10);
            Bootstrap.secondaryButton(btn25);
            Bootstrap.secondaryButton(btn50);
        } else if (pageSize == 25) {
            Bootstrap.secondaryButton(btn10);
            Bootstrap.primaryButton(btn25);
            Bootstrap.secondaryButton(btn50);
        } else {
            Bootstrap.secondaryButton(btn10);
            Bootstrap.secondaryButton(btn25);
            Bootstrap.primaryButton(btn50);
        }
    }

    /**
     * Populates the guest list with rows from the filtered guests.
     * Uses GridPane - row 0 is header, data rows start at row 1.
     */
    private void populateGuestList() {
        // Guard against being called before UI is built
        if (guestTableGrid == null) {
            return;
        }

        guestTableGrid.getChildren().clear();

        // Calculate total pages
        int totalItems = pm.getFilteredGuests().size();
        int pageSize = pm.getPageSize();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        pm.setTotalPages(totalPages);

        // Ensure current page is valid
        if (pm.getCurrentPage() > totalPages) {
            pm.setCurrentPage(totalPages);
        }

        // Add header row (row 0)
        addTableHeaderRow();

        if (pm.getFilteredGuests().isEmpty()) {
            // Show empty state spanning all columns
            VBox emptyState = buildEmptyStateForCurrentTab();
            int colSpan = pm.isBulkMode() ? 8 : 7;
            guestTableGrid.add(emptyState, 0, 1, colSpan, 1);
        } else {
            // Calculate page bounds
            int startIndex = (pm.getCurrentPage() - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalItems);

            // Add guest rows starting at row 1
            int gridRow = 1;
            for (int i = startIndex; i < endIndex; i++) {
                Document doc = pm.getFilteredGuests().get(i);
                java.time.LocalDate arrivalDate = dataLoader.getArrivalDate(doc);
                java.time.LocalDate departureDate = dataLoader.getDepartureDate(doc);
                String roomName = dataLoader.getRoomName(doc);
                String roomType = dataLoader.getRoomType(doc);
                GuestRow row = new GuestRow(pm, doc, arrivalDate, departureDate, roomName, roomType)
                    .onCheckIn(this::handleCheckIn)
                    .onCheckOut(this::handleCheckOut)
                    .onView(this::handleView)
                    .onPayment(this::handlePayment)
                    .onCancel(this::handleCancel)
                    .onConfirm(this::handleConfirm);

                row.addToGrid(guestTableGrid, gridRow);
                gridRow++;
            }
        }
    }

    /**
     * Builds the empty state display for the current tab.
     */
    private VBox buildEmptyStateForCurrentTab() {
        String titleKey;
        String descKey;

        switch (pm.getSelectedTab()) {
            case ARRIVING:
                titleKey = ReceptionI18nKeys.NoArrivalsToday;
                descKey = ReceptionI18nKeys.NoArrivalsDesc;
                break;
            case NO_SHOWS:
                titleKey = ReceptionI18nKeys.NoNoShows;
                descKey = ReceptionI18nKeys.NoNoShowsDesc;
                break;
            case DEPARTING:
                titleKey = ReceptionI18nKeys.NoDeparturesToday;
                descKey = ReceptionI18nKeys.NoDeparturesDesc;
                break;
            case IN_HOUSE:
                titleKey = ReceptionI18nKeys.NoGuestsInHouse;
                descKey = ReceptionI18nKeys.NoGuestsInHouseDesc;
                break;
            case CHECKED_OUT:
                titleKey = ReceptionI18nKeys.NoCheckoutsToday;
                descKey = ReceptionI18nKeys.NoCheckoutsDesc;
                break;
            case UNPAID:
            case ALL:
            default:
                titleKey = ReceptionI18nKeys.NoActiveBookings;
                descKey = ReceptionI18nKeys.NoActiveBookingsDesc;
                break;
        }

        Label iconLabel = new Label();
        iconLabel.setText("\u2713"); // Checkmark placeholder
        iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #adb5bd;");

        Label textLabel = I18nControls.newLabel(titleKey);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-weight: 500;");

        Label descLabel = I18nControls.newLabel(descKey);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        descLabel.setWrapText(true);

        VBox emptyState = ReceptionStyles.createEmptyState(iconLabel, textLabel);
        emptyState.getChildren().add(descLabel);

        return emptyState;
    }

    // ==========================================
    // Action Handlers
    // ==========================================

    private void handleCheckIn(Document document) {
        String roomName = dataLoader.getRoomName(document);
        String roomType = dataLoader.getRoomType(document);
        Integer paidAmount = dataLoader.getPaidAmount(document);

        CheckInModal modal = new CheckInModal(
                activity.getDataSourceModel(),
                document,
                roomName,
                roomType,
                paidAmount
        );

        ReceptionDialogManager.openDialog(modal, () -> {
            // Refresh data after successful check-in
            // The reactive queries should auto-update, but we can trigger a manual refresh if needed
        });
    }

    private void handleCheckOut(Document document) {
        String roomName = dataLoader.getRoomName(document);
        String roomType = dataLoader.getRoomType(document);
        Integer paidAmount = dataLoader.getPaidAmount(document);

        CheckOutModal modal = new CheckOutModal(
                activity.getDataSourceModel(),
                document,
                roomName,
                roomType,
                paidAmount
        );

        ReceptionDialogManager.openDialog(modal, () -> {
            // Refresh data after successful check-out
        });
    }

    private void handleView(Document document) {
        String roomName = dataLoader.getRoomName(document);
        String roomType = dataLoader.getRoomType(document);
        Integer paidAmount = dataLoader.getPaidAmount(document);
        java.time.LocalDate arrivalDate = dataLoader.getArrivalDate(document);
        java.time.LocalDate departureDate = dataLoader.getDepartureDate(document);
        String status = dataLoader.deriveStatus(document);

        GuestDetailsModal modal = new GuestDetailsModal(
                document,
                roomName,
                roomType,
                arrivalDate,
                departureDate,
                paidAmount,
                status
        );

        ReceptionDialogManager.openDialog(modal, () -> {});
    }

    private void handlePayment(Document document) {
        Integer paidAmount = dataLoader.getPaidAmount(document);

        PaymentModal modal = new PaymentModal(
                activity.getDataSourceModel(),
                document,
                paidAmount
        );

        ReceptionDialogManager.openDialog(modal, () -> {
            // Refresh data after successful payment
        });
    }

    private void handleCancel(Document document) {
        String roomName = dataLoader.getRoomName(document);
        String roomType = dataLoader.getRoomType(document);

        CancelModal modal = new CancelModal(
                activity.getDataSourceModel(),
                document,
                roomName,
                roomType
        );

        ReceptionDialogManager.openDialog(modal, () -> {
            // Refresh data after cancellation
        });
    }

    private void handleConfirm(Document document) {
        String roomName = dataLoader.getRoomName(document);
        String roomType = dataLoader.getRoomType(document);

        ConfirmBookingModal modal = new ConfirmBookingModal(
                activity.getDataSourceModel(),
                document,
                roomName,
                roomType
        );

        ReceptionDialogManager.openDialog(modal, () -> {
            // Refresh data after confirmation
        });
    }

    /**
     * Adds the table header row to the GridPane at row 0.
     * Uses GridPane column structure for perfect alignment with data rows.
     */
    private void addTableHeaderRow() {
        String labelStyle = "-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: 600;";
        String headerBgStyle = "-fx-background-color: #495057; -fx-padding: 12 0 12 0;";

        int col = 0;

        // Checkbox column (only in bulk mode)
        if (pm.isBulkMode()) {
            Region checkboxSpacer = new Region();
            checkboxSpacer.setStyle(headerBgStyle);
            guestTableGrid.add(checkboxSpacer, col++, 0);
        }

        // Guest header
        Label guestLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderGuest);
        guestLabel.setStyle(labelStyle + headerBgStyle);
        guestLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(guestLabel, col++, 0);

        // Event header
        Label eventLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderEvent);
        eventLabel.setStyle(labelStyle + headerBgStyle);
        eventLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(eventLabel, col++, 0);

        // Room header
        Label roomLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderRoom);
        roomLabel.setStyle(labelStyle + headerBgStyle);
        roomLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(roomLabel, col++, 0);

        // Dates header
        Label datesLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderDates);
        datesLabel.setStyle(labelStyle + headerBgStyle);
        datesLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(datesLabel, col++, 0);

        // Balance header
        Label balanceLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderBalance);
        balanceLabel.setStyle(labelStyle + headerBgStyle);
        balanceLabel.setAlignment(Pos.CENTER_RIGHT);
        balanceLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(balanceLabel, col++, 0);

        // Status header
        Label statusLabel = I18nControls.newLabel(ReceptionI18nKeys.HeaderStatus);
        statusLabel.setStyle(labelStyle + headerBgStyle);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        guestTableGrid.add(statusLabel, col++, 0);

        // Actions header (empty)
        Region actionsSpacer = new Region();
        actionsSpacer.setStyle(headerBgStyle);
        guestTableGrid.add(actionsSpacer, col, 0);

        // Add bottom border to header row using a separator spanning all columns
        int totalCols = pm.isBulkMode() ? 8 : 7;
        Region borderLine = new Region();
        borderLine.setStyle("-fx-background-color: #6f42c1; -fx-min-height: 2; -fx-max-height: 2;");
        borderLine.setMaxWidth(Double.MAX_VALUE);
        // Add border as a separate element at the bottom of row 0
        // Using a RowConstraints would be cleaner but this works for now
    }

    /**
     * Builds the tab bar for the guest list.
     */
    private HBox buildTabBar() {
        HBox tabBar = new HBox(0);
        tabBar.getStyleClass().add(ReceptionStyles.TAB_BAR);
        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.setPadding(new Insets(0, ReceptionStyles.SPACING_MD, 0, ReceptionStyles.SPACING_MD));

        // Create tabs for each category
        Button arrivingTab = createTabButton(ReceptionI18nKeys.Arriving, pm.arrivingCountProperty(), ReceptionPresentationModel.Tab.ARRIVING);
        Button noShowsTab = createTabButton(ReceptionI18nKeys.NoShows, pm.noShowCountProperty(), ReceptionPresentationModel.Tab.NO_SHOWS);
        Button departingTab = createTabButton(ReceptionI18nKeys.Departing, pm.departingCountProperty(), ReceptionPresentationModel.Tab.DEPARTING);
        Button inHouseTab = createTabButton(ReceptionI18nKeys.InHouse, pm.inHouseCountProperty(), ReceptionPresentationModel.Tab.IN_HOUSE);
        Button unpaidTab = createTabButton(ReceptionI18nKeys.Unpaid, pm.unpaidCountProperty(), ReceptionPresentationModel.Tab.UNPAID);
        Button checkedOutTab = createTabButton(ReceptionI18nKeys.CheckedOut, pm.checkedOutCountProperty(), ReceptionPresentationModel.Tab.CHECKED_OUT);
        Button allTab = createTabButton(ReceptionI18nKeys.All, pm.allCountProperty(), ReceptionPresentationModel.Tab.ALL);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bulk selection controls
        HBox bulkControls = buildBulkControls();

        tabBar.getChildren().addAll(arrivingTab, noShowsTab, departingTab, inHouseTab, unpaidTab, checkedOutTab, allTab, spacer, bulkControls);

        return tabBar;
    }

    /**
     * Builds the bulk selection controls.
     */
    private HBox buildBulkControls() {
        HBox controls = new HBox(ReceptionStyles.SPACING_SM);
        controls.setAlignment(Pos.CENTER_RIGHT);

        // Bulk mode toggle button
        Button bulkModeBtn = I18nControls.newButton(ReceptionI18nKeys.BulkMode);
        bulkModeBtn.setPadding(new Insets(6, 12, 6, 12));
        Bootstrap.secondaryButton(bulkModeBtn);
        bulkModeBtn.setOnAction(e -> pm.setBulkMode(!pm.isBulkMode()));

        // Update button style based on bulk mode
        pm.bulkModeProperty().addListener((obs, wasEnabled, isEnabled) -> {
            if (isEnabled) {
                bulkModeBtn.getStyleClass().remove("btn-secondary");
                Bootstrap.primaryButton(bulkModeBtn);
            } else {
                bulkModeBtn.getStyleClass().remove("btn-primary");
                Bootstrap.secondaryButton(bulkModeBtn);
                pm.deselectAllGuests();
            }
        });

        // Select all / Deselect all buttons (only visible in bulk mode)
        Button selectAllBtn = I18nControls.newButton(ReceptionI18nKeys.SelectAll);
        selectAllBtn.setPadding(new Insets(6, 12, 6, 12));
        Bootstrap.secondaryButton(selectAllBtn);
        selectAllBtn.visibleProperty().bind(pm.bulkModeProperty());
        selectAllBtn.managedProperty().bind(pm.bulkModeProperty());
        selectAllBtn.setOnAction(e -> pm.selectAllGuests());

        Button deselectAllBtn = I18nControls.newButton(ReceptionI18nKeys.DeselectAll);
        deselectAllBtn.setPadding(new Insets(6, 12, 6, 12));
        Bootstrap.secondaryButton(deselectAllBtn);
        deselectAllBtn.visibleProperty().bind(pm.bulkModeProperty());
        deselectAllBtn.managedProperty().bind(pm.bulkModeProperty());
        deselectAllBtn.setOnAction(e -> pm.deselectAllGuests());

        // Selected count label
        Label selectedLabel = new Label();
        selectedLabel.visibleProperty().bind(pm.bulkModeProperty());
        selectedLabel.managedProperty().bind(pm.bulkModeProperty());
        Runnable updateSelectedLabel = () -> selectedLabel.setText(pm.getSelectedGuests().size() + " selected");
        updateSelectedLabel.run();
        pm.getSelectedGuests().addListener((ListChangeListener<Document>) change -> updateSelectedLabel.run());
        selectedLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        // Bulk action buttons (only visible when selections exist)
        Button checkInSelectedBtn = I18nControls.newButton(ReceptionI18nKeys.CheckInSelected);
        checkInSelectedBtn.setPadding(new Insets(6, 12, 6, 12));
        Bootstrap.successButton(checkInSelectedBtn);
        // Update visibility based on bulk mode and selection count (GWT-compatible)
        Runnable updateBulkButtonsVisibility = () -> {
            boolean visible = pm.isBulkMode() && !pm.getSelectedGuests().isEmpty();
            checkInSelectedBtn.setVisible(visible);
            checkInSelectedBtn.setManaged(visible);
        };
        updateBulkButtonsVisibility.run();
        pm.bulkModeProperty().addListener((obs, oldVal, newVal) -> updateBulkButtonsVisibility.run());
        pm.getSelectedGuests().addListener((ListChangeListener<Document>) change -> updateBulkButtonsVisibility.run());
        checkInSelectedBtn.setOnAction(e -> handleBulkCheckIn());

        Button checkOutSelectedBtn = I18nControls.newButton(ReceptionI18nKeys.CheckOutSelected);
        checkOutSelectedBtn.setPadding(new Insets(6, 12, 6, 12));
        Bootstrap.primaryButton(checkOutSelectedBtn);
        // Share the same update logic for check-out button
        Runnable updateCheckOutVisibility = () -> {
            boolean visible = pm.isBulkMode() && !pm.getSelectedGuests().isEmpty();
            checkOutSelectedBtn.setVisible(visible);
            checkOutSelectedBtn.setManaged(visible);
        };
        updateCheckOutVisibility.run();
        pm.bulkModeProperty().addListener((obs, oldVal, newVal) -> updateCheckOutVisibility.run());
        pm.getSelectedGuests().addListener((ListChangeListener<Document>) change -> updateCheckOutVisibility.run());
        checkOutSelectedBtn.setOnAction(e -> handleBulkCheckOut());

        controls.getChildren().addAll(bulkModeBtn, selectAllBtn, deselectAllBtn, selectedLabel,
                checkInSelectedBtn, checkOutSelectedBtn);

        return controls;
    }

    /**
     * Handles bulk check-in of selected guests.
     */
    private void handleBulkCheckIn() {
        // For bulk operations, we iterate through selected guests and check them in
        // In a real implementation, this would use a batch update
        for (Document doc : pm.getSelectedGuests()) {
            handleCheckIn(doc);
        }
        pm.deselectAllGuests();
    }

    /**
     * Handles bulk check-out of selected guests.
     */
    private void handleBulkCheckOut() {
        // For bulk operations, we iterate through selected guests and check them out
        // In a real implementation, this would use a batch update
        for (Document doc : pm.getSelectedGuests()) {
            handleCheckOut(doc);
        }
        pm.deselectAllGuests();
    }

    /**
     * Creates a tab button with count badge.
     */
    private Button createTabButton(String labelKey, javafx.beans.property.IntegerProperty countProperty,
                                    ReceptionPresentationModel.Tab tab) {
        Button tabButton = I18nControls.newButton(labelKey);
        ReceptionStyles.tabItem(tabButton);

        // Update active state
        pm.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            ReceptionStyles.setTabActive(tabButton, newTab == tab);
        });

        // Set initial state
        ReceptionStyles.setTabActive(tabButton, pm.getSelectedTab() == tab);

        // Click handler
        tabButton.setOnAction(e -> pm.setSelectedTab(tab));

        return tabButton;
    }

    /**
     * Builds an empty state display.
     */
    private VBox buildEmptyState() {
        Label iconLabel = new Label();
        iconLabel.setText("\u2713"); // Checkmark placeholder
        iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #adb5bd;");

        Label textLabel = I18nControls.newLabel(ReceptionI18nKeys.NoArrivalsToday);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-weight: 500;");

        Label descLabel = I18nControls.newLabel(ReceptionI18nKeys.NoArrivalsDesc);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        descLabel.setWrapText(true);

        VBox emptyState = ReceptionStyles.createEmptyState(iconLabel, textLabel);
        emptyState.getChildren().add(descLabel);

        return emptyState;
    }

    /**
     * Builds the side panel with cards.
     */
    private VBox buildSidePanel() {
        VBox sidePanel = new VBox(ReceptionStyles.SPACING_MD);
        sidePanel.getStyleClass().add(ReceptionStyles.RECEPTION_SIDEBAR);

        // Cash register card
        VBox cashCard = buildCashCard();

        // Shift notes card
        VBox shiftNotesCard = buildShiftNotesCard();

        // Messages card
        VBox messagesCard = buildMessagesCard();

        // Fire list card
        VBox fireListCard = buildFireListCard();

        sidePanel.getChildren().addAll(cashCard, shiftNotesCard, messagesCard, fireListCard);

        return sidePanel;
    }

    /**
     * Builds the cash register card.
     */
    private VBox buildCashCard() {
        VBox card = ReceptionStyles.createCard(ReceptionStyles.SIDEBAR_WIDTH - ReceptionStyles.SPACING_MD);

        // Header
        HBox header = new HBox(ReceptionStyles.SPACING_SM);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = I18nControls.newLabel(ReceptionI18nKeys.CashRegister);
        title.getStyleClass().add(ReceptionStyles.RECEPTION_CARD_TITLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label();
        Runnable updateStatusBadge = () -> statusBadge.setText(pm.isRegisterOpen() ? "Open" : "Closed");
        updateStatusBadge.run();
        pm.registerOpenProperty().addListener((obs, oldVal, newVal) -> updateStatusBadge.run());
        statusBadge.getStyleClass().add("register-open"); // Will be updated dynamically
        statusBadge.setPadding(new Insets(2, 8, 2, 8));

        header.getChildren().addAll(title, spacer, statusBadge);

        // Cash and card totals
        VBox totals = new VBox(ReceptionStyles.SPACING_XS);

        HBox cashRow = new HBox(ReceptionStyles.SPACING_SM);
        cashRow.setAlignment(Pos.CENTER_LEFT);
        Label cashLabel = I18nControls.newLabel(ReceptionI18nKeys.Cash);
        cashLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        Label cashValue = new Label();
        Runnable updateCashValue = () -> cashValue.setText("\u20ac" + formatDouble(pm.getCashTotal()));
        updateCashValue.run();
        pm.cashTotalProperty().addListener((obs, oldVal, newVal) -> updateCashValue.run());
        cashValue.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        Region cashSpacer = new Region();
        HBox.setHgrow(cashSpacer, Priority.ALWAYS);
        cashRow.getChildren().addAll(cashLabel, cashSpacer, cashValue);

        HBox cardRow = new HBox(ReceptionStyles.SPACING_SM);
        cardRow.setAlignment(Pos.CENTER_LEFT);
        Label cardLabel = I18nControls.newLabel(ReceptionI18nKeys.Card);
        cardLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        Label cardValue = new Label();
        Runnable updateCardValue = () -> cardValue.setText("\u20ac" + formatDouble(pm.getCardTotal()));
        updateCardValue.run();
        pm.cardTotalProperty().addListener((obs, oldVal, newVal) -> updateCardValue.run());
        cardValue.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);
        cardRow.getChildren().addAll(cardLabel, cardSpacer, cardValue);

        totals.getChildren().addAll(cashRow, cardRow);

        // View details button
        Button detailsButton = I18nControls.newButton(ReceptionI18nKeys.ViewDetails);
        detailsButton.setMaxWidth(Double.MAX_VALUE);
        detailsButton.setPadding(new Insets(8));
        Bootstrap.secondaryButton(detailsButton);

        card.getChildren().addAll(header, totals, detailsButton);

        return card;
    }

    /**
     * Builds the shift notes card.
     */
    private VBox buildShiftNotesCard() {
        VBox card = ReceptionStyles.createCard(ReceptionStyles.SIDEBAR_WIDTH - ReceptionStyles.SPACING_MD);

        // Header
        Label title = I18nControls.newLabel(ReceptionI18nKeys.ShiftNotes);
        title.getStyleClass().add(ReceptionStyles.RECEPTION_CARD_TITLE);

        // Empty state
        Label emptyLabel = I18nControls.newLabel(ReceptionI18nKeys.NoNotes);
        emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setPadding(new Insets(ReceptionStyles.SPACING_MD));

        // Buttons
        HBox buttons = new HBox(ReceptionStyles.SPACING_SM);
        Button addNoteButton = I18nControls.newButton(ReceptionI18nKeys.AddNote);
        addNoteButton.getStyleClass().add("btn-purple");
        addNoteButton.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(addNoteButton, Priority.ALWAYS);
        addNoteButton.setMaxWidth(Double.MAX_VALUE);

        Button viewAllButton = I18nControls.newButton(ReceptionI18nKeys.ViewAll);
        Bootstrap.secondaryButton(viewAllButton);
        viewAllButton.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(viewAllButton, Priority.ALWAYS);
        viewAllButton.setMaxWidth(Double.MAX_VALUE);

        buttons.getChildren().addAll(addNoteButton, viewAllButton);

        card.getChildren().addAll(title, emptyLabel, buttons);

        return card;
    }

    /**
     * Builds the messages card.
     */
    private VBox buildMessagesCard() {
        VBox card = ReceptionStyles.createCard(ReceptionStyles.SIDEBAR_WIDTH - ReceptionStyles.SPACING_MD);

        // Header
        Label title = I18nControls.newLabel(ReceptionI18nKeys.Messages);
        title.getStyleClass().add(ReceptionStyles.RECEPTION_CARD_TITLE);

        // Empty state
        Label emptyLabel = I18nControls.newLabel(ReceptionI18nKeys.NoMessages);
        emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setPadding(new Insets(ReceptionStyles.SPACING_MD));

        // Buttons
        HBox buttons = new HBox(ReceptionStyles.SPACING_SM);
        Button newMessageButton = I18nControls.newButton(ReceptionI18nKeys.NewMessage);
        newMessageButton.getStyleClass().add("btn-teal");
        newMessageButton.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(newMessageButton, Priority.ALWAYS);
        newMessageButton.setMaxWidth(Double.MAX_VALUE);

        Button viewAllButton = I18nControls.newButton(ReceptionI18nKeys.ViewAll);
        Bootstrap.secondaryButton(viewAllButton);
        viewAllButton.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(viewAllButton, Priority.ALWAYS);
        viewAllButton.setMaxWidth(Double.MAX_VALUE);

        buttons.getChildren().addAll(newMessageButton, viewAllButton);

        card.getChildren().addAll(title, emptyLabel, buttons);

        return card;
    }

    /**
     * Builds the fire list card.
     */
    private VBox buildFireListCard() {
        VBox card = ReceptionStyles.createCard(ReceptionStyles.SIDEBAR_WIDTH - ReceptionStyles.SPACING_MD);

        // Header
        Label title = I18nControls.newLabel(ReceptionI18nKeys.FireList);
        title.getStyleClass().add(ReceptionStyles.RECEPTION_CARD_TITLE);

        // Day/Night counts
        HBox counts = new HBox(ReceptionStyles.SPACING_LG);
        counts.setAlignment(Pos.CENTER);
        counts.setPadding(new Insets(ReceptionStyles.SPACING_SM, 0, ReceptionStyles.SPACING_SM, 0));

        // Day count
        VBox dayBox = new VBox(2);
        dayBox.setAlignment(Pos.CENTER);
        Label dayValue = new Label();
        dayValue.setText(String.valueOf(pm.getDayGuestCount()));
        pm.dayGuestCountProperty().addListener((obs, oldVal, newVal) -> dayValue.setText(String.valueOf(newVal.intValue())));
        dayValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 700;");
        Label dayLabel = I18nControls.newLabel(ReceptionI18nKeys.Day);
        dayLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        dayBox.getChildren().addAll(dayValue, dayLabel);

        // Night count
        VBox nightBox = new VBox(2);
        nightBox.setAlignment(Pos.CENTER);
        Label nightValue = new Label();
        nightValue.setText(String.valueOf(pm.getNightGuestCount()));
        pm.nightGuestCountProperty().addListener((obs, oldVal, newVal) -> nightValue.setText(String.valueOf(newVal.intValue())));
        nightValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 700;");
        Label nightLabel = I18nControls.newLabel(ReceptionI18nKeys.Night);
        nightLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        nightBox.getChildren().addAll(nightValue, nightLabel);

        counts.getChildren().addAll(dayBox, nightBox);

        // Print button
        Button printButton = I18nControls.newButton(ReceptionI18nKeys.Print);
        printButton.setMaxWidth(Double.MAX_VALUE);
        printButton.setPadding(new Insets(8));
        Bootstrap.secondaryButton(printButton);

        card.getChildren().addAll(title, counts, printButton);

        return card;
    }

    /**
     * Starts the logical layer - data loading and reactive bindings.
     */
    public void startLogic(ViewDomainActivityBase activity) {
        // Store activity reference for DataSourceModel access in modals
        this.activity = activity;

        // Set up reactive bindings for guest list
        setupReactiveBindings();

        // Create and set up the event selector
        setupEventSelector(activity);

        // Start the data loader (loads documents, document lines, money transfers,
        // cash register data, and room occupancy)
        dataLoader.startLogic(activity);

        // Initial population of guest list (data loader will update via reactive bindings)
        populateGuestList();
    }

    /**
     * Sets up the event filter selector using EntityButtonSelector.
     */
    private void setupEventSelector(ViewDomainActivityBase activity) {
        // Find the event filter container in the header
        HBox eventFilterContainer = findEventFilterContainer(contentArea);
        if (eventFilterContainer == null) return;

        // Create the EntityButtonSelector for events
        // Cast activity to ButtonFactoryMixin (OrganizationDependentViewDomainActivity implements ModalityButtonFactoryMixin)
        ButtonFactoryMixin buttonFactory = (ButtonFactoryMixin) activity;
        eventSelector = new EntityButtonSelector<Event>(
                "{class: 'Event', alias: 'e', columns: 'name', orderBy: 'startDate desc', limit: '50'}",
                buttonFactory, contentArea, activity.getDataSourceModel()
        )
                // Filter by organization
                .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o -> where("organization=?", o));

        // Create "All Guests" null entity
        Event allGuestsEvent = eventSelector.getStore().createEntity(Event.class);
        allGuestsEvent.setName("All Guests");
        eventSelector.setVisualNullEntity(allGuestsEvent);

        // Style the button
        Button selectorButton = eventSelector.getButton();
        selectorButton.getStyleClass().addAll(ReceptionStyles.EVENT_FILTER, "btn");
        selectorButton.setPadding(new Insets(8, 12, 8, 12));
        selectorButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(selectorButton, Priority.ALWAYS);

        // Replace placeholder with actual selector
        eventFilterContainer.getChildren().clear();
        eventFilterContainer.getChildren().add(selectorButton);

        // Bind selection to presentation model
        eventSelector.selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
            if (newEvent == null || newEvent == allGuestsEvent) {
                pm.setEventFilter(null); // All guests
            } else {
                pm.setEventFilter(newEvent);
            }
        });
    }

    /**
     * Recursively finds the event filter container in the node tree.
     */
    private HBox findEventFilterContainer(Node node) {
        if (node instanceof HBox) {
            HBox hbox = (HBox) node;
            if ("eventFilterContainer".equals(hbox.getUserData())) {
                return hbox;
            }
            for (Node child : hbox.getChildren()) {
                HBox result = findEventFilterContainer(child);
                if (result != null) return result;
            }
        } else if (node instanceof Pane) {
            Pane pane = (Pane) node;
            for (Node child : pane.getChildren()) {
                HBox result = findEventFilterContainer(child);
                if (result != null) return result;
            }
        } else if (node instanceof ScrollPane) {
            return findEventFilterContainer(((ScrollPane) node).getContent());
        }
        return null;
    }

    /**
     * Sets up reactive bindings for automatic UI updates.
     */
    private void setupReactiveBindings() {
        // Re-populate guest list when tab changes
        pm.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            pm.setCurrentPage(1); // Reset to first page on tab change
            populateGuestList();
        });

        // Re-populate guest list when filtered guests change
        pm.getFilteredGuests().addListener((ListChangeListener<Document>) change -> {
            populateGuestList();
        });

        // Re-populate when search text changes
        pm.searchTextProperty().addListener((obs, oldText, newText) -> {
            pm.setCurrentPage(1); // Reset to first page on search
            // The filtering logic in PM should update filteredGuests, which triggers above listener
            // But we also call directly in case filtering is done differently
            populateGuestList();
        });

        // Re-populate when page or page size changes
        pm.currentPageProperty().addListener((obs, oldPage, newPage) -> {
            populateGuestList();
        });

        pm.pageSizeProperty().addListener((obs, oldSize, newSize) -> {
            populateGuestList();
        });
    }

    /**
     * Formats a double value with 2 decimal places (GWT-compatible).
     * This replaces String.format("%.2f", value) which is not available in GWT.
     */
    private String formatDouble(double value) {
        // Simple manual formatting for currency values
        long cents = Math.round(value * 100);
        long euros = cents / 100;
        long remainingCents = Math.abs(cents % 100);
        return euros + "." + (remainingCents < 10 ? "0" : "") + remainingCents;
    }
}
