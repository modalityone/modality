package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.Site;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.data.SiteComparisonDataLoader;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model.EventSiteComparison;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model.ResourceLink;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.presenter.SiteComparisonPresenter;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View for the Site Comparison tab.
 * Displays a side-by-side comparison of accommodation resource names
 * between the global site and event sites.
 * Supports linking event resources to global resources.
 *
 * LAZY LOADING VERSION:
 * - All sections start collapsed
 * - Resources are loaded on-demand when a section is expanded
 * - Progress indicators shown during loading
 *
 * @author Claude Code
 */
public final class SiteComparisonView {

    private final RoomSetupPresentationModel pm;
    private final SiteComparisonPresenter presenter;
    private SiteComparisonDataLoader dataLoader;
    private ObservableValue<Boolean> activeProperty;
    private DataSourceModel dataSourceModel;
    private Object mixin;

    // Lazy loading state
    private boolean loadingInitiated = false;
    private boolean startLogicCalled = false;

    // UI state - all sections start collapsed
    private Resource selectedEventResource = null;
    private Node selectedEventNode = null;
    private final Map<String, Boolean> expandedSections = new HashMap<>();

    // UI Components
    private VBox mainContainer;
    private VBox contentContainer;

    public SiteComparisonView(RoomSetupPresentationModel pm) {
        this.pm = pm;
        this.presenter = new SiteComparisonPresenter();
    }

    public Node buildView() {
        mainContainer = new VBox();
        mainContainer.setSpacing(16);
        mainContainer.setPadding(new Insets(20));

        // Header section
        VBox headerSection = createHeaderSection();

        // Content container
        contentContainer = new VBox();
        contentContainer.setSpacing(16);

        // Wrap content in scroll pane
        ScrollPane scrollPane = Controls.createVerticalScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContainer.getChildren().addAll(headerSection, scrollPane);

        // If startLogic was already called, initiate data loading now
        if (startLogicCalled && !loadingInitiated) {
            initiateDataLoading();
            Platform.runLater(this::renderView);
        }

        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox();
        header.setSpacing(16);
        header.setPadding(new Insets(0, 0, 16, 0));

        VBox titleBox = new VBox();
        titleBox.setSpacing(6);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.SiteComparison);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);
        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.SiteComparisonSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        header.getChildren().add(titleBox);
        return header;
    }

    public void startLogic(Object mixin) {
        this.mixin = mixin;
        this.startLogicCalled = true;

        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        if (mixin instanceof HasDataSourceModel) {
            dataSourceModel = ((HasDataSourceModel) mixin).getDataSourceModel();
        }

        if (mainContainer != null && !loadingInitiated) {
            initiateDataLoading();
            Platform.runLater(this::renderView);
        }
    }

    private void initiateDataLoading() {
        if (loadingInitiated) {
            return;
        }
        loadingInitiated = true;
        Console.log("[SiteComparisonView] Initiating data loading");

        dataLoader = new SiteComparisonDataLoader(pm);
        dataLoader.startLogic(mixin);

        // Listen for initial loading state changes
        FXProperties.runOnPropertiesChange(this::onLoadingStateChanged, dataLoader.loadingProperty());

        // Listen for event sites list changes
        dataLoader.getEventSites().addListener((ListChangeListener.Change<?> c) -> Platform.runLater(this::renderView));

        // Listen for global site/resources changes
        FXProperties.runOnPropertiesChange(this::onGlobalDataChanged, dataLoader.globalSiteProperty());
        dataLoader.getGlobalSiteResources().addListener((ListChangeListener.Change<?> c) -> onGlobalDataChanged());

        Platform.runLater(this::renderView);
    }

    private void onLoadingStateChanged() {
        Platform.runLater(this::renderView);
    }

    private void onGlobalDataChanged() {
        // Initialize presenter with global resources when they're loaded
        Site globalSite = dataLoader.globalSiteProperty().get();
        if (globalSite != null && !dataLoader.getGlobalSiteResources().isEmpty()) {
            presenter.initializeGlobalResources(globalSite, new ArrayList<>(dataLoader.getGlobalSiteResources()));
        }
        Platform.runLater(this::renderView);
    }

    private void clearSelection() {
        if (selectedEventNode != null) {
            selectedEventNode.getStyleClass().remove(UIComponentDecorators.CSS_COMPARISON_RESOURCE_SELECTED);
            if (selectedEventNode instanceof Label) {
                UIComponentDecorators.applyComparisonTextStyle((Label) selectedEventNode, "event");
            }
        }
        selectedEventResource = null;
        selectedEventNode = null;
    }

    private void renderView() {
        if (contentContainer == null) return;
        contentContainer.getChildren().clear();

        if (!loadingInitiated) {
            contentContainer.getChildren().add(createLoadingIndicator());
            return;
        }

        // Show loading indicator while initial data is being fetched
        if (dataLoader != null && dataLoader.isLoading()) {
            contentContainer.getChildren().add(createLoadingIndicator());
            return;
        }

        Site globalSite = dataLoader.globalSiteProperty().get();

        // Check for no global site
        if (globalSite == null) {
            contentContainer.getChildren().add(createWarningBox(RoomSetupI18nKeys.NoGlobalSiteConfigured));
            return;
        }

        // Check for no global resources
        if (dataLoader.getGlobalSiteResources().isEmpty()) {
            contentContainer.getChildren().add(createWarningBox(RoomSetupI18nKeys.NoResourcesInGlobalSite));
            return;
        }

        // Check for no event sites
        List<Site> eventSites = new ArrayList<>(dataLoader.getEventSites());
        if (eventSites.isEmpty()) {
            contentContainer.getChildren().add(createWarningBox(RoomSetupI18nKeys.NoEventSitesFound));
            return;
        }

        // Quick stats bar
        contentContainer.getChildren().add(createQuickStatsBar(globalSite, eventSites));

        // Expand/Collapse all buttons
        contentContainer.getChildren().add(createExpandCollapseButtons(eventSites));

        // Link instructions
        contentContainer.getChildren().add(createLinkInstructions());

        // Render each event site as collapsible section
        for (Site site : eventSites) {
            contentContainer.getChildren().add(createCollapsibleSiteSection(site));
        }

        // Info helper
        contentContainer.getChildren().add(createInfoHelper());
    }

    private Node createLoadingIndicator() {
        VBox loadingBox = new VBox();
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(16);
        loadingBox.setPadding(new Insets(60, 20, 60, 20));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(50, 50);

        Label loadingMessage = I18nControls.newLabel(RoomSetupI18nKeys.LoadingData);
        loadingMessage.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        loadingBox.getChildren().addAll(spinner, loadingMessage);
        return loadingBox;
    }

    /**
     * Creates a small loading indicator for use inside sections.
     */
    private Node createSectionLoadingIndicator() {
        HBox loadingBox = new HBox(12);
        loadingBox.setAlignment(Pos.CENTER_LEFT);
        loadingBox.setPadding(new Insets(20));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(24, 24);

        Label loadingMessage = I18nControls.newLabel(RoomSetupI18nKeys.LoadingData);
        loadingMessage.getStyleClass().add(UIComponentDecorators.CSS_SMALL);

        loadingBox.getChildren().addAll(spinner, loadingMessage);
        return loadingBox;
    }

    private Node createQuickStatsBar(Site globalSite, List<Site> eventSites) {
        HBox statsBar = new HBox(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(12, 16, 12, 16));
        statsBar.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        // Global site info
        String globalSiteName = globalSite.getName() != null ? globalSite.getName() : "Global Site";
        int globalResourceCount = dataLoader.getGlobalSiteResources().size();
        Label globalLabel = I18nControls.newLabel(RoomSetupI18nKeys.GlobalSiteRoomCount, globalSiteName, globalResourceCount);
        globalLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusCounts = new HBox(12);
        statusCounts.setAlignment(Pos.CENTER_RIGHT);

        Label sitesLabel = new Label(eventSites.size() + " event sites");
        sitesLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        statusCounts.getChildren().add(sitesLabel);

        statsBar.getChildren().addAll(globalLabel, spacer, statusCounts);
        return statsBar;
    }

    private String getSectionKey(Site site) {
        return site != null ? String.valueOf(site.getId()) : "unknown";
    }

    private Node createExpandCollapseButtons(List<Site> eventSites) {
        HBox buttonsBar = new HBox(8);
        buttonsBar.setAlignment(Pos.CENTER_LEFT);
        buttonsBar.setPadding(new Insets(8, 0, 8, 0));

        Button expandAll = Bootstrap.secondaryButton(I18nControls.newButton(RoomSetupI18nKeys.ExpandAll));
        expandAll.setOnAction(e -> {
            for (Site site : eventSites) {
                expandedSections.put(getSectionKey(site), true);
                // Trigger lazy load for each site
                Object siteId = site.getId().getPrimaryKey();
                if (!dataLoader.isSiteLoaded(siteId) && !dataLoader.isSiteLoading(siteId)) {
                    dataLoader.loadSiteResources(siteId, resources -> Platform.runLater(this::renderView));
                }
            }
            renderView();
        });

        Button collapseAll = Bootstrap.secondaryButton(I18nControls.newButton(RoomSetupI18nKeys.CollapseAll));
        collapseAll.setOnAction(e -> {
            for (Site site : eventSites) {
                expandedSections.put(getSectionKey(site), false);
            }
            renderView();
        });

        buttonsBar.getChildren().addAll(expandAll, collapseAll);
        return buttonsBar;
    }

    private Node createLinkInstructions() {
        HBox infoBox = Bootstrap.infoBox(new HBox());
        infoBox.setSpacing(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));

        Label icon = new Label("\u2139");

        Label text = I18nControls.newLabel(RoomSetupI18nKeys.SiteComparisonLinkInstructions);
        text.setWrapText(true);

        infoBox.getChildren().addAll(icon, text);
        return infoBox;
    }

    private Node createCollapsibleSiteSection(Site site) {
        VBox section = new VBox();
        section.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        String sectionKey = getSectionKey(site);
        Object siteId = site.getId().getPrimaryKey();

        // Check if expanded (default: collapsed)
        boolean isExpanded = expandedSections.getOrDefault(sectionKey, false);

        // Check loading/loaded state
        boolean isLoading = dataLoader.isSiteLoading(siteId);
        boolean isLoaded = dataLoader.isSiteLoaded(siteId);

        // Get comparison if loaded
        EventSiteComparison comparison = null;
        if (isLoaded) {
            List<Resource> siteResources = new ArrayList<>(dataLoader.getResourcesForSite(siteId));
            comparison = presenter.computeComparisonForSite(site, siteResources);
        }

        // Calculate status for styling (if loaded)
        int totalEventOnly = comparison != null ? comparison.eventOnlyResources().size() : 0;
        int confirmedCount = comparison != null ? comparison.confirmedLinks().size() : 0;

        // Clickable header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.getStyleClass().add(UIComponentDecorators.CSS_CLICKABLE);
        if (isExpanded) {
            header.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_HEADER);
        }

        // Expand/collapse indicator
        Label expandIcon = new Label(isExpanded ? "\u25BC" : "\u25B6");
        expandIcon.getStyleClass().add(UIComponentDecorators.CSS_EXPAND_ARROW);

        // Event name and dates
        HBox nameBox = new HBox(8);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Event siteEvent = site.getEvent();
        if (siteEvent != null) {
            String eventName = siteEvent.getName() != null ? siteEvent.getName() : site.getName();
            Label nameLabel = new Label(eventName);
            nameLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);
            nameBox.getChildren().add(nameLabel);

            if (siteEvent.getStartDate() != null) {
                String dateStr;
                if (siteEvent.getEndDate() != null) {
                    dateStr = "(" + siteEvent.getStartDate().format(DateTimeFormatter.ofPattern("d MMM")) +
                              " - " + siteEvent.getEndDate().format(DateTimeFormatter.ofPattern("d MMM yyyy")) + ")";
                } else {
                    dateStr = "(" + siteEvent.getStartDate().format(DateTimeFormatter.ofPattern("d MMM yyyy")) + ")";
                }
                Label dateLabel = new Label(dateStr);
                dateLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
                nameBox.getChildren().add(dateLabel);
            }
        } else {
            Label nameLabel = new Label(site.getName());
            nameLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);
            nameBox.getChildren().add(nameLabel);
        }

        header.getChildren().addAll(expandIcon, nameBox);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Status section (only show if loaded)
        VBox statusBox = new VBox(2);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        if (isLoaded && comparison != null) {
            String statusText;
            Label statusBadge;

            if (totalEventOnly == 0) {
                statusText = I18n.getI18nText(RoomSetupI18nKeys.StatusAllMatched);
                statusBadge = Bootstrap.successBadge(new Label(statusText));
            } else if (confirmedCount == totalEventOnly) {
                statusText = I18n.getI18nText(RoomSetupI18nKeys.StatusCompleteCount, confirmedCount, totalEventOnly);
                statusBadge = Bootstrap.successBadge(new Label(statusText));
            } else if (confirmedCount > 0) {
                statusText = I18n.getI18nText(RoomSetupI18nKeys.StatusInProgressCount, confirmedCount, totalEventOnly);
                statusBadge = Bootstrap.warningBadge(new Label(statusText));
            } else {
                statusText = I18n.getI18nText(RoomSetupI18nKeys.StatusNeedsLinking, totalEventOnly);
                statusBadge = Bootstrap.dangerBadge(new Label(statusText));
            }

            Label countsSummary = new Label(I18n.getI18nText(RoomSetupI18nKeys.ResourceSummary,
                    comparison.inBoth().size(),
                    comparison.globalOnlyResources().size(),
                    comparison.eventOnlyResources().size()));
            countsSummary.getStyleClass().add(UIComponentDecorators.CSS_SMALL);

            statusBox.getChildren().addAll(statusBadge, countsSummary);
        } else if (isLoading) {
            ProgressIndicator miniSpinner = new ProgressIndicator();
            miniSpinner.setPrefSize(16, 16);
            statusBox.getChildren().add(miniSpinner);
        } else {
            // Not loaded - show "click to load" indicator
            Label notLoadedLabel = new Label("...");
            notLoadedLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
            statusBox.getChildren().add(notLoadedLabel);
        }

        // Save button (only if loaded and has comparison)
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(RoomSetupI18nKeys.Save));

        if (isLoaded && comparison != null) {
            UpdateStore updateStore = presenter.getOrCreateUpdateStore(site);
            if (updateStore != null) {
                BooleanBinding hasNoChanges = new BooleanBinding() {
                    {
                        super.bind(EntityBindings.hasChangesProperty(updateStore));
                    }
                    @Override
                    protected boolean computeValue() {
                        return !updateStore.hasChanges();
                    }
                };
                saveButton.disableProperty().bind(hasNoChanges);
            } else {
                saveButton.setDisable(true);
            }

            saveButton.setOnAction(e -> {
                e.consume();
                presenter.saveChangesForSite(site, () -> {
                    presenter.clearComparisonCacheForSite(siteId);
                    Platform.runLater(this::renderView);
                });
            });
        } else {
            saveButton.setDisable(true);
        }
        saveButton.setOnMouseClicked(javafx.event.Event::consume);

        header.getChildren().addAll(headerSpacer, statusBox, saveButton);

        // Click to toggle
        header.setOnMouseClicked(e -> {
            if (e.getTarget() != saveButton) {
                boolean newExpanded = !isExpanded;
                expandedSections.put(sectionKey, newExpanded);

                // Trigger lazy load when expanding if not already loaded
                if (newExpanded && !isLoaded && !isLoading) {
                    dataLoader.loadSiteResources(siteId, resources -> Platform.runLater(this::renderView));
                }

                renderView();
            }
        });

        section.getChildren().add(header);

        // Content (only if expanded)
        if (isExpanded) {
            if (isLoading) {
                // Show loading indicator inside the section
                section.getChildren().add(createSectionLoadingIndicator());
            } else if (isLoaded && comparison != null) {
                // Show the comparison content
                VBox content = new VBox(16);
                content.setPadding(new Insets(16));
                content.getStyleClass().add(UIComponentDecorators.CSS_CARD_HEADER);

                // Links section
                if (!comparison.confirmedLinks().isEmpty()) {
                    content.getChildren().add(createLinksSection(comparison));
                }

                // Build badge map for confirmed links
                Map<Object, String> eventResourceBadges = new HashMap<>();
                Map<Object, String> globalResourceBadges = new HashMap<>();
                int badgeIndex = 0;
                String[] badgeColors = {"#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#00bcd4", "#009688", "#4caf50", "#ff9800", "#ff5722"};

                for (ResourceLink link : comparison.confirmedLinks()) {
                    String badge = String.valueOf((char) ('A' + badgeIndex));
                    String color = badgeColors[badgeIndex % badgeColors.length];
                    eventResourceBadges.put(link.eventResource().getId().getPrimaryKey(), badge + ":" + color);
                    globalResourceBadges.put(link.globalResource().getId().getPrimaryKey(), badge + ":" + color);
                    badgeIndex++;
                }

                // Columns
                HBox columns = new HBox(16);
                columns.setAlignment(Pos.TOP_LEFT);

                VBox inBothCol = createResourceColumn(RoomSetupI18nKeys.InBothSites, comparison.inBoth(), "both");
                VBox globalOnlyCol = createResourceColumnWithResources(RoomSetupI18nKeys.OnlyInGlobalSite, comparison.globalOnlyResources(),
                        "global", false, globalResourceBadges);
                VBox eventOnlyCol = createResourceColumnWithResources(RoomSetupI18nKeys.OnlyInEventSite, comparison.eventOnlyResources(),
                        "event", true, eventResourceBadges);

                HBox.setHgrow(inBothCol, Priority.ALWAYS);
                HBox.setHgrow(globalOnlyCol, Priority.ALWAYS);
                HBox.setHgrow(eventOnlyCol, Priority.ALWAYS);

                columns.getChildren().addAll(inBothCol, globalOnlyCol, eventOnlyCol);
                content.getChildren().add(columns);

                section.getChildren().add(content);
            }
        }

        return section;
    }

    private Node createLinksSection(EventSiteComparison comparison) {
        VBox linksSection = new VBox(8);
        linksSection.setPadding(new Insets(12));
        linksSection.getStyleClass().add(UIComponentDecorators.CSS_INFO_BAR);

        Label linksTitle = I18nControls.newLabel(RoomSetupI18nKeys.ResourceLinks);
        linksTitle.getStyleClass().add(UIComponentDecorators.CSS_CAPTION);
        linksSection.getChildren().add(linksTitle);

        for (ResourceLink link : comparison.confirmedLinks()) {
            linksSection.getChildren().add(createConfirmedLinkRow(link));
        }

        return linksSection;
    }

    private Node createConfirmedLinkRow(ResourceLink link) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));

        Label checkmark = Bootstrap.textSuccess(new Label("\u2713"));

        Label eventName = new Label(link.eventResourceName());
        UIComponentDecorators.applyComparisonTextStyle(eventName, "event");

        Label arrow = new Label("\u2192");

        Label globalName = new Label(link.globalResourceName());
        UIComponentDecorators.applyComparisonTextStyle(globalName, "global");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button unlinkBtn = new Button("\u2715");
        UIComponentDecorators.applyUnlinkButtonStyle(unlinkBtn);
        unlinkBtn.setOnAction(e -> {
            presenter.unlinkResource(link.eventResource());
            // Clear cache and re-render
            Object siteId = link.eventResource().getSite().getId().getPrimaryKey();
            presenter.clearComparisonCacheForSite(siteId);
            renderView();
        });

        row.getChildren().addAll(checkmark, eventName, arrow, globalName, spacer, unlinkBtn);
        return row;
    }

    private VBox createResourceColumnWithResources(Object titleKey, List<Resource> resources, String columnType, boolean isEventSide, Map<Object, String> badges) {
        VBox column = new VBox(8);
        UIComponentDecorators.applyComparisonColumnStyle(column, columnType);

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(titleKey);
        UIComponentDecorators.applyComparisonHeaderStyle(titleLabel, columnType);

        Label countLabel = new Label("(" + resources.size() + ")");
        UIComponentDecorators.applyComparisonTextStyle(countLabel, columnType);

        headerBox.getChildren().addAll(titleLabel, countLabel);
        column.getChildren().add(headerBox);

        if (resources.isEmpty()) {
            Label emptyLabel = new Label("-");
            emptyLabel.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE_TEXT);
            column.getChildren().add(emptyLabel);
        } else {
            for (Resource resource : resources) {
                HBox resourceRow = new HBox(6);
                resourceRow.setAlignment(Pos.CENTER_LEFT);

                String resourceName = resource.getName() != null ? resource.getName() : I18n.getI18nText(RoomSetupI18nKeys.Unnamed);
                Label resourceLabel = new Label(resourceName);

                boolean isLinked = presenter.isLinked(resource);
                boolean isSelected = selectedEventResource != null && selectedEventResource.equals(resource);

                if (isEventSide) {
                    applyEventResourceStyle(resourceLabel, isLinked, isSelected);
                    if (!isLinked) {
                        resourceRow.setOnMouseClicked(e -> selectEventResource(resource, resourceLabel));
                        resourceRow.getStyleClass().add(UIComponentDecorators.CSS_CLICKABLE);
                    }
                } else {
                    applyGlobalResourceStyle(resourceLabel, isLinked);
                    resourceRow.setOnMouseClicked(e -> linkSelectedToGlobal(resource));
                    resourceRow.getStyleClass().add(UIComponentDecorators.CSS_CLICKABLE);
                }

                resourceRow.getChildren().add(resourceLabel);

                Object resourceId = resource.getId().getPrimaryKey();
                String badgeInfo = badges.get(resourceId);
                if (badgeInfo != null) {
                    String[] parts = badgeInfo.split(":");
                    String badgeLetter = parts[0];
                    String badgeColor = parts[1];

                    Label badge = new Label("[" + badgeLetter + "]");
                    UIComponentDecorators.applyLinkBadgeStyle(badge, badgeColor);
                    resourceRow.getChildren().add(badge);
                }

                column.getChildren().add(resourceRow);
            }
        }

        return column;
    }

    private void applyEventResourceStyle(Label label, boolean isLinked, boolean isSelected) {
        if (isSelected) {
            UIComponentDecorators.applySelectedResourceStyle(label);
        } else if (isLinked) {
            UIComponentDecorators.applyLinkedResourceStyle(label);
        } else {
            UIComponentDecorators.applyComparisonTextStyle(label, "event");
        }
    }

    private void applyGlobalResourceStyle(Label label, boolean isLinked) {
        if (isLinked) {
            UIComponentDecorators.applyLinkedResourceStyle(label);
        } else {
            UIComponentDecorators.applyComparisonTextStyle(label, "global");
        }
    }

    private void selectEventResource(Resource resource, Label label) {
        clearSelection();
        selectedEventResource = resource;
        selectedEventNode = label;
        UIComponentDecorators.applySelectedResourceStyle(label);
    }

    private void linkSelectedToGlobal(Resource globalResource) {
        if (selectedEventResource != null) {
            presenter.linkResources(selectedEventResource, globalResource);
            // Clear cache for the site and re-render
            Object siteId = selectedEventResource.getSite().getId().getPrimaryKey();
            presenter.clearComparisonCacheForSite(siteId);
            clearSelection();
            renderView();
        }
    }

    private VBox createResourceColumn(Object titleKey, List<String> resources, String columnType) {
        VBox column = new VBox(8);
        UIComponentDecorators.applyComparisonColumnStyle(column, columnType);

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(titleKey);
        UIComponentDecorators.applyComparisonHeaderStyle(titleLabel, columnType);

        Label countLabel = new Label("(" + resources.size() + ")");
        UIComponentDecorators.applyComparisonTextStyle(countLabel, columnType);

        headerBox.getChildren().addAll(titleLabel, countLabel);
        column.getChildren().add(headerBox);

        if (resources.isEmpty()) {
            Label emptyLabel = new Label("-");
            emptyLabel.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE_TEXT);
            column.getChildren().add(emptyLabel);
        } else {
            for (String resource : resources) {
                Label resourceLabel = new Label(resource);
                UIComponentDecorators.applyComparisonTextStyle(resourceLabel, columnType);
                column.getChildren().add(resourceLabel);
            }
        }

        return column;
    }

    private Node createWarningBox(Object messageKey) {
        VBox warningBox = new VBox();
        warningBox.setAlignment(Pos.CENTER);
        warningBox.setSpacing(12);
        UIComponentDecorators.applyWarningBoxStyle(warningBox);

        Label warningIcon = new Label("!");
        UIComponentDecorators.applyWarningIconStyle(warningIcon);

        Label message = I18nControls.newLabel(messageKey);
        UIComponentDecorators.applyWarningTextStyle(message);

        warningBox.getChildren().addAll(warningIcon, message);
        return warningBox;
    }

    private HBox createInfoHelper() {
        HBox infoBox = Bootstrap.infoBox(new HBox());
        infoBox.setSpacing(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));
        infoBox.getStyleClass().add(UIComponentDecorators.CSS_INFO_BAR);
        VBox.setMargin(infoBox, new Insets(34, 0, 0, 0));

        Label iconLabel = new Label("\uD83D\uDCA1");

        Label textLabel = I18nControls.newLabel(RoomSetupI18nKeys.SiteComparisonInfoHelper);
        textLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
        textLabel.setWrapText(true);

        infoBox.getChildren().addAll(iconLabel, textLabel);
        return infoBox;
    }
}
