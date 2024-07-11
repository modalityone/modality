package one.modality.ecommerce.backoffice.activities.bookings;

import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.YearWeek;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationAction;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.controls.masterslave.group.GroupMasterSlaveView;
import one.modality.base.backoffice.operations.entities.generic.AddNewSnapshotRequest;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.entities.util.filters.FilterSearchBar;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.*;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsKnownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsUncheckedRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsUnknownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsVerifiedRequest;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.function.Function;

import static dev.webfx.stack.orm.dql.DqlStatement.fields;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class BookingsActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin,
        ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final BookingsPresentationModel pm = new BookingsPresentationModel();

    @Override
    public BookingsPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    private FilterSearchBar filterSearchBar;

    @Override
    public Node buildUi() {
        // The container (shown under the events gantt canvas) is a border pane that will display the filter search bar
        // on top, and the group/master/slave view in the center.
        BorderPane container = new BorderPane();
        // We create the filter search bar and put it on top of the container.
        filterSearchBar = createFilterSearchBar("bookings", "Document", container, pm);
        container.setTop(filterSearchBar.buildUi());
        // We create the group/master/slave view and put it in the center of the container. The master view is the
        // bookings table and the group view is initially hidden, but if the user selects a group in the filter search
        // bar, this is the group view that is displayed and the master view is hidden at this point. However, if the
        // user selects a specific group in the group view, then the master view appears again below the group view on
        // top, and displays the bookings of that particular group. In all cases, the slave view is initially hidden but
        // if the user selects 1 specific booking in the master view, the slave view appears at below the master view
        // and display the details of that particular booking.
        GroupMasterSlaveView groupMasterSlaveView = GroupMasterSlaveView.createAndBind(pm, this);
        //BookingDetailsPanel bdp = groupMasterSlaveView.getSlaveView();
        container.setCenter(groupMasterSlaveView.buildUi());

        // We set up a context menu on the master view
        Node masterView = groupMasterSlaveView.getMasterView();
        setUpContextMenu(ControlUtil.lookupChild(masterView, n -> n instanceof VisualGrid), () -> newActionGroup(
                newSnapshotActionGroup(),
                newSelectedDocumentOperationAction(SendLetterRequest::new),
                newSeparatorActionGroup("Registration",
                        newSelectedDocumentOperationAction(ToggleMarkDocumentAsReadRequest::new),
                        newSelectedDocumentOperationAction(ToggleMarkDocumentAsWillPayRequest::new),
                        newSelectedDocumentOperationAction(ToggleCancelDocumentRequest::new),
                        newSelectedDocumentOperationAction(ToggleConfirmDocumentRequest::new),
                        newSelectedDocumentOperationAction(ToggleFlagDocumentRequest::new),
                        newSelectedDocumentOperationAction(ToggleMarkDocumentPassAsReadyRequest::new),
                        newSelectedDocumentOperationAction(MarkDocumentPassAsUpdatedRequest::new),
                        newSelectedDocumentOperationAction(ToggleMarkDocumentAsArrivedRequest::new)
                ),
                newSeparatorActionGroup("Security",
                        newSelectedDocumentOperationAction(MarkDocumentAsUncheckedRequest::new),
                        newSelectedDocumentOperationAction(MarkDocumentAsUnknownRequest::new),
                        newSelectedDocumentOperationAction(MarkDocumentAsKnownRequest::new),
                        newSelectedDocumentOperationAction(MarkDocumentAsVerifiedRequest::new)
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new CopySelectionRequest(masterVisualMapper.getSelectedEntities(), masterVisualMapper.getEntityColumns())),
                        newOperationAction(() -> new CopyAllRequest(masterVisualMapper.getCurrentEntities(), masterVisualMapper.getEntityColumns()))
                )
        ));

        pm.ganttSelectedObjectProperty().bind(FXGanttSelection.ganttSelectedObjectProperty());

        // Setting an Id for CSS styling
        container.setId("bookings");

        return container;
    }

    // TODO move this into an interface
    private ActionGroup newSnapshotActionGroup() {
        return newActionGroup("Snapshot", true,
                newOperationAction(() -> new AddNewSnapshotRequest(masterVisualMapper.getSelectedEntities(), pm.getSelectedMaster().getOrganization()),  pm.selectedDocumentProperty()));
    }

    private OperationAction newSelectedDocumentOperationAction(Function<Document, ?> operationRequestFactory) {
        return newOperationAction(() -> operationRequestFactory.apply(pm.getSelectedDocument()), /* to update the i18n text when the selection change -> */ pm.selectedDocumentProperty());
    }

    @Override
    public void onResume() {
        super.onResume();
        filterSearchBar.onResume(); // activate search text focus on activity resume
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
        FXGanttInterstice.setGanttIntersticeRequired(true);
    }

    @Override
    public void onPause() {
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        FXGanttInterstice.setGanttIntersticeRequired(false);
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<Document> groupVisualMapper, masterVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        groupVisualMapper = ReactiveVisualMapper.<Document>createGroupReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd'}")
                // Applying the event condition
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=?", organizationId))
                .ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day    -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class,  week   -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class,      year   -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("exists(select Attendance where documentLine.document=d and scheduledItem = ?)", si))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class,     event  -> where("event=?", event))
                .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-bookings-group"))
                .start();

        // Setting up the master mapper that build the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<Document>createMasterPushReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd', orderBy: 'ref desc'}")
                // Always loading the fields required for viewing the booking details
                .always(fields(BookingDetailsPanel.REQUIRED_FIELDS))
                // Applying the event condition
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=?", organizationId))
                .ifNotNull(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                //.ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day    -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class,  week   -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class,      year   -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("exists(select Attendance where documentLine.document=d and scheduledItem = ?)", si))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class,     event  -> where("event=?", event))
                // Applying the user search
                .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                        Character.isDigit(s.charAt(0)) ? where("ref = ?", Integer.parseInt(s))
                                : s.contains("@") ? where("lower(person_email) like ?", "%" + s.toLowerCase() + "%")
                                : where("person_abcNames like ?", AbcNames.evaluate(s, true)))
                .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-bookings-master"))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .start();
    }

    @Override
    protected void refreshDataOnActive() {
        groupVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
    }
}
