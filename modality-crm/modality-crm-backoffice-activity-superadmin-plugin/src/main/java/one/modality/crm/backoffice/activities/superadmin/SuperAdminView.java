package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import static one.modality.crm.backoffice.activities.superadmin.SuperAdmin18nKeys.*;

/**
 * Super Admin View containing 6 sub-tabs for comprehensive rights management:
 * - Organizations: Assign admins to organizations
 * - Operations: Manage individual operations
 * - Operation Groups: Manage groups of related operations
 * - Routes: Manage application routes
 * - Roles: Manage user roles and permissions
 * - Rules: Manage authorization rules
 *
 * @author Claude Code
 */
public class SuperAdminView {

    private final AssignAdminToOrganizationsView organizationsView = new AssignAdminToOrganizationsView();
    private final OperationsView operationsView = new OperationsView(false);  // false = operations
    private final OperationGroupsView operationGroupsView = new OperationGroupsView();
    private final OperationsView routesView = new OperationsView(true);  // true = routes
    private final RolesView rolesView = new RolesView();
    private final AuthorizationRulesView rulesView = new AuthorizationRulesView();

    private final VBox view;
    private final TabPane tabPane;

    public SuperAdminView() {
        view = new VBox();
        view.setSpacing(16);
        view.setPadding(new Insets(24));
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);

        // Title and description
        Label titleLabel = I18nControls.newLabel(RightsManagement);
        titleLabel.getStyleClass().add("admin-title");

        Label descriptionLabel = I18nControls.newLabel(ManageOrganizationsOperationsRoutesRoles);
        descriptionLabel.getStyleClass().add("admin-subtitle");

        // Create sub-tabs
        Tab organizationsTab = new Tab(null, organizationsView.getView());
        I18nControls.bindI18nTextProperty(organizationsTab, OrganizationTab);
        organizationsTab.setClosable(false);

        Tab operationsTab = new Tab(null, operationsView.getView());
        I18nControls.bindI18nTextProperty(operationsTab, Operations);
        operationsTab.setClosable(false);

        Tab operationGroupsTab = new Tab(null, operationGroupsView.getView());
        I18nControls.bindI18nTextProperty(operationGroupsTab, OperationGroups);
        operationGroupsTab.setClosable(false);

        Tab routesTab = new Tab(null, routesView.getView());
        I18nControls.bindI18nTextProperty(routesTab, Routes);
        routesTab.setClosable(false);

        Tab rolesTab = new Tab(null, rolesView.getView());
        I18nControls.bindI18nTextProperty(rolesTab, Roles);
        rolesTab.setClosable(false);

        Tab rulesTab = new Tab(null, rulesView.getView());
        I18nControls.bindI18nTextProperty(rulesTab, Rules);
        rulesTab.setClosable(false);

        // Create TabPane with sub-tabs
        tabPane = new TabPane(organizationsTab, operationsTab, operationGroupsTab, routesTab, rolesTab, rulesTab);
        tabPane.getStyleClass().add("super-admin-sub-tabs");

        view.getChildren().addAll(titleLabel, descriptionLabel, tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
    }

    public Node getView() {
        return view;
    }

    public void setActive(boolean active) {
        if (active) {
            // Activate the currently selected sub-tab
            int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
            switch (selectedIndex) {
                case 0 -> organizationsView.setActive(true);
                case 1 -> operationsView.setActive(true);
                case 2 -> operationGroupsView.setActive(true);
                case 3 -> routesView.setActive(true);
                case 4 -> rolesView.setActive(true);
                case 5 -> rulesView.setActive(true);
            }
        } else {
            // Deactivate all
            organizationsView.setActive(false);
            operationsView.setActive(false);
            operationGroupsView.setActive(false);
            routesView.setActive(false);
            rolesView.setActive(false);
            rulesView.setActive(false);
        }
    }
}
