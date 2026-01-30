package one.modality.booking.backoffice.activities.bookings;

import dev.webfx.extras.action.ActionBinder;
import dev.webfx.extras.action.ActionGroup;
import dev.webfx.extras.operation.action.OperationAction;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.YearWeek;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.controls.masterslave.group.GroupMasterSlaveView;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.operations.entities.generic.AddNewSnapshotRequest;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.entities.filters.FilterSearchBar;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.backoffice.operations.entities.document.registration.ShowNewBookingEditorRequest;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.*;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsKnownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsUncheckedRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsUnknownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.MarkDocumentAsVerifiedRequest;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;

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
    private FilterSearchBar filterSearchBar;

    @Override
    public BookingsPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from the route
    }

    @Override
    protected void updateModelFromContextParameters() {
        super.updateModelFromContextParameters();
        // Considering the documentId parameter if present in the route
        Object documentId = getParameter("documentId");
        if (documentId != null) {
            EntityStore.create(getDataSourceModel())
                .<Document>executeQuery("select event.organization,ref from Document where id=$1 limit 1", documentId)
                .onSuccess(documents -> {
                    Document document = Collections.first(documents);
                    if (document != null) {
                        // TODO: check that the user can actually access this organization and event
                        FXOrganizationId.setOrganizationId(document.getEvent().getOrganizationId());
                        FXEventId.setEventId(document.getEventId());
                        pm.setSearchText(Strings.toSafeString(document.getRef()));
                    }
                });
        }
    }

    @Override
    public Node buildUi() {
        // The container (shown under the events gantt canvas) is a border pane that will display the filter search bar
        // on top, and the group/master/slave view in the center.
        BorderPane container = new BorderPane();
        // We create the filter search bar
        filterSearchBar = createFilterSearchBar("bookings", "Document", container, pm);
        // and a hyperlink to create a new booking, we put the filter search bar and the hyperlink on top of
        Hyperlink newBookingLink = ActionBinder.newActionHyperlink(newSelectedEventOperationAction(ShowNewBookingEditorRequest::new));
        HBox filterSearchBox = filterSearchBar.buildUi();
        // and put them together on top of the container.
        HBox.setHgrow(filterSearchBox, Priority.ALWAYS);
        HBox filterAndLinkBox = new HBox(10, filterSearchBox, newBookingLink);
        filterAndLinkBox.setAlignment(Pos.CENTER);
        container.setTop(filterAndLinkBox);
        // We create the group/master/slave view and put it in the center of the container. The master view is the
        // booking table. The group view is initially hidden, but if the user selects a group in the filter search
        // bar, this is the group view that is displayed and the master view is hidden at this point. However, if the
        // user selects a specific group in the group view, then the master view appears again below the group view on
        // top and displays the bookings of that particular group. In all cases, the slave view is initially hidden, but
        // if the user selects 1 specific booking in the master view, the slave view appears at below the master view
        // and displays the details of that particular booking.
        GroupMasterSlaveView groupMasterSlaveView = GroupMasterSlaveView.createAndBind(pm, this);
        container.setCenter(groupMasterSlaveView.buildUi());

        // We set up a context menu on the master view
        Node masterView = groupMasterSlaveView.getMasterView();
        setUpContextMenu(Controls.lookupChild(masterView, n -> n instanceof VisualGrid), () -> newActionGroup(
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
            newOperationAction(() -> new AddNewSnapshotRequest(masterVisualMapper.getSelectedEntities(), pm.getSelectedMaster() == null ? null : pm.getSelectedMaster().getOrganization()), pm.selectedDocumentProperty()));
    }

    private OperationAction newSelectedEventOperationAction(Function<Event, ?> operationRequestFactory) {
        return newOperationAction(
            // Creating a new operation request associated with the selected document each time the user clicks on this action
            () -> operationRequestFactory.apply(FXEvent.getEvent()),
            // Refreshing the graphical properties of this action (through i18n) each time the user selects another document,
            FXEvent.eventProperty()
        );
    }

    private OperationAction newSelectedDocumentOperationAction(Function<Document, ?> operationRequestFactory) {
        return newOperationAction(
            // Creating a new operation request associated with the selected document each time the user clicks on this action
            () -> operationRequestFactory.apply(pm.getSelectedDocument()),
            // Refreshing the graphical properties of this action (through i18n) each time the user selects another document,
            pm.selectedDocumentProperty(),
            // or when the server refreshes the data, in particular on push notification after that action has been
            // executed (ex: "Confirm" => confirmed=true in the database => server push => "Unconfirm").
            pm.masterVisualResultProperty()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        filterSearchBar.onResume(); // activate search text focus on activity resume
        FXGanttVisibility.showAllEvents();
        FXGanttInterstice.showGanttInterstice();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXGanttVisibility.resetToDefault();
        FXGanttInterstice.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<Document> groupVisualMapper, masterVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that builds the content displayed in the group view
        groupVisualMapper = ReactiveVisualMapper.<Document>createGroupReactiveChain(this, pm)
            .always( // language=JSON5
                "{class: 'Document', alias: 'd'}")
            // Applying the event condition
            //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
            .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=$1", organizationId))
            .ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day -> where("exists(select Attendance where documentLine.document=d and date = $1)", day))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class, week -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfWeek(week), TimeUtil.getLastDayOfWeek(week)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class, year -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfYear(year), TimeUtil.getLastDayOfYear(year)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("exists(select Attendance where documentLine.document=d and scheduledItem = $1)", si))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class, event -> where("event=$1", event))
            .setResultCacheEntry("modality/ecommerce/bookings/group-documents")
            .start();

        // Setting up the master mapper that builds the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<Document>createMasterPushReactiveChain(this, pm)
            .always( // language=JSON5
                "{class: 'Document', alias: 'd', orderBy: 'ref desc'}")
            // Always loading the fields required for viewing the booking details
            .always(fields(BookingDetailsPanel.REQUIRED_FIELDS))
            // Applying the event condition
            //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
            .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=$1", organizationId))
            .ifNotNull(pm.eventIdProperty(), eventId -> where("event=$1", eventId))
            //.ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day -> where("exists(select Attendance where documentLine.document=d and date = $1)", day))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class, week -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfWeek(week), TimeUtil.getLastDayOfWeek(week)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class, year -> where("exists(select Attendance where documentLine.document=d and date >= $1 and date <= $2)", TimeUtil.getFirstDayOfYear(year), TimeUtil.getLastDayOfYear(year)))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("exists(select Attendance where documentLine.document=d and scheduledItem = $1)", si))
            .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class, event -> where("event=$1", event))
            // Applying the user search
            .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                Character.isDigit(s.charAt(0)) ? where("ref = $1", Integer.parseInt(s))
                    : s.contains("@") ? where("lower(person_email) like $1", "%" + s.toLowerCase() + "%")
                    : where("person_abcNames like $1", AbcNames.evaluate(s, true)))
            .setResultCacheEntry("modality/ecommerce/bookings/documents")
            .applyDomainModelRowStyle() // Colorizing the rows
            .autoSelectSingleRow() // When the result is a single row, automatically select it
            .start();
    }

    @Override
    protected void refreshDataOnActive() {
        groupVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
    }
}
