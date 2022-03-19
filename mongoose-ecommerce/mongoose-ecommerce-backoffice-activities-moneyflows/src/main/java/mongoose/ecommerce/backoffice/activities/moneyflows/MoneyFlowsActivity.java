package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.kit.util.properties.Properties;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public class MoneyFlowsActivity extends OrganizationDependentViewDomainActivity implements ConventionalUiBuilderMixin {

    private ReactiveVisualMapper<MoneyAccount> masterVisualMapper;

    private final MoneyFlowsPresentationModel pm = new MoneyFlowsPresentationModel();

    @Override
    public MoneyFlowsPresentationModel getPresentationModel() {
        return pm;
    }

    private ConventionalUiBuilder ui;
    private final MoneyTransferEntityGraph graph = new MoneyTransferEntityGraph();
    private MoneyAccountEditorPane editorPane;
    private Label addLabel;
    private Label deleteLabel;

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "bookings", "MoneyAccount");
        Pane table = ui.buildUi();
        editorPane = new MoneyAccountEditorPane(graph.moneyAccountPanes(), graph.moneyFlowArrowViews());
        ScrollPane editorScrollPane = new ScrollPane(editorPane);
        editorScrollPane.setStyle("-fx-background-color: lightgray");
        HBox editorAndGraph = new HBox(editorScrollPane, graph);
        graph.prefWidthProperty().bind(Properties.combine(editorAndGraph.widthProperty(), editorPane.widthProperty(), (parentWidth, editorWidth) -> parentWidth.doubleValue() - editorWidth.doubleValue()));
        VBox root = new VBox(table, editorAndGraph);
        table.prefHeightProperty().bind(Properties.compute(root.heightProperty(), height -> height.doubleValue() * 0.3));
        graph.prefHeightProperty().bind(Properties.compute(root.heightProperty(), height -> height.doubleValue() * 0.7));
        pm.selectedMasterProperty().addListener(e -> updateSelectedEntity());
        Pane rootPane = new Pane(root);
        root.prefWidthProperty().bind(rootPane.widthProperty());
        root.prefHeightProperty().bind(rootPane.heightProperty());

        createAddLabel(rootPane);
        createDeleteLabel(rootPane);

        return rootPane;
    }

    private void createAddLabel(Pane rootPane) {
        addLabel = new Label("+");
        addLabel.setFont(new Font(128));
        addLabel.layoutXProperty().bind(Properties.combine(rootPane.widthProperty(), addLabel.widthProperty(), (nodeWidth, buttonWidth) -> nodeWidth.doubleValue() - buttonWidth.doubleValue()));
        addLabel.layoutYProperty().bind(Properties.combine(rootPane.heightProperty(), addLabel.heightProperty(), (nodeHeight, buttonHeight) -> nodeHeight.doubleValue() - buttonHeight.doubleValue()));
        addLabel.setOnMouseClicked(e -> {
            UpdateStore updateStore = UpdateStore.createAbove(masterVisualMapper.getStore());
            MoneyAccount insertEntity = updateStore.insertEntity(MoneyAccount.class);
            editorPane.edit(insertEntity);
        });
        rootPane.getChildren().add(addLabel);
    }

    private void createDeleteLabel(Pane rootPane) {
        deleteLabel = new Label("X");
        deleteLabel.setFont(new Font(128));
        deleteLabel.layoutXProperty().bind(Properties.combine(addLabel.layoutXProperty(), deleteLabel.widthProperty(), (x, width) -> x.doubleValue() - width.doubleValue()));
        deleteLabel.layoutYProperty().bind(addLabel.layoutYProperty());
        deleteLabel.setVisible(false);
        deleteLabel.setOnMouseClicked(e -> {
            Label label = new Label(buildDeleteMoneyAccountMsg());
            DialogContent dialogContent = new DialogContent().setContent(label);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, rootPane);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                System.out.println("TODO write function to delete money account and its linked money flows");
                dialogCallback.closeDialog();
            });
        });
        rootPane.getChildren().add(deleteLabel);
    }

    private String buildDeleteMoneyAccountMsg() {
        MoneyAccount moneyAccount = graph.selectedMoneyAccount().get();

        List<MoneyFlow> moneyFlowsLinkedToAccount = graph.moneyFlowArrowViews().stream()
                .map(arrow -> arrow.moneyFlowProperty().get())
                .filter(moneyFlow -> moneyFlow.getToMoneyAccount().equals(moneyAccount) ||
                        moneyFlow.getFromMoneyAccount().equals(moneyAccount))
                .collect(Collectors.toList());

        if (moneyFlowsLinkedToAccount.isEmpty()) {
            return String.format("Are you sure you wish to delete %s?", moneyAccount.getName());
        } else {
            String joinedAccountNames = moneyFlowsLinkedToAccount.stream()
                    .map(moneyFlow -> moneyFlow.getToMoneyAccount().equals(moneyAccount) ?
                            moneyFlow.getFromMoneyAccount().getName() : moneyFlow.getToMoneyAccount().getName())
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.joining(System.lineSeparator()));

            return String.format("%s has money flows with the following accounts:\n\n%s\n\nThese money flows will also be deleted. Continue?",
                    moneyAccount.getName(), joinedAccountNames);
        }
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
                .always("{class: 'MoneyAccount', alias: 'ma', columns: 'name,type'}")
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                .setIndividualEntityToObjectMapperFactory(moneyAccount -> new MoneyAccountToPaneMapper(moneyAccount, editorPane))
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

        MoneyAccountToPaneMapper(MoneyAccount moneyAccount, MoneyAccountEditorPane editorPane) {
            pane = new MoneyAccountPane(moneyAccount, graph.selectedMoneyAccount());
            pane.setOnMouseClicked(e -> {
                graph.selectedMoneyAccount().set(moneyAccount);
                editorPane.edit(moneyAccount);
                deleteLabel.setVisible(true);
            });
        }

        @Override
        public MoneyAccountPane getMappedObject() {
            return pane;
        }

        @Override
        public void onEntityChangedOrReplaced(MoneyAccount moneyAccount) {
            pane.moneyAccountProperty().set(moneyAccount);
        }

        @Override
        public void onEntityRemoved(MoneyAccount moneyAccount) {
        }
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
