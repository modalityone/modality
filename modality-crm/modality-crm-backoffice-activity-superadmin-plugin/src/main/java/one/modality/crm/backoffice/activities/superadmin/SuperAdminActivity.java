package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.TabsBar;

/**
 * Super Admin Activity with two main tabs:
 * - Rights Management: Sub-tabs for operations, routes, rules, groups, roles, and admin assignment
 * - Organizations: CRUD management for organizations
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class SuperAdminActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    // Lazy loading flags
    private boolean rightsManagementLogicStarted = false;
    //private boolean organizationsLogicStarted = false;

    // Views (created lazily)
    private RightsManagementView rightsManagementView;
    //private OrganizationsView organizationsView;

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
            headerTabsBar.createTab(I18n.getI18nText(SuperAdmin18nKeys.RightsManagementTab), this::buildRightsManagementTab, this::onRightsManagementTabSelected)
            //headerTabsBar.createTab(I18n.getI18nText(SuperAdmin18nKeys.OrganizationsTab), this::buildOrganizationsTab, this::onOrganizationsTabSelected)
        );
        return container;
    }

    /**
     * Builds the Rights Management tab content (called once, then cached).
     */
    private Node buildRightsManagementTab() {
        rightsManagementView = new RightsManagementView();
        return rightsManagementView.buildUi();
    }

    /**
     * Called every time the Rights Management tab is selected.
     */
    private void onRightsManagementTabSelected() {
        if (!rightsManagementLogicStarted && rightsManagementView != null) {
            rightsManagementView.startLogic(this);
            rightsManagementLogicStarted = true;
        }
    }

    /**
     * Called every time the Organizations tab is selected.
     */
    /*private void onOrganizationsTabSelected() {
        if (!organizationsLogicStarted && organizationsView != null) {
            // OrganizationsView starts its own logic in constructor, just set active
            organizationsView.setActive(true);
            organizationsLogicStarted = true;
        } else if (organizationsView != null) {
            organizationsView.setActive(true);
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        // Deactivate views when leaving
        if (rightsManagementView != null) {
            rightsManagementView.setActive(false);
        }
        /*if (organizationsView != null) {
            organizationsView.setActive(false);
        }*/
        super.onPause();
    }

    /**
     * Inner view class for Rights Management that contains all the sub-tabs.
     */
    static class RightsManagementView {
        private VBox view;
        private TabPane tabPane;

        // Sub-views
        private AssignAdminToOrganizationsView assignAdminsView;
        private OperationsView operationsView;
        private OperationsView routesView;
        private AuthorizationRulesView rulesView;
        private OperationGroupsView operationGroupsView;
        private RolesView rolesView;

        // Tabs
        private Tab assignAdminsTab;
        private Tab operationsTab;
        private Tab routesTab;
        private Tab rulesTab;
        private Tab operationGroupsTab;
        private Tab rolesTab;

        Node buildUi() {
            // Title and description
            Label titleLabel = I18nControls.newLabel(SuperAdmin18nKeys.RightsManagement);
            titleLabel.getStyleClass().add("admin-title");

            Label descriptionLabel = I18nControls.newLabel(SuperAdmin18nKeys.ManageOrganizationsOperationsRoutesRoles);
            descriptionLabel.getStyleClass().add("admin-subtitle");

            // Create sub-tabs with padded content
            assignAdminsView = new AssignAdminToOrganizationsView();
            assignAdminsTab = createTab(SuperAdmin18nKeys.OrganizationTab, assignAdminsView.getView());

            operationsView = new OperationsView(false);
            operationsTab = createTab(SuperAdmin18nKeys.Operations, operationsView.getView());

            routesView = new OperationsView(true);
            routesTab = createTab(SuperAdmin18nKeys.Routes, routesView.getView());

            rulesView = new AuthorizationRulesView();
            rulesTab = createTab(SuperAdmin18nKeys.Rules, rulesView.getView());

            operationGroupsView = new OperationGroupsView();
            operationGroupsTab = createTab(SuperAdmin18nKeys.OperationGroups, operationGroupsView.getView());

            rolesView = new RolesView();
            rolesTab = createTab(SuperAdmin18nKeys.Roles, rolesView.getView());

            // Create TabPane with sub-tabs
            tabPane = new TabPane(
                assignAdminsTab,
                operationsTab,
                routesTab,
                rulesTab,
                operationGroupsTab,
                rolesTab
            );
            tabPane.getStyleClass().add("super-admin-sub-tabs");

            view = new VBox(16,
                titleLabel,
                descriptionLabel,
                tabPane
            );
            VBox.setVgrow(tabPane, Priority.ALWAYS);
            view.setPadding(new Insets(24));
            view.setAlignment(Pos.TOP_LEFT);

            return view;
        }

        void startLogic(SuperAdminActivity activity) {
            // Set up tab selection listener for lazy activation
            FXProperties.runNowAndOnPropertiesChange(() -> {
                boolean active = activity.isActive();
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                assignAdminsView.setActive(active && selectedTab == assignAdminsTab);
                operationsView.setActive(active && selectedTab == operationsTab);
                routesView.setActive(active && selectedTab == routesTab);
                rulesView.setActive(active && selectedTab == rulesTab);
                operationGroupsView.setActive(active && selectedTab == operationGroupsTab);
                rolesView.setActive(active && selectedTab == rolesTab);
            }, activity.activeProperty(), tabPane.getSelectionModel().selectedItemProperty());
        }

        void setActive(boolean active) {
            if (!active) {
                // Deactivate all sub-views
                if (assignAdminsView != null) assignAdminsView.setActive(false);
                if (operationsView != null) operationsView.setActive(false);
                if (routesView != null) routesView.setActive(false);
                if (rulesView != null) rulesView.setActive(false);
                if (operationGroupsView != null) operationGroupsView.setActive(false);
                if (rolesView != null) rolesView.setActive(false);
            }
        }

        private Tab createTab(Object i18nKey, Node content) {
            Tab tab = new Tab(null, wrapWithPadding(content));
            I18nControls.bindI18nTextProperty(tab, i18nKey);
            tab.setClosable(false);
            return tab;
        }

        private Node wrapWithPadding(Node content) {
            MonoPane wrapper = new MonoPane(content);
            wrapper.setPadding(new Insets(24));
            return wrapper;
        }
    }
}
