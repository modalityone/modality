package one.modality.ecommerce.backoffice.activities.bookings;

import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.YearWeek;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.operations.entities.generic.AddNewSnapshotRequest;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.*;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsKnownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUncheckedRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUnknownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsVerifiedRequest;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

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

    private ConventionalUiBuilder ui; // Keeping this reference for activity resume

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "bookings", "Document");

        // Adding new booking button on left and clone event on right of the filter search bar
        //Button newBookingButton = newButton(newOperationAction(() -> new RouteToNewBackOfficeBookingRequest(getEventId(), getHistory())));
                //cloneEventButton = newButton(newOperationAction(() -> new RouteToCloneEventRequest(getEventId(), getHistory())));
        //ui.setLeftTopNodes(setUnmanagedWhenInvisible(newBookingButton));
        //ui.setRightTopNodes(setUnmanagedWhenInvisible(cloneEventButton));

        Pane container = ui.buildUi();

        setUpContextMenu(ControlUtil.lookupChild(ui.getGroupMasterSlaveView().getMasterView(), n -> n instanceof VisualGrid), () -> newActionGroup(
                newSnapshotActionGroup(container),
                newOperationAction(() -> new SendLetterRequest(pm.getSelectedDocument(), container)),
                newSeparatorActionGroup("Registration",
                        newOperationAction(() -> new ToggleMarkDocumentAsReadRequest(pm.getSelectedDocument(), container), /* to update the i18n text when the selection change -> */ pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentAsWillPayRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleCancelDocumentRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleConfirmDocumentRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleFlagDocumentRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentPassAsReadyRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new MarkDocumentPassAsUpdatedRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentAsArrivedRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty())
                ),
                newSeparatorActionGroup("Security",
                        newOperationAction(() -> new ToggleMarkDocumentAsUncheckedRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentAsUnknownRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentAsKnownRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()),
                        newOperationAction(() -> new ToggleMarkDocumentAsVerifiedRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty())
                ),
                newSeparatorActionGroup(
                        newOperationAction(() -> new CopySelectionRequest(masterVisualMapper.getSelectedEntities(), masterVisualMapper.getEntityColumns())),
                        newOperationAction(() -> new CopyAllRequest(masterVisualMapper.getCurrentEntities(), masterVisualMapper.getEntityColumns()))
                )
        ));

        pm.ganttSelectedObjectProperty().bind(FXGanttSelection.ganttSelectedObjectProperty());

        // for CSS styling
        container.setId("bookings");

        return container;
    }

    // TODO move this into an interface
    private ActionGroup newSnapshotActionGroup(Pane container) {
        return newActionGroup("Snapshot", true,
                newOperationAction(() -> new AddNewSnapshotRequest(masterVisualMapper.getSelectedEntities(), pm.getSelectedMaster().getOrganization(), container),  pm.selectedDocumentProperty()));
    }

    @Override
    public void onResume() {
        super.onResume();
        ui.onResume(); // activate search text focus on activity resume
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
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day   -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class,  week  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class,      year  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class,     event -> where("event=?", event))
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
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day   -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class,  week  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class,      year  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class,     event -> where("event=?", event))
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
