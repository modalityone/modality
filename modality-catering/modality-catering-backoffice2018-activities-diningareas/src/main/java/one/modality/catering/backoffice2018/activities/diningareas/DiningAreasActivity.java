package one.modality.catering.backoffice2018.activities.diningareas;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import one.modality.ecommerce.backoffice2018.activities.statistics.StatisticsBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.catering.backoffice2018.operations.entities.allocationrule.AddNewAllocationRuleRequest;
import one.modality.catering.backoffice2018.operations.entities.allocationrule.DeleteAllocationRuleRequest;
import one.modality.catering.backoffice2018.operations.entities.allocationrule.EditAllocationRuleRequest;
import one.modality.catering.backoffice2018.operations.entities.allocationrule.TriggerAllocationRuleRequest;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.orm.entity.Entity;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class DiningAreasActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin,
        ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final DiningAreasPresentationModel pm = new DiningAreasPresentationModel();

    @Override
    public DiningAreasPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    @Override
    public Node buildUi() {
        VisualGrid sittingTable = new VisualGrid();
        sittingTable.visualResultProperty().bind(pm.sittingVisualResultProperty());
        VisualGrid rulesTable = new VisualGrid();
        rulesTable.visualResultProperty().bind(pm.rulesVisualResultProperty());
        rulesTable.visualSelectionProperty().bindBidirectional(pm.rulesVisualSelectionProperty());
        SplitPane splitPane = new SplitPane(sittingTable, rulesTable);
        splitPane.setOrientation(Orientation.VERTICAL);
        Pane container = new StackPane(splitPane);
        setUpContextMenu(rulesTable, () -> newActionGroup(
                newSeparatorActionGroup(
                        newOperationAction(() -> new AddNewAllocationRuleRequest(getEvent(), container))
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new TriggerAllocationRuleRequest(rulesVisualMapper.getSelectedEntity(), container))
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new EditAllocationRuleRequest(rulesVisualMapper.getSelectedEntity(), container)),
                        newOperationAction(() -> new DeleteAllocationRuleRequest(rulesVisualMapper.getSelectedEntity(), container))
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new CopySelectionRequest(rulesVisualMapper.getSelectedEntities(), rulesVisualMapper.getEntityColumns())),
                        newOperationAction(() -> new CopyAllRequest(rulesVisualMapper.getCurrentEntities(), rulesVisualMapper.getEntityColumns()))
                )));
        return container;
    }


    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<DocumentLine> leftSittingVisualMapper;
    private ReactiveVisualMapper<Attendance> rightAttendanceVisualMapper;
    private ReactiveVisualMapper<Entity> rulesVisualMapper;
    private StatisticsBuilder statisticsBuilder; // to avoid GC

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        leftSittingVisualMapper = ReactiveVisualMapper.<DocumentLine>createReactiveChain(this)
                .always("{class: 'DocumentLine', alias: 'dl', columns: 'resourceConfiguration,count(1)', where: `!cancelled and item.family.code='meals'`, groupBy: 'resourceConfiguration', orderBy: 'resourceConfiguration..name'}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("document.event=?", eventId))
        ;

        rightAttendanceVisualMapper = ReactiveVisualMapper.<Attendance>createReactiveChain(this)
                .always("{class: 'Attendance', alias: 'a', columns: `documentLine.resourceConfiguration,date,count(1)`, where: `present and documentLine.(!cancelled and item.family.code='meals')`, groupBy: 'documentLine.resourceConfiguration,date', orderBy: 'date'}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("documentLine.document.event=?", eventId))
        ;

        // Building the statistics final display result from the 2 above filters
        statisticsBuilder = new StatisticsBuilder(leftSittingVisualMapper, rightAttendanceVisualMapper, pm.sittingVisualResultProperty()).start();

        rulesVisualMapper = ReactiveVisualMapper.createPushReactiveChain(this)
                .always("{class: 'AllocationRule', alias: 'ar', columns: '<default>', orderBy: 'ord,id'}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                // Displaying the result into the rules table through the presentation model
                .visualizeResultInto(pm.rulesVisualResultProperty())
                .setVisualSelectionProperty(pm.rulesVisualSelectionProperty())
                // Colorizing the rows
                .applyDomainModelRowStyle()
                .start();
    }

    @Override
    protected void refreshDataOnActive() {
        leftSittingVisualMapper.refreshWhenActive();
        rightAttendanceVisualMapper.refreshWhenActive();
        rulesVisualMapper.refreshWhenActive();
    }
}
