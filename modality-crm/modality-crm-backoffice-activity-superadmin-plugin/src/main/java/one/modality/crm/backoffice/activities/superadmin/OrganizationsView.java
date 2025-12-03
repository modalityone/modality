package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
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
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Organization;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static one.modality.crm.backoffice.activities.superadmin.SuperAdmin18nKeys.*;

/**
 * Organizations tab view for managing organizations (centers).
 *
 * @author Claude Code
 */
public class OrganizationsView {

    private final VBox view;
    private final VisualGrid organizationsGrid;
    private TextField searchField;
    private ReactiveEntitiesMapper<Organization> organizationsMapper;
    private final ObservableList<Organization> organizationsFeed = FXCollections.observableArrayList();
    private final ObservableList<Organization> displayedOrganizations = FXCollections.observableArrayList();
    private EntityColumn<Organization>[] columns;

    private UpdateStore updateStore;

    static {
        // Register custom renderers
        OrganizationsRenderers.registerRenderers();
    }

    public OrganizationsView() {
        view = new VBox();
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);

        organizationsGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        organizationsGrid.setMinRowHeight(48);
        organizationsGrid.setPrefRowHeight(48);
        organizationsGrid.setPrefHeight(600);
        organizationsGrid.setMaxHeight(Double.MAX_VALUE);
        organizationsGrid.setMinWidth(0);
        organizationsGrid.setPrefWidth(Double.MAX_VALUE);
        organizationsGrid.setMaxWidth(Double.MAX_VALUE);
        organizationsGrid.getStyleClass().add("organizations-grid");

        // Pass this view to renderers
        OrganizationsRenderers.setOrganizationsView(this);

        // Info box - outside card
        Label infoBox = Bootstrap.infoBox(I18nControls.newLabel(OrganizationsInfoBox));
        infoBox.setWrapText(true);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Card container - wraps content with white background
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        Label sectionTitle = I18nControls.newLabel(OrganizationsSectionTitle);
        sectionTitle.getStyleClass().add("section-title");

        // Header with search and create button
        HBox header = createHeader();

        card.getChildren().addAll(sectionTitle, header, organizationsGrid);
        VBox.setVgrow(organizationsGrid, Priority.ALWAYS);

        view.getChildren().addAll(infoBox, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Initialize ReactiveVisualMapper
        startLogic();
    }

    public Node getView() {
        return view;
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create search field with icon
        searchField = new TextField();
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchOrganizationsPlaceholder);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("\uD83D\uDD0D");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        Button createButton = Bootstrap.primaryButton(I18nControls.newButton(CreateOrganization));
        createButton.setOnAction(e -> showCreateDialog());

        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        header.getChildren().addAll(searchContainer, createButton);

        return header;
    }

    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        updateStore = UpdateStore.createAbove(EntityStore.create(dataSourceModel));

        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'this', label: 'ID', renderer: 'organizationId', minWidth: 60, prefWidth: 70, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Name', renderer: 'organizationName', minWidth: 200, prefWidth: 300},
                {expression: 'type.code', label: 'Type', minWidth: 80, prefWidth: 100, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Location', renderer: 'organizationLocation', minWidth: 150, prefWidth: 250},
                {expression: 'this', label: 'Contact', renderer: 'organizationContact', minWidth: 150, prefWidth: 250},
                {expression: 'this', label: 'Status', renderer: 'organizationStatus', minWidth: 80, prefWidth: 100, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Actions', renderer: 'organizationActions', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "Organization");

        // Query organizations with all needed fields
        String fields = "id,name,type.code,type.name,country.name,language.name,closed,domainName,email,phone,street,postCode,cityName,timezone,latitude,longitude";
        String query = "{class: 'Organization', alias: 'o', fields: '" + fields + "', orderBy: 'name'}";

        organizationsMapper = ReactiveEntitiesMapper.<Organization>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always(query)
            .storeEntitiesInto(organizationsFeed)
            .start();

        // Update displayed organizations when feed or search text changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedOrganizations,
            ObservableLists.versionNumber(organizationsFeed),
            searchField.textProperty()
        );

        // Update grid when displayed organizations change
        ObservableLists.runNowAndOnListChange(change -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedOrganizations, columns);
            organizationsGrid.setVisualResult(vr);
        }, displayedOrganizations);
    }

    private void updateDisplayedOrganizations() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Get all organizations and sort by name
        List<Organization> organizations = organizationsFeed.stream()
            .sorted(Comparator.comparing(Organization::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
            .collect(Collectors.toList());

        // Apply search filter
        if (!searchText.isEmpty()) {
            organizations = organizations.stream()
                .filter(org -> {
                    String name = org.getName();
                    String cityName = org.getStringFieldValue("cityName");
                    String countryName = org.getCountry() != null ? org.getCountry().getName() : null;
                    String typeName = org.getType() != null ? org.getType().getName() : null;
                    String typeCode = org.getType() != null ? org.getType().getCode() : null;

                    return (name != null && name.toLowerCase().contains(searchText)) ||
                           (cityName != null && cityName.toLowerCase().contains(searchText)) ||
                           (countryName != null && countryName.toLowerCase().contains(searchText)) ||
                           (typeName != null && typeName.toLowerCase().contains(searchText)) ||
                           (typeCode != null && typeCode.toLowerCase().contains(searchText));
                })
                .collect(Collectors.toList());
        }

        // Update displayed organizations
        displayedOrganizations.setAll(organizations);
    }

    private void showCreateDialog() {
        OrganizationDialog.show(null, this::refresh);
    }

    void showEditDialog(Organization organization) {
        OrganizationDialog.show(organization, this::refresh);
    }

    void toggleClosedStatus(Organization organization) {
        Boolean currentClosed = organization.getBooleanFieldValue("closed");
        boolean newClosed = currentClosed == null ? true : !currentClosed;

        // Create update store and update the organization
        UpdateStore localUpdateStore = UpdateStore.createAbove(organization.getStore());
        Organization orgToUpdate = localUpdateStore.updateEntity(organization);
        orgToUpdate.setClosed(newClosed);

        localUpdateStore.submitChanges()
            .onSuccess(result -> refresh())
            .onFailure(error -> showErrorDialog(error.getMessage()));
    }

    void showDeleteDialog(Organization organization) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = I18nControls.newLabel(DeleteOrganization);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(Delete) + I18n.getI18nText(Space) + organization.getName() + I18n.getI18nText(QuestionMark));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Label confirmLabel = I18nControls.newLabel(DeleteOrganizationConfirm);
        confirmLabel.setWrapText(true);
        confirmLabel.setMaxWidth(Double.MAX_VALUE);
        confirmLabel.getStyleClass().add("delete-dialog-confirm");

        dialogContent.getChildren().addAll(titleLabel, messageLabel, confirmLabel);

        // Buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(Delete));

        footer.getChildren().addAll(cancelButton, deleteButton);
        dialogContent.getChildren().add(footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        deleteButton.setOnAction(e -> {
            // Soft delete - set closed to true instead of actual deletion
            UpdateStore localUpdateStore = UpdateStore.createAbove(organization.getStore());
            Organization orgToUpdate = localUpdateStore.updateEntity(organization);
            orgToUpdate.setClosed(true);

            localUpdateStore.submitChanges()
                .onSuccess(result -> {
                    dialogCallback.closeDialog();
                    refresh();
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

        Label headerLabel = I18nControls.newLabel(FailedToSaveOrganization);
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
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    public void refresh() {
        if (organizationsMapper != null) {
            organizationsMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (organizationsMapper != null) {
            organizationsMapper.getReactiveDqlQuery().setActive(active);
        }
    }
}
