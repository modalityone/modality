package org.modality_project.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.action.ActionGroup;
import dev.webfx.framework.client.ui.action.operation.OperationActionFactoryMixin;
import dev.webfx.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.framework.shared.orm.dql.DqlStatement;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.kit.util.properties.Properties;
import dev.webfx.platform.shared.services.submit.SubmitArgument;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import org.modality_project.base.shared.domainmodel.functions.AbcNames;
import org.modality_project.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import org.modality_project.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import org.modality_project.base.shared.entities.MoneyAccount;
import org.modality_project.base.shared.entities.MoneyFlow;
import org.modality_project.base.shared.entities.Organization;
import org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount.AddNewMoneyAccountRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount.DeleteMoneyAccountRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount.EditMoneyAccountRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.moneyflow.DeleteMoneyFlowRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.moneyflow.EditMoneyFlowRequest;

import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public class MoneyFlowsActivity extends OrganizationDependentViewDomainActivity implements ConventionalUiBuilderMixin, OperationActionFactoryMixin {

    private static final DataFormat dndDataFormat = DataFormat.PLAIN_TEXT; // Using standard plain text format to ensure drag & drop works between applications

    private final MoneyTransferEntityGraph graph = new MoneyTransferEntityGraph();
    private final MoneyFlowsPresentationModel pm = new MoneyFlowsPresentationModel();
    @Override
    public MoneyFlowsPresentationModel getPresentationModel() {
        return pm;
    }

    private ReactiveVisualMapper<MoneyAccount> moneyAccountVisualMapper;
    private ReactiveVisualMapper<MoneyFlow> moneyFlowVisualMapper;
    private Pane moneyAccountTableContainer;
    private Pane moneyFlowTableContainer;

    @Override
    public Node buildUi() {
        VisualGrid moneyAccountTable = new VisualGrid();
        moneyAccountTable.visualResultProperty().bind(pm.moneyAccountsVisualResultProperty());
        moneyAccountTable.visualSelectionProperty().bindBidirectional(pm.moneyAccountsVisualSelectionProperty());
        moneyAccountTable.prefWidthProperty().bind(graph.widthProperty());
        moneyAccountTableContainer = new HBox(moneyAccountTable);
        setUpContextMenu(moneyAccountTable, this::createMoneyAccountTableContextMenuActionGroup);

        VisualGrid moneyFlowTable = new VisualGrid();
        moneyFlowTable.visualResultProperty().bind(pm.moneyFlowsVisualResultProperty());
        moneyFlowTable.visualSelectionProperty().bindBidirectional(pm.moneyFlowsVisualSelectionProperty());
        moneyFlowTable.prefWidthProperty().bind(graph.widthProperty());
        moneyFlowTableContainer = new HBox(moneyFlowTable);
        setUpContextMenu(moneyFlowTable, this::createMoneyFlowTableContextMenuActionGroup);

        graph.selectedMoneyAccount().addListener(e -> {
            MoneyAccount selectedEntity = graph.selectedMoneyAccount().get();
            int rowIndex = moneyAccountVisualMapper.getEntities().indexOf(selectedEntity);
            if (rowIndex != -1) {
                VisualSelection value = VisualSelection.createSingleRowSelection(rowIndex);
                pm.moneyAccountsVisualSelectionProperty().set(value);
            }
        });
        graph.selectedMoneyFlow().addListener(e -> {
            MoneyFlow selectedEntity = graph.selectedMoneyFlow().get();
            int rowIndex = moneyFlowVisualMapper.getEntities().indexOf(selectedEntity);
            if (rowIndex != -1) {
                VisualSelection value = VisualSelection.createSingleRowSelection(rowIndex);
                pm.moneyFlowsVisualSelectionProperty().set(value);
            }
        });

        createAddNewMoneyAccountButton();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(new Tab("Graph", graph));
        tabPane.getTabs().add(new Tab("Money Account Table", moneyAccountTableContainer));
        tabPane.getTabs().add(new Tab("Money Flow Table", moneyFlowTableContainer));
        return tabPane;
    }

    private ActionGroup createMoneyAccountTableContextMenuActionGroup() {
        return newActionGroup(
                newOperationAction(() -> new EditMoneyAccountRequest(graph.selectedMoneyAccount().get(), moneyAccountTableContainer)),
                newOperationAction(() -> new DeleteMoneyAccountRequest(graph.selectedMoneyAccount().get(), getMoneyFlows(), moneyAccountTableContainer))
        );
    }

    private ActionGroup createMoneyFlowTableContextMenuActionGroup() {
        return newActionGroup(
                newOperationAction(() -> new EditMoneyFlowRequest(graph.selectedMoneyFlow().get(), moneyFlowTableContainer)),
                newOperationAction(() -> new DeleteMoneyFlowRequest(graph.selectedMoneyFlow().get(), moneyFlowTableContainer))
        );
    }

    private void createAddNewMoneyAccountButton() {
        Button addNewMoneyAccountButton = newButton(newOperationAction(() -> new AddNewMoneyAccountRequest(getOrganization(), graph)));
        addNewMoneyAccountButton.setFont(new Font(32));
        addNewMoneyAccountButton.layoutXProperty().bind(Properties.combine(graph.widthProperty(), addNewMoneyAccountButton.widthProperty(), (nodeWidth, buttonWidth) -> nodeWidth.doubleValue() - buttonWidth.doubleValue()));
        addNewMoneyAccountButton.layoutYProperty().bind(Properties.combine(graph.heightProperty(), addNewMoneyAccountButton.heightProperty(), (nodeHeight, buttonHeight) -> nodeHeight.doubleValue() - buttonHeight.doubleValue()));
        graph.getChildren().add(addNewMoneyAccountButton);
    }

    private Organization getOrganization() {
        Object organizationId = getOrganizationId();
        EntityStore entityStore = EntityStore.createAbove(moneyAccountVisualMapper.getStore());
        return entityStore.getEntity(Organization.class, organizationId);
    }

    private MoneyAccount getSelectedMoneyAccount() {
        return graph.selectedMoneyAccount().get();
    }

    private List<MoneyFlow> getMoneyFlows() {
        return graph.moneyFlowArrowViews().stream()
                .map(arrow -> arrow.moneyFlowProperty().get())
                .collect(Collectors.toList());
    }

    @Override
    protected void startLogic() {
        // Setting up the master mapper that build the content displayed in the master view
        moneyAccountVisualMapper = ReactiveVisualMapper.<MoneyAccount>createPushReactiveChain(this)
                .always("{class: 'MoneyAccount', alias: 'ma', columns: 'name,closed,currency,event,gatewayCompany,type', fields: 'id', orderBy: 'name desc'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> DqlStatement.where("name like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.moneyAccountsVisualResultProperty())
                .setVisualSelectionProperty(pm.moneyAccountsVisualSelectionProperty())
                .setSelectedEntityHandler(entity -> graph.selectedMoneyAccount().set(entity))
                .start();

        moneyFlowVisualMapper = ReactiveVisualMapper.<MoneyFlow>createPushReactiveChain(this)
                .always("{class: 'MoneyFlow', alias: 'mf', fields: 'organization'}")
                .setEntityColumns("[" +
                        "{label: 'From', expression: 'fromMoneyAccount'}," +
                        "{label: 'To', expression: 'toMoneyAccount'}," +
                        "{label: 'Method', expression: 'method'}," +
                        "{label: 'Positive Amounts', expression: 'positiveAmounts'}," +
                        "{label: 'Negative Amounts', expression: 'negativeAmounts'}" +
                        //"{label: 'Auto Transfer Time', expression: '[autoTransferTime]'}," +
                        "]")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.moneyFlowsVisualResultProperty())
                .setVisualSelectionProperty(pm.moneyFlowsVisualSelectionProperty())
                .setSelectedEntityHandler(entity -> graph.selectedMoneyFlow().set(entity))
                .start();

        ReactiveObjectsMapper.<MoneyAccount, MoneyAccountPane>createPushReactiveChain(this)
                .always("{class: 'MoneyAccount', alias: 'ma', fields: 'name,type,organization'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .setIndividualEntityToObjectMapperFactory(MoneyAccountToPaneMapper::new)
                .setStore(moneyAccountVisualMapper.getStore())
                .storeMappedObjectsInto(graph.moneyAccountPanes())
                .start();

        ReactiveObjectsMapper.<MoneyFlow, MoneyFlowArrowView>createPushReactiveChain(this)
                .always("{class: 'MoneyFlow', alias: 'mf', fields: 'fromMoneyAccount,toMoneyAccount'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .setIndividualEntityToObjectMapperFactory(graph::newMoneyFlowToArrowMapper)
                .setStore(moneyAccountVisualMapper.getStore())
                .storeMappedObjectsInto(graph.moneyFlowArrowViews())
                .start();
    }

    class MoneyAccountToPaneMapper implements IndividualEntityToObjectMapper<MoneyAccount, MoneyAccountPane> {

        final MoneyAccountPane pane;

        MoneyAccountToPaneMapper(MoneyAccount moneyAccount) {
            pane = new MoneyAccountPane(moneyAccount, graph.selectedMoneyAccount());
            pane.setOnMouseClicked(e -> selectMoneyAccount(moneyAccount));
            pane.setOnDragDetected(e -> {
                selectMoneyAccount(moneyAccount);
                Dragboard db = graph.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(dndDataFormat, String.valueOf(moneyAccount.getId().getPrimaryKey()));
                db.setContent(content);
            });
            pane.setOnDragOver(e -> {
                if (moneyAccount != getSelectedMoneyAccount()) {
                    MoneyAccount fromAccount = getSelectedMoneyAccount();
                    MoneyAccount toAccount = moneyAccount;
                    pane.setShowIllegalIndicator(!canCreateMoneyFlow(fromAccount, toAccount));
                    pane.setHovering(true);
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            });
            pane.setOnDragExited(e -> {
                pane.setShowIllegalIndicator(false);
                pane.setHovering(false);
            });
            pane.setOnDragDropped(e -> {
                MoneyAccount fromAccount = getSelectedMoneyAccount();
                MoneyAccount toAccount = moneyAccount;
                if (doesMoneyFlowExist(fromAccount, moneyAccount)) {
                    String msg = "A money flow from " + fromAccount.getName() + " to " + toAccount.getName() + " already exists.";
                    showMsg(msg);
                } else if (isFirstAccountUpstreamOfSeconds(moneyAccount, fromAccount)) {
                    String msg = "Creating a money flow from " + fromAccount.getName() + " to " + toAccount.getName() + " would result in a circular reference.";
                    showMsg(msg);
                } else {
                    UpdateStore updateStore = UpdateStore.createAbove(moneyAccount.getStore());
                    MoneyFlow insertEntity = updateStore.insertEntity(MoneyFlow.class);
                    insertEntity.setFromMoneyAccount(fromAccount);
                    insertEntity.setToMoneyAccount(moneyAccount);
                    insertEntity.setOrganization(moneyAccount.getOrganization());
                    updateStore.submitChanges(SubmitArgument.builder()
                            .setStatement("select set_transaction_parameters(false)")
                            .setDataSourceId(updateStore.getDataSourceId())
                            .build());
                }
            });
            setUpContextMenu(pane, this::createContextMenuActionGroup);
        }

        private ActionGroup createContextMenuActionGroup() {
            return newActionGroup(
                    newOperationAction(() -> new EditMoneyAccountRequest(graph.selectedMoneyAccount().get(), graph)),
                    newOperationAction(() -> new DeleteMoneyAccountRequest(getSelectedMoneyAccount(), getMoneyFlows(), graph))
            );
        }

        private void showMsg(String msg) {
            Label label = new Label(msg);
            DialogContent dialogContent = new DialogContent().setContent(label);
            dialogContent.getCancelButton().setVisible(false);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, graph);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> dialogCallback.closeDialog());
        }

        private MoneyAccount getSelectedMoneyAccount() {
            return graph.selectedMoneyAccount().get();
        }

        private void selectMoneyAccount(MoneyAccount moneyAccount) {
            graph.selectedMoneyAccount().set(moneyAccount);
        }

        @Override
        public MoneyAccountPane getMappedObject() {
            return pane;
        }

        @Override
        public void onEntityChangedOrReplaced(MoneyAccount moneyAccount) {
            pane.populate(moneyAccount);
        }

        @Override
        public void onEntityRemoved(MoneyAccount moneyAccount) {
        }
    }

    private boolean canCreateMoneyFlow(MoneyAccount fromAccount, MoneyAccount toAccount) {
        return !doesMoneyFlowExist(fromAccount, toAccount) && !isFirstAccountUpstreamOfSeconds(toAccount, fromAccount);
    }

    private boolean doesMoneyFlowExist(MoneyAccount fromAccount, MoneyAccount toAccount) {
        return graph.moneyFlowArrowViews().stream()
                .map(arrow -> arrow.moneyFlowProperty().get())
                .anyMatch(moneyFlow -> moneyFlow.getFromMoneyAccount().equals(fromAccount) && moneyFlow.getToMoneyAccount().equals(toAccount));
    }

    private boolean isFirstAccountUpstreamOfSeconds(MoneyAccount firstAccount, MoneyAccount secondAccount) {
        List<MoneyFlow> moneyFlowsFrom = getMoneyFlowsFrom(firstAccount);
        for (MoneyFlow moneyFlow : moneyFlowsFrom) {
            MoneyAccount toAccount = moneyFlow.getToMoneyAccount();
            if (toAccount.equals(secondAccount)) {
                return true;
            }
            if (isFirstAccountUpstreamOfSeconds(toAccount, secondAccount)) {
                return true;
            }
        }
        return false;
    }

    private List<MoneyFlow> getMoneyFlowsFrom(MoneyAccount sourceAccount) {
        return graph.moneyFlowArrowViews().stream()
                .map(arrow -> arrow.moneyFlowProperty().get())
                .filter(moneyFlow -> moneyFlow.getFromMoneyAccount().equals(sourceAccount))
                .collect(Collectors.toList());
    }

    @Override
    protected void refreshDataOnActive() {
        moneyAccountVisualMapper.refreshWhenActive();
        moneyFlowVisualMapper.refreshWhenActive();
    }

    @Override
    public void onResume() {
        super.onResume();
        //ui.onResume();
        /*if (filterSearchBar != null)
            filterSearchBar.onResume();*/
    }

}
