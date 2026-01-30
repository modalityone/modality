package one.modality.base.backoffice.activities.operations;

import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.tile.TabsBar;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
final class OperationsActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final static String OPERATION_COLUMNS = // language=JSON5
        """
            [
            {expression: 'name', label: 'Name'},
            {expression: 'code', label: 'Code'},
            {expression: 'grantRoute', label: 'Grant route'},
            {expression: 'i18nCode', label: 'i18n'},
            {expression: 'backoffice', label:'BackOffice'},
            {expression: 'frontoffice', label: 'FrontOffice'},
            {expression: 'guest', label: 'Guest'},
            {expression: 'public', label: 'Public'}
            ]""";

    enum OperationTab {
        BACKOFFICE_ROUTES(true, true),
        BACKOFFICE_OPERATIONS(true, false),
        FRONTOFFICE_ROUTES(false, true),
        FRONTOFFICE_OPERATIONS(false, false);
        private final boolean backoffice;
        private final boolean routes;

        OperationTab(boolean backoffice, boolean routes) {
            this.backoffice = backoffice;
            this.routes = routes;
        }
    }

    private final ObjectProperty<OperationTab> selectedTabProperty = new SimpleObjectProperty<>(OperationTab.BACKOFFICE_ROUTES);
    private final TabsBar<OperationTab> headerTabsBar = new TabsBar<>(this, selectedTabProperty::set);
    private final VisualGrid operationsTable = new VisualGrid();
    private final TextField searchBox = new TextField();
    private final ToggleGroup publicPrivateGroup = new ToggleGroup();
    private final RadioButton privateRadioButton = new RadioButton("Private operations (require authorization)");
    private final RadioButton publicRadioButton = new RadioButton("Public operations (don't require authorization)");
    private final CheckBox kbsxCheckBox = new CheckBox("KBSX");

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
            headerTabsBar.createTab("BackOffice - Routes", OperationTab.BACKOFFICE_ROUTES),
            headerTabsBar.createTab("BackOffice - Operations", OperationTab.BACKOFFICE_OPERATIONS),
            headerTabsBar.createTab("FrontOffice - Routes", OperationTab.FRONTOFFICE_ROUTES),
            headerTabsBar.createTab("FrontOffice - Operations", OperationTab.FRONTOFFICE_OPERATIONS)
        );
        BorderPane container = new BorderPane(operationsTable);
        searchBox.setPromptText("Search");
        container.setTop(searchBox);
        publicRadioButton.setToggleGroup(publicPrivateGroup);
        privateRadioButton.setToggleGroup(publicPrivateGroup);
        privateRadioButton.setSelected(true);
        HBox hBox = new HBox(10, privateRadioButton, publicRadioButton, kbsxCheckBox);
        hBox.setPadding(new Insets(5));
        container.setBottom(hBox);
        return container;
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        super.onPause();
    }

    @Override
    protected void startLogic() {
        ReactiveVisualMapper.createPushReactiveChain(this)
            .always( // language=JSON5
                "{class: 'Operation', alias: 'o', orderBy: 'code'}")
            // Search box condition
            .ifTrimNotEmpty(searchBox.textProperty(), s -> where("lower(code) like $1", "%" + s.toLowerCase() + "%"))
            // Limit condition
            //.ifPositive(pm.limitProperty(), l -> limit("?", l))
            .setEntityColumns(OPERATION_COLUMNS)
            .always(selectedTabProperty, ot -> where(ot.routes ? "code like 'RouteTo%'" : "code not like 'RouteTo%'"))
            .always(selectedTabProperty, ot -> where(ot.backoffice ? "backoffice" : "frontoffice"))
            .always(publicRadioButton.selectedProperty(), pub -> where(pub ? "public" : "!public"))
            .always(kbsxCheckBox.selectedProperty(), kbsx -> where(kbsx ? "name like 'KBSX%'" : "name not like 'KBSX%'"))
            .visualizeResultInto(operationsTable.visualResultProperty())
            .setVisualSelectionProperty(operationsTable.visualSelectionProperty())
            .setSelectedEntityHandler(this::editOperation)
            .start()
        ;
    }

    private void editOperation(Entity operation) {
        EntityPropertiesSheet.editEntity(operation, OPERATION_COLUMNS, FXMainFrameDialogArea.getDialogArea());
    }

}