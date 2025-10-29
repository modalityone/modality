package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.dql.query.ReactiveDqlQuery;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * User Management view for managers to assign roles to users within their organizations.
 *
 * @author Claude Code
 */
public class AssignUserAndRoleToOrganizationView {

    private final VBox view;
    private final VisualGrid userAccessGrid;
    private Label currentOrgLabel;
    private TextField searchField;
    private final ButtonFactoryMixin buttonFactory;
    private ReactiveEntitiesMapper<AuthorizationOrganizationUserAccess> userAccessMapper;
    private final ObservableList<AuthorizationOrganizationUserAccess> userAccess = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationOrganizationUserAccess> displayedUser = FXCollections.observableArrayList();
    private EntityColumn<AuthorizationOrganizationUserAccess>[] columns;

    static {
        // Register custom renderers
        AssignUserAndRoleToOrganizationViewRenderers.registerRenderers();
    }

    public AssignUserAndRoleToOrganizationView(ButtonFactoryMixin buttonFactory) {
        this.buttonFactory = buttonFactory;
        view = new VBox();
        view.setSpacing(16);
        view.setPadding(new Insets(24));
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);

        userAccessGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        userAccessGrid.setMinRowHeight(40);
        userAccessGrid.setPrefRowHeight(40);
        userAccessGrid.setPrefHeight(600);
        userAccessGrid.setMaxHeight(Double.MAX_VALUE);
        userAccessGrid.setMinWidth(0);
        userAccessGrid.setPrefWidth(Double.MAX_VALUE);
        userAccessGrid.setMaxWidth(Double.MAX_VALUE);

        // Pass this view to renderers
        AssignUserAndRoleToOrganizationViewRenderers.setUserManagementView(this);

        // Title and description
        Label titleLabel = I18nControls.newLabel(ManageUsers);
        titleLabel.getStyleClass().add("admin-title");

        Label descriptionLabel = I18nControls.newLabel(ManageUsersDescription);
        descriptionLabel.getStyleClass().add("admin-subtitle");

        // User management card
        VBox managementCard = createUserManagementCard();

        view.getChildren().addAll(titleLabel, descriptionLabel, managementCard);
        VBox.setVgrow(managementCard, Priority.ALWAYS);

        // Listen to organization changes
        FXOrganization.organizationProperty().addListener((obs, oldOrg, newOrg) -> {
            updateOrganizationLabel();
            if (userAccessMapper != null) {
                userAccessMapper.refreshWhenActive();
            }
        });

        // Set initial organization label
        updateOrganizationLabel();

        // Initialize database query
        startLogic();
    }

    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'this', label: 'User', renderer: 'userName', minWidth: 150},
                {expression: 'user.email', label: 'Email', minWidth: 250},
                {expression: 'role.name', label: 'Role', renderer: 'roleName', minWidth: 130},
                {expression: 'this', label: 'Scope', renderer: 'scopeName', minWidth: 200},
                {expression: 'readOnly', label: 'Access Type', renderer: 'accessType', minWidth: 110, prefWidth: 130, hShrink: false, textAlign: 'center'},
                {expression: 'date', label: 'Assigned', minWidth: 90, prefWidth: 110, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Actions', renderer: 'userActions', minWidth: 90, prefWidth: 100, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "AuthorizationOrganizationUserAccess");

        // Query user access records for the current organization
        userAccessMapper = ReactiveEntitiesMapper.<AuthorizationOrganizationUserAccess>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationOrganizationUserAccess', alias: 'ua', fields: 'user.(firstName,lastName,email),role.(id,name),event.(id,name),readOnly,date,organization', orderBy: 'user.firstName,user.lastName'}")
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(),
                orgId -> DqlStatement.where("organization=?", orgId))
            .storeEntitiesInto(userAccess)
            .start();

        // Update displayed user access when data or search changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedUserRoles,
            ObservableLists.versionNumber(userAccess),
            searchField.textProperty()
        );

        // Update grid when displayed user access changes
        ObservableLists.runNowAndOnListChange(change -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedUser, columns);
            userAccessGrid.setVisualResult(vr);
        }, displayedUser);
    }

    private void updateDisplayedUserRoles() {
        // Get search text
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Filter user access records
        List<AuthorizationOrganizationUserAccess> filtered = userAccess.stream()
            .filter(access -> {
                // If no search text, show all
                if (searchText.isEmpty()) {
                    return true;
                }

                // Match against user name
                Person user = access.getUser();
                if (user != null) {
                    String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                    if (fullName.contains(searchText)) {
                        return true;
                    }

                    // Match against user email
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                    if (email.contains(searchText)) {
                        return true;
                    }
                }

                // Match against role name
                AuthorizationRole role = access.getRole();
                if (role != null && role.getName() != null) {
                    if (role.getName().toLowerCase().contains(searchText)) {
                        return true;
                    }
                }

                // Match against event name in scope
                Event event = access.getEvent();
                if (event != null && event.getName() != null) {
                    return event.getName().toLowerCase().contains(searchText);
                }

                return false;
            })
            .sorted(Comparator.comparing(access -> {
                Person user = access.getUser();
                return user != null ? user.getFirstName() + " " + user.getLastName() : "";
            }))
            .collect(Collectors.toList());

        displayedUser.setAll(filtered);
    }

    public Node getView() {
        return view;
    }

    private void updateOrganizationLabel() {
        Organization organization = FXOrganization.getOrganization();
        if (organization != null) {
            String orgName = organization.getStringFieldValue("name");
            currentOrgLabel.setText(orgName != null ? I18n.getI18nText(DashSeparator) + orgName : "");
        } else {
            currentOrgLabel.setText("");
        }
    }

    private VBox createUserManagementCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("admin-card");
        card.setPadding(new Insets(24));

        // Header with title and button
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(TeamMembersAndRoles);
        titleLabel.getStyleClass().add("admin-card-title");

        currentOrgLabel = new Label("");
        currentOrgLabel.getStyleClass().add("admin-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button assignButton = Bootstrap.successButton(I18nControls.newButton(AssignRoleToUser));
        assignButton.setOnAction(e -> showAssignRoleDialog());

        header.getChildren().addAll(titleLabel, currentOrgLabel, spacer, assignButton);

        // Search field with icon
        searchField = new TextField();
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchUsers);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        // Add search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateDisplayedUserRoles());

        card.getChildren().addAll(header, searchContainer, userAccessGrid);
        VBox.setVgrow(userAccessGrid, Priority.ALWAYS);

        return card;
    }


    private void showAssignRoleDialog() {
        AssignUserAndRoleToOrganizationDialog.show(null, this::refresh, buttonFactory, getEntityStore());
    }

    void showEditRoleDialog(AuthorizationOrganizationUserAccess userAccess) {
        AssignUserAndRoleToOrganizationDialog.show(userAccess, this::refresh, buttonFactory, getEntityStore());
    }

    private EntityStore getEntityStore() {
        return userAccessMapper != null ? userAccessMapper.getStore() : EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
    }

    void showRevokeAccessDialog(AuthorizationOrganizationUserAccess userAccess) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(400);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(600);

        Label titleLabel = I18nControls.newLabel(Delete);
        titleLabel.getStyleClass().add("delete-dialog-title");

        Person user = userAccess.getUser();
        String userName = user != null ? user.getFirstName() + I18n.getI18nText(Space) + user.getLastName() : "Unknown";
        Label messageLabel = new Label(I18n.getI18nText(Delete) + I18n.getI18nText(Space) + userName);
        messageLabel.getStyleClass().add("delete-dialog-message");
        messageLabel.setWrapText(true);

        Label confirmLabel = I18nControls.newLabel(RevokeAccessConfirmation);
        confirmLabel.getStyleClass().add("delete-dialog-confirm");
        confirmLabel.setWrapText(true);

        dialogContent.getChildren().addAll(titleLabel, messageLabel, confirmLabel);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(Delete));

        footer.getChildren().addAll(cancelButton, deleteButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        dev.webfx.extras.util.dialog.DialogCallback dialogCallback = dev.webfx.extras.util.dialog.DialogUtil.showModalNodeInGoldLayout(
            dialogPane, one.modality.base.client.mainframe.fx.FXMainFrameDialogArea.getDialogArea()
        );

        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        deleteButton.setOnAction(e -> {
            UpdateStore updateStore = UpdateStore.createAbove(getEntityStore());
            updateStore.deleteEntity(userAccess);
            updateStore.submitChanges()
                .onSuccess(result -> {
                    dialogCallback.closeDialog();
               //     refresh();
                })
                .onFailure(error -> {
                    dialogCallback.closeDialog();
                    showErrorDialog(error.getMessage());
                });
        });
    }

    private void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = I18nControls.newLabel(Error);
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label headerLabel = I18nControls.newLabel(FailedToRevokeAccess);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        dev.webfx.extras.util.dialog.DialogCallback dialogCallback = dev.webfx.extras.util.dialog.DialogUtil.showModalNodeInGoldLayout(
            dialogPane, one.modality.base.client.mainframe.fx.FXMainFrameDialogArea.getDialogArea()
        );

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    public void refresh() {
        if (userAccessMapper != null) {
            userAccessMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (userAccessMapper != null) {
            ReactiveDqlQuery<AuthorizationOrganizationUserAccess> reactiveDqlQuery = userAccessMapper.getReactiveDqlQuery();
            reactiveDqlQuery.setActive(active);
        }
    }
}
