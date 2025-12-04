package one.modality.crm.backoffice.activities.administrators;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.AuthorizationOrganizationAdmin;
import one.modality.base.shared.entities.AuthorizationOrganizationUserAccess;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author David Hello
 * @author Claude Code
 */
final class AdministratorsActivity extends ViewDomainActivityBase {

    private final VBox view = new VBox();
    private final VisualGrid organizationsGrid = VisualGrid.createVisualGridWithTableLayoutSkin();;
    private TextField searchField;
    private Switch showAllSwitch;
    private final ObservableList<Organization> organizationsFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationOrganizationAdmin> organizationAdminsFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationOrganizationUserAccess> userAccessFeed = FXCollections.observableArrayList();
    private final ObservableList<Organization> displayedOrganizations = FXCollections.observableArrayList();
    private EntityColumn<Organization>[] columns;

    @Override
    public Node buildUi() {
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);
        organizationsGrid.setMinRowHeight(40);
        organizationsGrid.setPrefRowHeight(40);
        organizationsGrid.setPrefHeight(600);
        organizationsGrid.setMaxHeight(Double.MAX_VALUE);
        organizationsGrid.setMinWidth(0);
        organizationsGrid.setPrefWidth(Double.MAX_VALUE);
        organizationsGrid.setMaxWidth(Double.MAX_VALUE);
        organizationsGrid.setAppContext(this); // Pass this activity to renderers

        // Card container
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        Label sectionTitle = I18nControls.newLabel(AdministratorsI18nKeys.OrganizationsAndManagers);
        sectionTitle.getStyleClass().add("section-title");

        // Search area
        HBox searchArea = createSearchArea();

        // Add all to card
        card.getChildren().addAll(sectionTitle, searchArea, organizationsGrid);
        VBox.setVgrow(organizationsGrid, Priority.ALWAYS);

        view.getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Update displayed organizations when organizations feed, org admins, search text, or switch changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedOrganizations,
            ObservableLists.versionNumber(organizationsFeed),
            ObservableLists.versionNumber(organizationAdminsFeed),
            searchField.textProperty(),
            showAllSwitch.selectedProperty()
        );

        // Update grid when displayed organizations change or when org admins or user access changes
        Runnable updateGrid = () -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedOrganizations, columns);
            organizationsGrid.setVisualResult(vr);
        };

        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), displayedOrganizations);
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), organizationAdminsFeed);
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), userAccessFeed);

        return view;
    }

    public Node getView() {
        return view;
    }

    private HBox createSearchArea() {
        HBox searchArea = new HBox(12);
        searchArea.setAlignment(Pos.CENTER_LEFT);

        // Create search field with icon
        searchField = new TextField();
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), AdministratorsI18nKeys.SearchOrganizations);

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        // Create toggle switch with label beside it
        HBox switchContainer = new HBox(8);
        switchContainer.setAlignment(Pos.CENTER_LEFT);

        showAllSwitch = new Switch();

        Label switchLabel = I18nControls.newLabel(AdministratorsI18nKeys.ShowOrganizationsWithoutManagers);
        switchLabel.getStyleClass().add("switch-label");

        switchContainer.getChildren().addAll(showAllSwitch, switchLabel);

        HBox.setHgrow(searchContainer, Priority.SOMETIMES);
        searchArea.getChildren().addAll(searchContainer, switchContainer);

        return searchArea;
    }

    @Override
    protected void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Register custom renderers
        AdministratorsRenderers.registerRenderers();
        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'name', label: 'Organization Name', minWidth: 200},
                {expression: 'this', label: 'Admin(s)', renderer: 'managersList', minWidth: 400},
                {expression: 'this', label: 'Active Users', renderer: 'activeUsers', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Actions', renderer: 'organizationActions', minWidth: 80, prefWidth: 100, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "Organization");

        // Query all organizations
        ReactiveEntitiesMapper.<Organization>createPushReactiveChain(this)
            .always("{class: 'Organization', alias: 'o', fields: 'name', orderBy: 'name'}")
            .storeEntitiesInto(organizationsFeed)
            .start();

        // Query all organization admins
        ReactiveEntitiesMapper.<AuthorizationOrganizationAdmin>createPushReactiveChain(this)
            .always("{class: 'AuthorizationOrganizationAdmin', alias: 'oa', fields: 'organization,admin.(firstName,lastName,email)', orderBy: 'organization,id'}")
            .storeEntitiesInto(organizationAdminsFeed)
            .start();

        // Query all user access to count active users per organization
        ReactiveEntitiesMapper.<AuthorizationOrganizationUserAccess>createPushReactiveChain(this)
            .always("{class: 'AuthorizationOrganizationUserAccess', alias: 'ua', fields: 'organization,user', orderBy: 'organization,id'}")
            .storeEntitiesInto(userAccessFeed)
            .start();
    }

    /**
     * Helper method for renderers to get user access count for a specific organization.
     */
    int getUserAccessCountForOrganization(Organization organization) {
        return (int) userAccessFeed.stream()
            .filter(ua -> ua.getOrganization() != null && ua.getOrganization().getPrimaryKey().equals(organization.getPrimaryKey()))
            .count();
    }

    private void updateDisplayedOrganizations() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";
        boolean showAll = showAllSwitch != null && showAllSwitch.isSelected();

        // Get all organizations and sort by name
        List<Organization> organizations = organizationsFeed.stream()
            .sorted(Comparator.comparing(Organization::getName, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());

        // Apply filters
        if (!searchText.isEmpty() || !showAll) {
            organizations = organizations.stream()
                .filter(org -> {
                    // Apply search filter
                    boolean matchesSearch = true;
                    if (!searchText.isEmpty()) {
                        boolean nameMatches = org.getName() != null && org.getName().toLowerCase().contains(searchText);
                        boolean managerMatches = getAdminsForOrganization(org).stream()
                            .anyMatch(admin -> {
                                Person person = admin.getAdmin();
                                if (person == null) return false;
                                String fullName = (person.getFirstName() + " " + person.getLastName()).toLowerCase();
                                String email = person.getEmail() != null ? person.getEmail().toLowerCase() : "";
                                return fullName.contains(searchText) || email.contains(searchText);
                            });
                        matchesSearch = nameMatches || managerMatches;
                    }

                    // Apply "show all" filter
                    boolean matchesFilter = showAll || hasManagers(org);

                    return matchesSearch && matchesFilter;
                })
                .collect(Collectors.toList());
        }
        // Update displayed organizations
        displayedOrganizations.setAll(organizations);
    }

    /**
     * Helper method for renderers to get admins for a specific organization.
     */
    List<AuthorizationOrganizationAdmin> getAdminsForOrganization(Organization organization) {
        return organizationAdminsFeed.stream()
            .filter(oa -> oa.getOrganization() != null && oa.getOrganization().getPrimaryKey().equals(organization.getPrimaryKey()))
            .collect(Collectors.toList());
    }

    /**
     * Check if an organization has managers.
     */
    private boolean hasManagers(Organization organization) {
        return organizationAdminsFeed.stream()
            .anyMatch(oa -> oa.getOrganization() != null && oa.getOrganization().getPrimaryKey().equals(organization.getPrimaryKey()));
    }

    void showManageManagersDialog(Organization organization) {
        List<AuthorizationOrganizationAdmin> admins = getAdminsForOrganization(organization);
        AssignAdminToOrganizationDialog.show(organization, admins);
    }

}
