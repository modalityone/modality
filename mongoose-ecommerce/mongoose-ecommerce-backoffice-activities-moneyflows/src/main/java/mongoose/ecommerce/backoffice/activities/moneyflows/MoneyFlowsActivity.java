package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.kit.util.properties.Properties;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import mongoose.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import mongoose.base.shared.domainmodel.functions.AbcNames;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public class MoneyFlowsActivity extends OrganizationDependentViewDomainActivity implements ConventionalUiBuilderMixin {

    private ReactiveVisualMapper<MoneyAccount> masterVisualMapper;
    private ReactiveObjectsMapper<MoneyFlow, MoneyFlowArrowView> moneyFlowToArrowMapper;

    private final MoneyFlowsPresentationModel pm = new MoneyFlowsPresentationModel();

    @Override
    public MoneyFlowsPresentationModel getPresentationModel() {
        return pm;
    }

    private ConventionalUiBuilder ui;
    private MoneyTransferEntityGraph graph = new MoneyTransferEntityGraph();

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "bookings", "MoneyAccount");
        Pane table = ui.buildUi();
        VBox container = new VBox(table, graph);
        table.prefHeightProperty().bind(Properties.compute(container.heightProperty(), height -> height.doubleValue() * 0.3));
        graph.prefHeightProperty().bind(Properties.compute(container.heightProperty(), height -> height.doubleValue() * 0.7));
        pm.masterVisualResultProperty().addListener(e -> updateGraph());
        pm.selectedMasterProperty().addListener(e -> updateSelectedEntity());
        return container;
    }

    private void updateGraph() {
        graph.setMoneyAccounts(masterVisualMapper.getEntities());
    }

    private void updateSelectedEntity() {
        System.out.println("selectedEntity = " + pm.selectedMasterProperty().get());
        graph.setSelectedEntity(pm.selectedMasterProperty().get());
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

        moneyFlowToArrowMapper = ReactiveObjectsMapper.<MoneyFlow, MoneyFlowArrowView>createPushReactiveChain(this)
                .always("{class: 'MoneyFlow', alias: 'mf', fields: 'fromMoneyAccount,toMoneyAccount'}")
                .setIndividualEntityToObjectMapperFactory(graph::newMoneyFlowToArrowMapper)
                .setStore(masterVisualMapper.getStore())
                .storeMappedObjectsInto(graph.moneyFlowArrowViews())
                .start();

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
