package one.modality.crm.backoffice.activities.admin;

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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.AuthorizationRole;
import one.modality.base.shared.entities.AuthorizationRule;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Authorization Rules view for managing authorization rules.
 *
 * @author Claude Code
 */
public class AuthorizationRulesView {

    private final VBox view;
    private final VisualGrid rulesGrid;
    private TextField searchField;
    private ReactiveEntitiesMapper<AuthorizationRule> rulesMapper;
    private final ObservableList<AuthorizationRule> rulesFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationRule> displayedRules = FXCollections.observableArrayList();
    private EntityColumn<AuthorizationRule>[] columns;

    static {
        // Register custom renderers
        AuthorizationRulesRenderers.registerRenderers();
    }

    private UpdateStore updateStore;

    public AuthorizationRulesView() {
        view = new VBox();
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);
        rulesGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        rulesGrid.setMinRowHeight(40);
        rulesGrid.setPrefRowHeight(40);
        rulesGrid.setPrefHeight(600);
        rulesGrid.setMaxHeight(Double.MAX_VALUE);
        rulesGrid.setMinWidth(0);
        rulesGrid.setPrefWidth(Double.MAX_VALUE);
        rulesGrid.setMaxWidth(Double.MAX_VALUE);

        // Pass this AuthorizationRulesView instance to the renderers
        AuthorizationRulesRenderers.setAuthorizationRulesView(this);

        // Info box - outside card
        Label infoBox = Bootstrap.infoBox(I18nControls.newLabel(RulesInfoBox));
        infoBox.setWrapText(true);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Legend
        HBox legend = createLegend();

        // Card container - wraps content with white background
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        Label sectionTitle = I18nControls.newLabel(RulesSectionTitle);
        sectionTitle.getStyleClass().add("section-title");

        // Header with search and create button
        HBox header = createHeader();

        card.getChildren().addAll(sectionTitle, header, rulesGrid);
        VBox.setVgrow(rulesGrid, Priority.ALWAYS);

        view.getChildren().addAll(infoBox, legend, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Initialize ReactiveEntitiesMapper
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
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchRules);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        Button createButton = Bootstrap.successButton(I18nControls.newButton(CreateRule));
        createButton.setOnAction(e -> showCreateDialog());

        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        header.getChildren().addAll(searchContainer, createButton);

        return header;
    }

    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(12, 0, 0, 0));

        Label legendLabel = I18nControls.newLabel(Legend);
        legendLabel.getStyleClass().add("admin-legend-label");

        // Role badge sample
        Label roleSample = ModalityStyle.badgeRole(new Label("Role"));
        roleSample.setPadding(new Insets(3, 8, 3, 8));

        legend.getChildren().addAll(legendLabel, roleSample);
        return legend;
    }

    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        updateStore = UpdateStore.createAbove(EntityStore.create(dataSourceModel));

        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'name', label: 'Rule Name', minWidth: 150},
                {expression: 'rule', label: 'Rule Expression', minWidth: 300},
                {expression: 'this', label: 'Used In', renderer: 'ruleUsedIn', minWidth: 200},
                {expression: 'this', label: 'Actions', renderer: 'ruleActions', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "AuthorizationRule");

        // Query all authorization rules with their role relationship
        rulesMapper = ReactiveEntitiesMapper.<AuthorizationRule>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationRule', alias: 'r', fields: 'name,rule,role.name', orderBy: 'name'}")
            .storeEntitiesInto(rulesFeed)
            .start();

        // Update displayed rules when rules feed or search text changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedRules,
            ObservableLists.versionNumber(rulesFeed),
            searchField.textProperty()
        );

        // Update grid when displayed rules change
        ObservableLists.runNowAndOnListChange(change -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedRules, columns);
            rulesGrid.setVisualResult(vr);
        }, displayedRules);
    }

    private void updateDisplayedRules() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Get all rules and sort by name (handle nulls)
        List<AuthorizationRule> rules = rulesFeed.stream()
            .sorted(Comparator.comparing(AuthorizationRule::getName, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());

        // Apply search filter
        if (!searchText.isEmpty()) {
            rules = rules.stream()
                .filter(rule -> {
                    return (rule.getName() != null && rule.getName().toLowerCase().contains(searchText)) ||
                           (rule.getRule() != null && rule.getRule().toLowerCase().contains(searchText));
                })
                .collect(Collectors.toList());
        }

        // Update displayed rules
        displayedRules.setAll(rules);
    }

    /**
     * Helper method for renderers to get the role that uses a specific rule.
     * Each rule belongs to exactly one role (via role_id foreign key).
     */
    List<AuthorizationRole> getRolesForRule(AuthorizationRule rule) {
        AuthorizationRole role = rule.getRole();
        if (role != null) {
            return List.of(role);
        }
        return List.of();
    }

    private void showCreateDialog() {
        AuthorizationRulesDialog.show(null, this::refresh);
    }

    void showEditDialog(AuthorizationRule rule) {
        AuthorizationRulesDialog.show(rule, this::refresh);
    }

    void showDeleteDialog(AuthorizationRule rule) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = I18nControls.newLabel(DeleteRule);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(Delete) + I18n.getI18nText(Space) + rule.getName() + I18n.getI18nText(QuestionMark));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Label confirmLabel = I18nControls.newLabel(DeleteRuleConfirm);
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
             updateStore.deleteEntity(rule);
             updateStore.submitChanges();
            dialogCallback.closeDialog();
            refresh();
        });
    }

    public void refresh() {
        if (rulesMapper != null) {
            rulesMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (rulesMapper != null) {
            rulesMapper.getReactiveDqlQuery().setActive(active);
        }
    }
}
