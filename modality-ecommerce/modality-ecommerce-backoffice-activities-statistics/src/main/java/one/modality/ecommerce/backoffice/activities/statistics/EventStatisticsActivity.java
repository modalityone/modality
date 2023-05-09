package one.modality.ecommerce.backoffice.activities.statistics;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.dql.DqlClause;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.DeleteDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.EditDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.ToggleCancelDocumentLineRequest;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class EventStatisticsActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin,
        ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final StatisticsPresentationModel pm = new StatisticsPresentationModel();

    @Override
    public StatisticsPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    @Override
    public Node buildUi() {
        ConventionalUiBuilder ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "statistics", "DocumentLine");

        Pane container = ui.buildUi();

        setUpContextMenu(LayoutUtil.lookupChild(ui.getGroupMasterSlaveView().getMasterView(), n -> n instanceof VisualGrid), () -> newActionGroup(
                newOperationAction(() -> new SendLetterRequest(pm.getSelectedDocument(), container)),
                newSeparatorActionGroup(
                        newOperationAction(() -> new EditDocumentLineRequest(pm.getSelectedDocumentLine(), container)),
                        newOperationAction(() -> new ToggleCancelDocumentLineRequest(pm.getSelectedDocumentLine(), container)),
                        newOperationAction(() -> new DeleteDocumentLineRequest(pm.getSelectedDocumentLine(), container))
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new CopySelectionRequest(masterVisualMapper.getSelectedEntities(), masterVisualMapper.getEntityColumns())),
                        newOperationAction(() -> new CopyAllRequest(masterVisualMapper.getCurrentEntities(), masterVisualMapper.getEntityColumns()))
                )
        ));

        return container;
    }

    @Override
    public void onResume() {
        super.onResume();
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
    }

    @Override
    public void onPause() {
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<DocumentLine> leftGroupVisualMapper, masterVisualMapper;
    private ReactiveVisualMapper<Attendance> rightAttendanceVisualMapper;
    private StatisticsBuilder statisticsBuilder; // to avoid GC

    @Override
    protected void startLogic() {
        // Setting up the left group filter for the left content displayed in the group view
        leftGroupVisualMapper = ReactiveVisualMapper.<DocumentLine>createGroupReactiveChain(this, pm)
                .always("{class: 'DocumentLine', alias: 'dl'}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("document.event=?", eventId))
        ;

        rightAttendanceVisualMapper = ReactiveVisualMapper.<Attendance>createReactiveChain(this)
                .always("{class: 'Attendance', alias: 'a', where: 'present', orderBy: 'date'}")
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("documentLine.document.event=?", eventId))
                // Applying the condition and group selected by the user
                .ifNotNullOtherwiseEmpty(pm.conditionDqlStatementProperty(), conditionDqlStatement -> {
                    DqlClause where = conditionDqlStatement.getWhere();
                    if (where == null)
                        return DqlStatement.EMPTY_STATEMENT;
                    return where("a.[documentLine as dl].(" + where.getDql() + ')', where.getParameterValues());
                })
                .ifNotNullOtherwiseEmpty(pm.groupDqlStatementProperty(), groupDqlStatement -> {
                    DqlClause groupBy = groupDqlStatement.getGroupBy();
                    if (groupBy == null)
                        return DqlStatement.EMPTY_STATEMENT;
                    String dqlGroupBy = "documentLine.(" + groupBy.getDql() + ')';
                    return DqlStatement.parse("{columns: `" + dqlGroupBy + ",date,count(1)`, groupBy: `" + dqlGroupBy + ",date`}");
                })
        ;

        // Building the statistics final display result from the 2 above filters
        statisticsBuilder = new StatisticsBuilder(leftGroupVisualMapper, rightAttendanceVisualMapper, leftGroupVisualMapper.visualResultProperty()).start();

        // Setting up the master filter for the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<DocumentLine>createMasterReactiveChain(this, pm)
                .always("{class: 'DocumentLine', alias: 'dl', orderBy: 'document.ref,item.family.ord,site..ord,item.ord'}")
                // Always loading the fields required for viewing the booking details
                .always("{fields: `document.(" + BookingDetailsPanel.REQUIRED_FIELDS + ")`}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("document.event=?", eventId))
                .applyDomainModelRowStyle() // Colorizing the rows
                .start();
    }

    @Override
    protected void refreshDataOnActive() {
        leftGroupVisualMapper.refreshWhenActive();
        rightAttendanceVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
    }
}
