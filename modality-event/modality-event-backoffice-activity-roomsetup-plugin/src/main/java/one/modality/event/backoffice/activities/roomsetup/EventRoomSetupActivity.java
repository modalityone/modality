package one.modality.event.backoffice.activities.roomsetup;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;

import java.util.List;

/**
 * Event Room Setup Activity - Manages room allocation and configuration for events.
 *
 * This activity provides three tabs:
 * 1. Select Rooms - Assign rooms from source pools to booking categories
 * 2. Customize - Event-specific room overrides (beds, gender, type)
 * 3. Summary - Review totals and room assignments
 *
 * Architecture:
 * - Uses a shared EventRoomSetupDataModel that loads all entities once
 * - All three tabs reference the same shared ObservableLists
 * - Eliminates duplicate queries across tabs (53% reduction)
 * - Single UpdateStore for all mutations
 *
 * @author Bruno Salmon
 */
final class EventRoomSetupActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {

    // Shared data model - loads all entities once, shared by all tabs
    private final EventRoomSetupDataModel dataModel = new EventRoomSetupDataModel();

    // Tab views
    private final SelectRoomsTabView selectRoomsTabView = new SelectRoomsTabView();
    private final CustomizeTabView customizeTabView = new CustomizeTabView();
    private final SummaryTabView summaryTabView = new SummaryTabView();

    // Main container and tabs bar
    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, this::changeTabSelection);

    @Override
    public Node buildUi() {
        // Create tabs for header
        headerTabsBar.setTabs(
            headerTabsBar.createTab(EventRoomSetupI18nKeys.SelectRoomsTabTitle, selectRoomsTabView::buildContainer),
            headerTabsBar.createTab(EventRoomSetupI18nKeys.CustomizeTabTitle, customizeTabView::buildContainer),
            headerTabsBar.createTab(EventRoomSetupI18nKeys.SummaryTabTitle, summaryTabView::buildContainer)
        );
        return container;
    }

    /**
     * Called when a tab is selected. Updates the center content.
     */
    private void changeTabSelection(Node tabContent) {
        container.setCenter(tabContent);
        updateTabsActiveProperties();
    }

    /**
     * Updates the active property of each tab view based on activity state and tab selection.
     */
    private void updateTabsActiveProperties() {
        List<Tab> tabs = headerTabsBar.getTabs();
        boolean activityActive = isActive();
        selectRoomsTabView.setActive(activityActive && tabs.get(0).isSelected());
        customizeTabView.setActive(activityActive && tabs.get(1).isSelected());
        summaryTabView.setActive(activityActive && tabs.get(2).isSelected());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Display tabs in the main frame header
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        // Show event selector
        FXEventSelector.showEventSelector();
        updateTabsActiveProperties();
    }

    @Override
    public void onPause() {
        // Reset header tabs to default
        FXMainFrameHeaderTabs.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause(); // This changes the active property
        updateTabsActiveProperties();
    }

    @Override
    protected void startLogic() {
        // Initialize shared data model - loads all entities once
        dataModel.startLogic(this, this);

        // Pass shared data model to all tab views
        selectRoomsTabView.startLogic(dataModel, this);
        customizeTabView.startLogic(dataModel, this);
        summaryTabView.startLogic(dataModel, this);
    }
}
