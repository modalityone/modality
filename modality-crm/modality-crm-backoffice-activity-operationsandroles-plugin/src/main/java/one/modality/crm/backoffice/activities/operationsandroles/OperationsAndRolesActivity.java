package one.modality.crm.backoffice.activities.operationsandroles;

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
 *  Activity with 5 tabs: operations, routes, rules, groups, and roles
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class OperationsAndRolesActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        Label descriptionLabel = I18nControls.newLabel(OperationsAndRolesI18nKeys.ManageOrganizationsOperationsRoutesRoles);
        descriptionLabel.getStyleClass().add("admin-subtitle");

        OperationsView operationsView = new OperationsView(false);
        Tab operationsTab = createTab(OperationsAndRolesI18nKeys.Operations, operationsView.getView());

        OperationsView routesView = new OperationsView(true);
        Tab routesTab = createTab(OperationsAndRolesI18nKeys.Routes, routesView.getView());

        AuthorizationRulesView rulesView = new AuthorizationRulesView();
        Tab rulesTab = createTab(OperationsAndRolesI18nKeys.Rules, rulesView.getView());

        OperationGroupsView operationGroupsView = new OperationGroupsView();
        Tab operationGroupsTab = createTab(OperationsAndRolesI18nKeys.OperationGroups, operationGroupsView.getView());

        RolesView rolesView = new RolesView();
        Tab rolesTab = createTab(OperationsAndRolesI18nKeys.Roles, rolesView.getView());

        // Create TabPane with sub-tabs
        TabPane tabPane = new TabPane(
            operationsTab,
            routesTab,
            rulesTab,
            operationGroupsTab,
            rolesTab
        );
        tabPane.getStyleClass().add("super-admin-sub-tabs");

        VBox view = new VBox(16,
            descriptionLabel,
            tabPane
        );
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        view.setPadding(new Insets(24));
        view.setAlignment(Pos.TOP_LEFT);

        // Set up tab selection listener for lazy activation
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean active = isActive();
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            operationsView.setActive(active && selectedTab == operationsTab);
            routesView.setActive(active && selectedTab == routesTab);
            rulesView.setActive(active && selectedTab == rulesTab);
            operationGroupsView.setActive(active && selectedTab == operationGroupsTab);
            rolesView.setActive(active && selectedTab == rolesTab);
        }, activeProperty(), tabPane.getSelectionModel().selectedItemProperty());

        return view;
    }

    private static Tab createTab(Object i18nKey, Node content) {
        Tab tab = new Tab(null, wrapWithPadding(content));
        I18nControls.bindI18nTextProperty(tab, i18nKey);
        tab.setClosable(false);
        return tab;
    }

    private static Node wrapWithPadding(Node content) {
        MonoPane wrapper = new MonoPane(content);
        wrapper.setPadding(new Insets(24));
        return wrapper;
    }
}
