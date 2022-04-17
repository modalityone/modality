package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.action.operation.OperationActionFactoryMixin;
import dev.webfx.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.kit.util.properties.Properties;
import dev.webfx.platform.shared.services.submit.SubmitArgument;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import mongoose.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import mongoose.base.shared.domainmodel.functions.AbcNames;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;
import mongoose.base.shared.entities.Organization;
import mongoose.ecommerce.backoffice.operations.entities.moneyaccount.AddNewMoneyAccountRequest;
import mongoose.ecommerce.backoffice.operations.entities.moneyaccount.DeleteMoneyAccountRequest;

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

    private ConventionalUiBuilder ui;
    private ReactiveVisualMapper<MoneyAccount> masterVisualMapper;
    private MoneyAccountEditorPane editorPane;
    private Pane rootPane;
    private Button addNewMoneyAccountButton;
    private Button deleteLabel;

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "bookings", "MoneyAccount");
        Pane table = ui.buildUi();
        editorPane = new MoneyAccountEditorPane(graph.moneyAccountPanes(), graph.moneyFlowArrowViews());
        HBox editorAndGraph = new HBox(editorPane, graph);
        graph.prefWidthProperty().bind(Properties.combine(editorAndGraph.widthProperty(), editorPane.widthProperty(), (parentWidth, editorWidth) -> parentWidth.doubleValue() - editorWidth.doubleValue()));
        VBox root = new VBox(table, editorAndGraph);
        table.prefHeightProperty().bind(Properties.compute(root.heightProperty(), height -> height.doubleValue() * 0.3));
        graph.prefHeightProperty().bind(Properties.compute(root.heightProperty(), height -> height.doubleValue() * 0.7));
        pm.selectedMasterProperty().addListener(e -> updateSelectedEntity());
        rootPane = new Pane(root);
        root.prefWidthProperty().bind(rootPane.widthProperty());
        root.prefHeightProperty().bind(rootPane.heightProperty());

        createAddNewMoneyAccountButton();
        createDeleteLabel();

        return rootPane;
    }

    private void createAddNewMoneyAccountButton() {
        addNewMoneyAccountButton = newButton(newOperationAction(() -> new AddNewMoneyAccountRequest(getOrganization(), rootPane)));
        addNewMoneyAccountButton.setFont(new Font(32));
        addNewMoneyAccountButton.layoutXProperty().bind(Properties.combine(rootPane.widthProperty(), addNewMoneyAccountButton.widthProperty(), (nodeWidth, buttonWidth) -> nodeWidth.doubleValue() - buttonWidth.doubleValue()));
        addNewMoneyAccountButton.layoutYProperty().bind(Properties.combine(rootPane.heightProperty(), addNewMoneyAccountButton.heightProperty(), (nodeHeight, buttonHeight) -> nodeHeight.doubleValue() - buttonHeight.doubleValue()));
        rootPane.getChildren().add(addNewMoneyAccountButton);
    }

    private Organization getOrganization() {
        Object organizationId = getOrganizationId();
        EntityStore entityStore = EntityStore.createAbove(masterVisualMapper.getStore());
        return entityStore.getEntity(Organization.class, organizationId);
    }

    private void createDeleteLabel() {
        deleteLabel = newButton(newOperationAction(() -> new DeleteMoneyAccountRequest(getSelectedMoneyAccount(), getMoneyFlows(), rootPane)));
        deleteLabel.setFont(new Font(32));
        deleteLabel.layoutXProperty().bind(Properties.combine(addNewMoneyAccountButton.layoutXProperty(), deleteLabel.widthProperty(), (x, width) -> x.doubleValue() - width.doubleValue()));
        deleteLabel.layoutYProperty().bind(addNewMoneyAccountButton.layoutYProperty());
        rootPane.getChildren().add(deleteLabel);
    }

    private MoneyAccount getSelectedMoneyAccount() {
        return graph.selectedMoneyAccount().get();
    }

    private List<MoneyFlow> getMoneyFlows() {
        return graph.moneyFlowArrowViews().stream()
                .map(arrow -> arrow.moneyFlowProperty().get())
                .collect(Collectors.toList());
    }

    private void updateSelectedEntity() {
        System.out.println("selectedEntity = " + pm.selectedMasterProperty().get());
        //graph.setSelectedEntity(pm.selectedMasterProperty().get());
    }

    @Override
    protected void startLogic() {
        // Setting up the master mapper that build the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<MoneyAccount>createMasterPushReactiveChain(this, pm)
                .always("{class: 'MoneyAccount', alias: 'ma', columns: 'name,type', orderBy: 'name desc'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("name like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .start();

        ReactiveObjectsMapper.<MoneyAccount, MoneyAccountPane>createPushReactiveChain(this)
                .always("{class: 'MoneyAccount', alias: 'ma', fields: 'name,type,organization'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .setIndividualEntityToObjectMapperFactory(MoneyAccountToPaneMapper::new)
                .setStore(masterVisualMapper.getStore())
                .storeMappedObjectsInto(graph.moneyAccountPanes())
                .start();

        ReactiveObjectsMapper.<MoneyFlow, MoneyFlowArrowView>createPushReactiveChain(this)
                .always("{class: 'MoneyFlow', alias: 'mf', fields: 'fromMoneyAccount,toMoneyAccount'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .setIndividualEntityToObjectMapperFactory(graph::newMoneyFlowToArrowMapper)
                .setStore(masterVisualMapper.getStore())
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
        }

        private void showMsg(String msg) {
            Label label = new Label(msg);
            DialogContent dialogContent = new DialogContent().setContent(label);
            dialogContent.getCancelButton().setVisible(false);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, rootPane);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> dialogCallback.closeDialog());
        }

        private MoneyAccount getSelectedMoneyAccount() {
            return graph.selectedMoneyAccount().get();
        }

        private void selectMoneyAccount(MoneyAccount moneyAccount) {
            graph.selectedMoneyAccount().set(moneyAccount);
            editorPane.edit(moneyAccount);
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
        masterVisualMapper.refreshWhenActive();
    }

    @Override
    public void onResume() {
        super.onResume();
        ui.onResume();
    }

}
