package one.modality.hotel.backoffice.activities.household.dashboard.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.HouseholdActivity;
import one.modality.hotel.backoffice.activities.household.HouseholdI18nKeys;
import one.modality.hotel.backoffice.activities.household.dashboard.data.DashboardDataLoader;
import one.modality.hotel.backoffice.activities.household.dashboard.model.*;
import one.modality.hotel.backoffice.activities.household.dashboard.presenter.DashboardFilterManager;
import one.modality.hotel.backoffice.activities.household.dashboard.presenter.DashboardPresenter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard view for the Household module.
 * Shows cleaning tasks, checkouts, and arrivals organized by day.
 * <p>
 * This class follows the MVP pattern:
 * - Handles UI construction and rendering only
 * - Delegates business logic to DashboardPresenter
 * - Delegates data loading to DashboardDataLoader
 * - Delegates card creation to DashboardCardFactory
 *
 * @author Claude Code Assistant
 */
public final class HouseholdDashboardView {

    private final AccommodationPresentationModel pm;
    private final DashboardPresenter presenter;
    private DashboardDataLoader dataLoader;
    private DashboardCardFactory cardFactory;

    // UI Components
    private final VBox container = new VBox();
    private final VBox mainContent = new VBox();
    private final VBox loadingIndicator = new VBox();


    public HouseholdDashboardView(AccommodationPresentationModel pm) {
        this.pm = pm;
        this.presenter = new DashboardPresenter();
        initUi();
    }

    public Node buildUi() {
        return container;
    }

    public void startLogic(HouseholdActivity activity) {
        // Initialize data loader
        dataLoader = new DashboardDataLoader(pm, presenter.daysToDisplayProperty());
        dataLoader.startLogic(activity);

        // Initialize card factory
        cardFactory = new DashboardCardFactory(presenter, container);

        // Listen for data changes and reprocess
        dataLoader.getDocumentLines().addListener((ListChangeListener.Change<?> c) -> processAndRender());
        dataLoader.getAttendancesForGaps().addListener((ListChangeListener.Change<?> c) -> processAndRender());

        // Re-render when days to display changes
        FXProperties.runOnPropertiesChange(this::processAndRender,
                presenter.daysToDisplayProperty(), FXToday.todayProperty());

        // Re-render when filters change
        DashboardFilterManager filterManager = presenter.getFilterManager();
        FXProperties.runOnPropertiesChange(this::processAndRender,
                filterManager.cleanStatusFilterProperty(),
                filterManager.cleanRoomTypeFilterProperty(),
                filterManager.cleanBuildingFilterProperty(),
                filterManager.cleanCheckInFilterProperty(),
                filterManager.inspectBuildingFilterProperty(),
                filterManager.inspectCheckInFilterProperty());

        // Initial render
        processAndRender();
    }

    private void processAndRender() {
        // Process data through presenter
        presenter.processData(
                new ArrayList<>(dataLoader.getDocumentLines()),
                new ArrayList<>(dataLoader.getAttendancesForGaps())
        );
        // Render the UI
        renderDashboard();
    }

    private void initUi() {
        container.getStyleClass().add("household-dashboard");
        container.setBackground(Background.EMPTY);

        // Header
        VBox header = createHeader();

        // Spacer between header and main content
        Region spacer = new Region();
        spacer.setPrefHeight(40);

        // Main Content
        mainContent.getStyleClass().add("main-content");
        mainContent.setSpacing(24);
        mainContent.setBackground(Background.EMPTY);
        mainContent.setPadding(new Insets(0, 12, 0, 0));

        // Wrap main content in ScrollPane for vertical scrolling
        ScrollPane scrollPane = new ScrollPane(mainContent);
        Controls.setupVerticalScrollPane(scrollPane, mainContent);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Loading Indicator
        createLoadingIndicator();

        container.getChildren().addAll(header, spacer, scrollPane);
    }

    private void createLoadingIndicator() {
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setAlignment(Pos.CENTER);
        loadingIndicator.setSpacing(16);
        loadingIndicator.setPadding(new Insets(60, 20, 60, 20));
        loadingIndicator.setBackground(Background.EMPTY);

        Region spinner = Controls.createSpinner(50);

        Label loadingMessage = I18nControls.newLabel(HouseholdI18nKeys.LoadingData);
        loadingMessage.getStyleClass().add("loading-message");

        loadingIndicator.getChildren().addAll(spinner, loadingMessage);
    }

    private static final int MOBILE_BREAKPOINT = 600;

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(25));

        // Title Section
        VBox headerTitleSection = new VBox(6);
        headerTitleSection.getStyleClass().add("header-title-section");

        Label title = I18nControls.newLabel(HouseholdI18nKeys.HouseholdDashboard);
        title.getStyleClass().add("h1");

        HBox dateInfo = new HBox(12);
        dateInfo.getStyleClass().add("date-info");
        dateInfo.setAlignment(Pos.CENTER_LEFT);

        Label dateRangeLabel = new Label();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate today = FXToday.getToday();
            int days = presenter.daysToDisplayProperty().get();
            LocalDate end = today.plusDays(days - 1);
            dateRangeLabel.setText(today.format(DateTimeFormatter.ofPattern("MMMM d")) + "-"
                    + end.format(DateTimeFormatter.ofPattern("d, yyyy")));
        }, FXToday.todayProperty(), presenter.daysToDisplayProperty());

        dateInfo.getChildren().add(dateRangeLabel);
        headerTitleSection.getChildren().addAll(title, dateInfo);

        // View range control
        HBox viewRangeControl = new HBox(10);
        viewRangeControl.getStyleClass().add("view-range-control");
        viewRangeControl.setAlignment(Pos.CENTER_LEFT);
        viewRangeControl.setPadding(new Insets(0, 12, 0, 12));

        Label viewRangeLabel = I18nControls.newLabel(HouseholdI18nKeys.ViewNext);
        viewRangeLabel.getStyleClass().add("view-range-label");

        HBox daysInputWrapper = new HBox(6);
        daysInputWrapper.getStyleClass().add("days-input-wrapper");
        daysInputWrapper.setAlignment(Pos.CENTER_LEFT);

        TextField daysInput = new TextField();
        daysInput.getStyleClass().add("days-input");
        daysInput.setPrefWidth(50);
        daysInput.textProperty().bindBidirectional(presenter.daysToDisplayProperty(), new javafx.util.StringConverter<>() {
            @Override
            public String toString(Number object) {
                return object.toString();
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 7;
                }
            }
        });

        Label daysUnit = I18nControls.newLabel(HouseholdI18nKeys.Days);
        daysUnit.getStyleClass().add("days-unit");

        daysInputWrapper.getChildren().addAll(daysInput, daysUnit);
        viewRangeControl.getChildren().addAll(viewRangeLabel, daysInputWrapper);

        // Quick view buttons - use FlowPane for wrapping on mobile
        FlowPane quickViewBtns = new FlowPane();
        quickViewBtns.getStyleClass().add("quick-view-btns");
        quickViewBtns.setHgap(4);
        quickViewBtns.setVgap(4);

        quickViewBtns.getChildren().addAll(
                createQuickViewBtn(HouseholdI18nKeys.ThreeDays, 3),
                createQuickViewBtn(HouseholdI18nKeys.OneWeek, 7),
                createQuickViewBtn(HouseholdI18nKeys.TwoWeeks, 14),
                createQuickViewBtn(HouseholdI18nKeys.OneMonth, 30));

        // Controls container
        HBox headerControls = new HBox(12);
        headerControls.getStyleClass().add("header-controls");
        headerControls.setAlignment(Pos.CENTER_LEFT);
        headerControls.getChildren().addAll(viewRangeControl, quickViewBtns);

        // Desktop layout: Title on left, controls on right (in same HBox row)
        HBox desktopLayout = new HBox();
        desktopLayout.getStyleClass().add("header-top");
        desktopLayout.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        desktopLayout.getChildren().addAll(headerTitleSection, spacer, headerControls);

        // Mobile layout: Title and controls stacked vertically
        VBox mobileLayout = new VBox(16);
        mobileLayout.setAlignment(Pos.CENTER_LEFT);
        // Note: We can't add the same nodes to both layouts, so mobile layout shares nodes via responsive switching

        // Container that holds both layouts (only one visible at a time)
        StackPane layoutContainer = new StackPane(desktopLayout, mobileLayout);
        header.getChildren().add(layoutContainer);

        // Setup responsive design based on container width
        new ResponsiveDesign(header)
            .addResponsiveLayout(
                width -> width < MOBILE_BREAKPOINT,
                () -> {
                    // Mobile: Stack title and controls vertically
                    desktopLayout.setVisible(false);
                    desktopLayout.setManaged(false);
                    mobileLayout.setVisible(true);
                    mobileLayout.setManaged(true);
                    // Move nodes to mobile layout
                    desktopLayout.getChildren().clear();
                    mobileLayout.getChildren().clear();
                    mobileLayout.getChildren().addAll(headerTitleSection, headerControls);
                }
            )
            .addResponsiveLayout(
                width -> width >= MOBILE_BREAKPOINT,
                () -> {
                    // Desktop: Title on left, controls on right
                    mobileLayout.setVisible(false);
                    mobileLayout.setManaged(false);
                    desktopLayout.setVisible(true);
                    desktopLayout.setManaged(true);
                    // Move nodes to desktop layout
                    mobileLayout.getChildren().clear();
                    desktopLayout.getChildren().clear();
                    Region newSpacer = new Region();
                    HBox.setHgrow(newSpacer, Priority.ALWAYS);
                    desktopLayout.getChildren().addAll(headerTitleSection, newSpacer, headerControls);
                }
            )
            .start();

        return header;
    }

    private Button createQuickViewBtn(Object i18nKey, int days) {
        Button btn = I18nControls.newButton(i18nKey);
        btn.getStyleClass().add("quick-view-btn");
        btn.setOnAction(e -> presenter.daysToDisplayProperty().set(days));

        presenter.daysToDisplayProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() == days) {
                btn.getStyleClass().add("active");
            } else {
                btn.getStyleClass().remove("active");
            }
        });

        if (presenter.daysToDisplayProperty().get() == days) {
            btn.getStyleClass().add("active");
        }

        return btn;
    }

    private void renderDashboard() {
        mainContent.getChildren().clear();

        // Show loading if no data
        if (dataLoader == null || dataLoader.getDocumentLines().isEmpty()) {
            mainContent.getChildren().add(loadingIndicator);
            return;
        }

        // Render each day from presenter's processed data
        for (DayData dayData : presenter.getDayDataList()) {
            Node daySection = createDaySection(dayData);
            mainContent.getChildren().add(daySection);
        }
    }

    private Node createDaySection(DayData dayData) {
        LocalDate date = dayData.getDate();
        boolean isToday = dayData.isToday();

        // For non-today days with no activity, show empty day state
        if (!dayData.hasActivity() && !isToday) {
            return createEmptyDayState(date);
        }

        VBox daySection = new VBox(20);
        daySection.getStyleClass().add("day-section");
        daySection.setPadding(new Insets(24, 32, 24, 32));
        if (isToday) daySection.getStyleClass().add("today");

        // Header
        HBox dayHeader = new HBox(16);
        dayHeader.getStyleClass().add("day-header");
        dayHeader.setAlignment(Pos.CENTER_LEFT);

        HBox dayTitle = new HBox(16);
        dayTitle.getStyleClass().add("day-title");
        dayTitle.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("d")));
        dateLabel.getStyleClass().add("day-date");

        Label dayNameLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")));
        dayNameLabel.getStyleClass().add("day-name");

        dayTitle.getChildren().addAll(dateLabel, dayNameLabel);
        if (isToday) {
            Label todayBadge = I18nControls.newLabel(HouseholdI18nKeys.Today);
            todayBadge.getStyleClass().add("today-badge");
            todayBadge.setPadding(new Insets(4, 12, 4, 12));
            dayTitle.getChildren().add(todayBadge);
        }

        dayHeader.getChildren().add(dayTitle);

        // Content
        VBox dayContent = new VBox(24);
        dayContent.getStyleClass().add("day-content");

        if (isToday) {
            dayContent.getChildren().add(createCleaningSection(dayData.getCleaningCards()));
            dayContent.getChildren().add(createInspectionSection(dayData.getInspectionCards()));
            if (!dayData.getCheckoutCards().isEmpty()) {
                dayContent.getChildren().add(createCheckoutsSection(dayData.getCheckoutCards()));
            }
        }

        if (!isToday && !dayData.getCheckoutCards().isEmpty()) {
            dayContent.getChildren().add(createCheckoutsSection(dayData.getCheckoutCards()));
        }

        if (!dayData.getPartialCheckoutCards().isEmpty()) {
            dayContent.getChildren().add(createPartialCheckoutsSection(dayData.getPartialCheckoutCards()));
        }

        if (!dayData.getArrivalCards().isEmpty()) {
            String sectionTitle = isToday ?
                    I18n.getI18nText(HouseholdI18nKeys.TodaysCheckIns) :
                    I18n.getI18nText(HouseholdI18nKeys.CheckIns);
            dayContent.getChildren().add(createArrivalsSection(dayData.getArrivalCards(), sectionTitle));
        }

        daySection.getChildren().addAll(dayHeader, dayContent);
        return daySection;
    }

    private Node createCleaningSection(List<RoomCardData> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("cleaning-section");

        // Title row with count
        HBox titleLeft = new HBox(8);
        titleLeft.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.RoomsToClean);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleLeft.getChildren().addAll(titleLabel, countLabel);

        // Filters - use FlowPane for wrapping on mobile
        Node filters = createCleaningFilters();

        // Desktop layout: Title on left, filters on right
        HBox desktopHeader = new HBox();
        desktopHeader.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        desktopHeader.getChildren().addAll(titleLeft, spacer, filters);

        // Mobile layout: Title and filters stacked vertically
        VBox mobileHeader = new VBox(8);
        mobileHeader.setAlignment(Pos.CENTER_LEFT);

        // Section header container
        StackPane sectionHeader = new StackPane(desktopHeader, mobileHeader);
        sectionHeader.getStyleClass().add("section-title");
        sectionHeader.setPadding(new Insets(8, 0, 8, 0));

        // Setup responsive design
        new ResponsiveDesign(sectionHeader)
            .addResponsiveLayout(
                width -> width < MOBILE_BREAKPOINT,
                () -> {
                    desktopHeader.setVisible(false);
                    desktopHeader.setManaged(false);
                    mobileHeader.setVisible(true);
                    mobileHeader.setManaged(true);
                    desktopHeader.getChildren().clear();
                    mobileHeader.getChildren().clear();
                    mobileHeader.getChildren().addAll(titleLeft, filters);
                }
            )
            .addResponsiveLayout(
                width -> width >= MOBILE_BREAKPOINT,
                () -> {
                    mobileHeader.setVisible(false);
                    mobileHeader.setManaged(false);
                    desktopHeader.setVisible(true);
                    desktopHeader.setManaged(true);
                    mobileHeader.getChildren().clear();
                    desktopHeader.getChildren().clear();
                    Region newSpacer = new Region();
                    HBox.setHgrow(newSpacer, Priority.ALWAYS);
                    desktopHeader.getChildren().addAll(titleLeft, newSpacer, filters);
                }
            )
            .start();

        if (cards.isEmpty()) {
            section.getChildren().addAll(sectionHeader, createEmptyState());
        } else {
            FlowPane listPane = new FlowPane();
            listPane.setHgap(10);
            listPane.setVgap(10);
            listPane.setRowValignment(VPos.TOP);
            listPane.getChildren().addAll(cards.stream()
                    .map(cardFactory::createRoomCard)
                    .collect(Collectors.toList()));
            section.getChildren().addAll(sectionHeader, listPane);
        }

        return section;
    }

    private Node createInspectionSection(List<RoomCardData> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("inspection-section");

        // Title row with count
        HBox titleLeft = new HBox(8);
        titleLeft.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.RoomsToInspect);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleLeft.getChildren().addAll(titleLabel, countLabel);

        // Filters - use FlowPane for wrapping on mobile
        Node filters = createInspectionFilters();

        // Desktop layout: Title on left, filters on right
        HBox desktopHeader = new HBox();
        desktopHeader.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        desktopHeader.getChildren().addAll(titleLeft, spacer, filters);

        // Mobile layout: Title and filters stacked vertically
        VBox mobileHeader = new VBox(8);
        mobileHeader.setAlignment(Pos.CENTER_LEFT);

        // Section header container
        StackPane sectionHeader = new StackPane(desktopHeader, mobileHeader);
        sectionHeader.getStyleClass().add("section-title");
        sectionHeader.setPadding(new Insets(8, 0, 8, 0));

        // Setup responsive design
        new ResponsiveDesign(sectionHeader)
            .addResponsiveLayout(
                width -> width < MOBILE_BREAKPOINT,
                () -> {
                    desktopHeader.setVisible(false);
                    desktopHeader.setManaged(false);
                    mobileHeader.setVisible(true);
                    mobileHeader.setManaged(true);
                    desktopHeader.getChildren().clear();
                    mobileHeader.getChildren().clear();
                    mobileHeader.getChildren().addAll(titleLeft, filters);
                }
            )
            .addResponsiveLayout(
                width -> width >= MOBILE_BREAKPOINT,
                () -> {
                    mobileHeader.setVisible(false);
                    mobileHeader.setManaged(false);
                    desktopHeader.setVisible(true);
                    desktopHeader.setManaged(true);
                    mobileHeader.getChildren().clear();
                    desktopHeader.getChildren().clear();
                    Region newSpacer = new Region();
                    HBox.setHgrow(newSpacer, Priority.ALWAYS);
                    desktopHeader.getChildren().addAll(titleLeft, newSpacer, filters);
                }
            )
            .start();

        if (cards.isEmpty()) {
            section.getChildren().addAll(sectionHeader, createEmptyState());
        } else {
            FlowPane listPane = new FlowPane();
            listPane.setHgap(10);
            listPane.setVgap(10);
            listPane.setRowValignment(VPos.TOP);
            listPane.getChildren().addAll(cards.stream()
                    .map(cardFactory::createRoomCard)
                    .collect(Collectors.toList()));
            section.getChildren().addAll(sectionHeader, listPane);
        }

        return section;
    }

    private Node createCheckoutsSection(List<CheckoutCardData> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("checkouts-section");

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.Checkouts);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.setRowValignment(VPos.TOP);
        listPane.getChildren().addAll(cards.stream()
                .map(cardFactory::createCheckoutCard)
                .collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createPartialCheckoutsSection(List<PartialCheckoutCardData> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("partial-checkouts-section");

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.PartialCheckouts);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.setRowValignment(VPos.TOP);
        listPane.getChildren().addAll(cards.stream()
                .map(cardFactory::createPartialCheckoutCard)
                .collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createArrivalsSection(List<ArrivalCardData> cards, String sectionTitle) {
        VBox section = new VBox(12);
        section.getStyleClass().add("arrivals-section");

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(sectionTitle);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.setRowValignment(VPos.TOP);
        listPane.getChildren().addAll(cards.stream()
                .map(cardFactory::createArrivalCard)
                .collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createCleaningFilters() {
        // Use FlowPane: stays on one line when enough space (desktop), wraps on mobile
        FlowPane filters = new FlowPane();
        filters.getStyleClass().add("filters");
        filters.setHgap(16);
        filters.setVgap(8);
        filters.setAlignment(Pos.CENTER_LEFT);

        DashboardFilterManager filterManager = presenter.getFilterManager();

        filters.getChildren().addAll(
                createFilterDropdown(HouseholdI18nKeys.Building, filterManager.cleanBuildingFilterProperty(),
                        new String[]{"All", "A", "B", "C"}),
                createFilterDropdown(HouseholdI18nKeys.Next, filterManager.cleanCheckInFilterProperty(),
                        new String[]{"All", "Today", "Tomorrow"})
        );

        return filters;
    }

    private Node createInspectionFilters() {
        // Use FlowPane: stays on one line when enough space (desktop), wraps on mobile
        FlowPane filters = new FlowPane();
        filters.getStyleClass().add("filters");
        filters.setHgap(16);
        filters.setVgap(8);
        filters.setAlignment(Pos.CENTER_LEFT);

        DashboardFilterManager filterManager = presenter.getFilterManager();

        filters.getChildren().addAll(
                createFilterDropdown(HouseholdI18nKeys.Building, filterManager.inspectBuildingFilterProperty(),
                        new String[]{"All", "A", "B", "C"}),
                createFilterDropdown(HouseholdI18nKeys.Next, filterManager.inspectCheckInFilterProperty(),
                        new String[]{"All", "Today", "Tomorrow"})
        );

        return filters;
    }

    private Node createFilterDropdown(Object labelKey, javafx.beans.property.ObjectProperty<String> filterProperty, String[] options) {
        HBox filterBox = new HBox(4);
        filterBox.getStyleClass().add("filter-box");
        filterBox.setAlignment(Pos.CENTER_LEFT);

        // Handle both i18n keys and plain strings
        Label label = labelKey instanceof String ? new Label((String) labelKey) : I18nControls.newLabel(labelKey);
        label.getStyleClass().add("filter-label");

        // Use toggle buttons instead of ChoiceBox for GWT compatibility
        HBox buttonGroup = new HBox(2);
        buttonGroup.setAlignment(Pos.CENTER_LEFT);

        List<Button> filterButtons = new ArrayList<>();
        for (String option : options) {
            Button btn = new Button(option);
            btn.getStyleClass().add("filter-toggle-btn");
            btn.setMinWidth(50);
            btn.setPadding(new Insets(4, 8, 4, 8));

            // Set initial active state
            boolean isActive = option.equals(filterProperty.get() != null ? filterProperty.get() : options[0]);
            updateFilterToggleStyle(btn, isActive);

            btn.setOnAction(e -> {
                filterProperty.set(option);
                // Update all button styles
                filterButtons.forEach(b -> updateFilterToggleStyle(b, b == btn));
            });

            filterButtons.add(btn);
            buttonGroup.getChildren().add(btn);
        }

        // Listen for external changes to the filter property
        filterProperty.addListener((obs, oldVal, newVal) -> {
            for (int i = 0; i < options.length; i++) {
                updateFilterToggleStyle(filterButtons.get(i), options[i].equals(newVal));
            }
        });

        filterBox.getChildren().addAll(label, buttonGroup);
        return filterBox;
    }

    private void updateFilterToggleStyle(Button btn, boolean isActive) {
        if (isActive) {
            btn.getStyleClass().removeAll("filter-toggle-inactive");
            if (!btn.getStyleClass().contains("filter-toggle-active")) {
                btn.getStyleClass().add("filter-toggle-active");
            }
        } else {
            btn.getStyleClass().removeAll("filter-toggle-active");
            if (!btn.getStyleClass().contains("filter-toggle-inactive")) {
                btn.getStyleClass().add("filter-toggle-inactive");
            }
        }
    }

    private Node createEmptyState() {
        VBox emptyState = new VBox(8);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(20));

        Label icon = new Label("✓"); // ✓
        icon.getStyleClass().add("empty-state-icon");

        Label message = I18nControls.newLabel(HouseholdI18nKeys.QuietMoment);
        message.getStyleClass().add("empty-state-message");

        emptyState.getChildren().addAll(icon, message);
        return emptyState;
    }

    private Node createEmptyDayState(LocalDate date) {
        VBox daySection = new VBox(12);
        daySection.getStyleClass().addAll("day-section", "empty-day");
        daySection.setPadding(new Insets(16, 32, 16, 32));

        HBox dayHeader = new HBox(16);
        dayHeader.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("d")));
        dateLabel.getStyleClass().add("day-date");

        Label dayNameLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")));
        dayNameLabel.getStyleClass().add("day-name");

        Label noActivityLabel = I18nControls.newLabel(HouseholdI18nKeys.NothingScheduled);
        noActivityLabel.getStyleClass().add("no-activity");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dayHeader.getChildren().addAll(dateLabel, dayNameLabel, spacer, noActivityLabel);
        daySection.getChildren().add(dayHeader);

        return daySection;
    }
}
