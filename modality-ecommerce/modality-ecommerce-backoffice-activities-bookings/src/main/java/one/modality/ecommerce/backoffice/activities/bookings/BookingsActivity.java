package one.modality.ecommerce.backoffice.activities.bookings;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.operations.entities.generic.AddNewSnapshotRequest;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.*;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsKnownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUncheckedRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUnknownRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsVerifiedRequest;
import one.modality.ecommerce.backoffice.operations.routes.bookings.RouteToNewBackOfficeBookingRequest;
import one.modality.base.client.gantt.visibility.fx.FXGanttVisibility;
import one.modality.base.client.gantt.visibility.GanttVisibility;
import one.modality.event.backoffice.operations.routes.cloneevent.RouteToCloneEventRequest;

import static dev.webfx.extras.util.layout.LayoutUtil.setUnmanagedWhenInvisible;
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
        Button newBookingButton = newButton(newOperationAction(() -> new RouteToNewBackOfficeBookingRequest(getEventId(), getHistory()))),
                cloneEventButton = newButton(newOperationAction(() -> new RouteToCloneEventRequest(getEventId(), getHistory())));
        ui.setLeftTopNodes(setUnmanagedWhenInvisible(newBookingButton));
        ui.setRightTopNodes(setUnmanagedWhenInvisible(cloneEventButton));

        Pane container = ui.buildUi();

        setUpContextMenu(LayoutUtil.lookupChild(ui.getGroupMasterSlaveView().getMasterView(), n -> n instanceof VisualGrid), () -> newActionGroup(
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

        return container;
    }

    // TODO move this into an interface
    private ActionGroup newSnapshotActionGroup(Pane container) {
        //List<Document> selectedEntities = masterVisualMapper.getSelectedEntities();
        //System.out.println("selectedEntities.size() = " + selectedEntities.size());
        return newActionGroup("Snapshot", true,
                newOperationAction(() -> new AddNewSnapshotRequest(masterVisualMapper.getSelectedEntities(), pm.getSelectedMaster().getOrganization(), container),  pm.selectedDocumentProperty()));
    }

    @Override
    public void onResume() {
        super.onResume();
        ui.onResume(); // activate search text focus on activity resume
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

    private ReactiveVisualMapper<Document> groupVisualMapper, masterVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        groupVisualMapper = ReactiveVisualMapper.<Document>createGroupReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd'}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                .start();

        // Setting up the master mapper that build the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<Document>createMasterPushReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd', orderBy: 'ref desc'}")
                // Always loading the fields required for viewing the booking details
                .always(fields(BookingDetailsPanel.REQUIRED_FIELDS))
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                // Applying the user search
                .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                        Character.isDigit(s.charAt(0)) ? where("ref = ?", Integer.parseInt(s))
                                : s.contains("@") ? where("lower(person_email) like ?", "%" + s.toLowerCase() + "%")
                                : where("person_abcNames like ?", AbcNames.evaluate(s, true)))
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
