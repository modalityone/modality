package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Super Admin Activity containing 6 sub-tabs for comprehensive rights management:
 * - Organizations: Assign admins to organizations
 * - Operations: Manage individual operations
 * - Routes: Manage application routes
 * - Rules: Manage authorization rules
 * - Operation Groups: Manage groups of related operations
 * - Roles: Manage user roles and permissions
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class SuperAdminActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        // Title and description
        Label titleLabel = I18nControls.newLabel(SuperAdmin18nKeys.RightsManagement);
        titleLabel.getStyleClass().add("admin-title");

        Label descriptionLabel = I18nControls.newLabel(SuperAdmin18nKeys.ManageOrganizationsOperationsRoutesRoles);
        descriptionLabel.getStyleClass().add("admin-subtitle");

        // Create sub-tabs with padded content
        AssignAdminToOrganizationsView organizationsView = new AssignAdminToOrganizationsView();
        Tab organizationsTab = createTab(SuperAdmin18nKeys.OrganizationTab, organizationsView.getView());

        OperationsView operationsView = new OperationsView(false);
        Tab operationsTab = createTab(SuperAdmin18nKeys.Operations, operationsView.getView());

        OperationsView routesView = new OperationsView(true);
        Tab routesTab = createTab(SuperAdmin18nKeys.Routes, routesView.getView());

        AuthorizationRulesView rulesView = new AuthorizationRulesView();
        Tab rulesTab = createTab(SuperAdmin18nKeys.Rules, rulesView.getView());

        OperationGroupsView operationGroupsView = new OperationGroupsView();
        Tab operationGroupsTab = createTab(SuperAdmin18nKeys.OperationGroups, operationGroupsView.getView());

        RolesView rolesView = new RolesView();
        Tab rolesTab = createTab(SuperAdmin18nKeys.Roles, rolesView.getView());

        // Create TabPane with sub-tabs
        TabPane tabPane = new TabPane(
            organizationsTab,
            operationsTab,
            routesTab,
            rulesTab,
            operationGroupsTab,
            rolesTab
        );
        tabPane.getStyleClass().add("super-admin-sub-tabs");

        VBox view = new VBox(16,
            titleLabel,
            descriptionLabel,
            tabPane
        );
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        view.setPadding(new Insets(24));
        view.setAlignment(Pos.TOP_LEFT);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean active = isActive();
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            organizationsView.setActive(active && selectedTab == organizationsTab);
            operationsView.setActive(active && selectedTab == operationsTab);
            routesView.setActive(active && selectedTab == routesTab);
            rulesView.setActive(active && selectedTab == rulesTab);
            operationGroupsView.setActive(active && selectedTab == operationGroupsTab);
            rolesView.setActive(active && selectedTab == rolesTab);
        }, activeProperty(), tabPane.getSelectionModel().selectedItemProperty());

        return view;
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
