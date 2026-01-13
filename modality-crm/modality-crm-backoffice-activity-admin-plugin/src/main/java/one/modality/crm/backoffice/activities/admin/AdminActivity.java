package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;

import java.util.List;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Rights Management activity with two main views:
 * 1. Super Admin View - Comprehensive rights management (Organizations,
 * Operations, Routes, Roles)
 * 2. Manage Users - User management for organization managers
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class AdminActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final AssignUserAndRoleToOrganizationView userManagementView = new AssignUserAndRoleToOrganizationView(
            this);
    private final ScheduledItemGenerationView scheduledItemGenerationView = new ScheduledItemGenerationView();
    private final LabelEditorView labelEditorView = new LabelEditorView(this);

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> mainTabsBar = new TabsBar<>(this, this::changeMainTabSelection);

    public AdminActivity() {
        System.out.println();
    }

    @Override
    public Node buildUi() {
        mainTabsBar.setTabs(
                mainTabsBar.createTab(ManageUsers, userManagementView::getView),
                mainTabsBar.createTab("Scheduled Items", scheduledItemGenerationView::getView),
                mainTabsBar.createTab(LabelEditor, labelEditorView::getView));
        return container;
    }

    private void changeMainTabSelection(Node tabContent) {
        container.setCenter(tabContent);
        updateAllTabsActiveProperties();
    }

    private void updateAllTabsActiveProperties() {
        List<Tab> mainTabs = mainTabsBar.getTabs();
        boolean activityActive = isActive();
        boolean manageUsersActive = activityActive && mainTabs.get(0).isSelected();
        boolean scheduledItemsActive = activityActive && mainTabs.size() > 1 && mainTabs.get(1).isSelected();
        boolean labelEditorActive = activityActive && mainTabs.size() > 2 && mainTabs.get(2).isSelected();

        userManagementView.setActive(manageUsersActive);
        scheduledItemGenerationView.setActive(scheduledItemsActive);
        labelEditorView.setActive(labelEditorActive);
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(mainTabsBar.getTabs());
        updateAllTabsActiveProperties();
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        super.onPause();
        updateAllTabsActiveProperties();
    }

}
