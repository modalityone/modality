package org.modality_project.ecommerce.backoffice.activities.bookings;

import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.action.ActionGroup;
import dev.webfx.framework.client.ui.action.operation.OperationActionFactoryMixin;
import dev.webfx.framework.client.ui.util.layout.LayoutUtil;
import dev.webfx.framework.shared.orm.dql.DqlStatement;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import org.modality_project.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import org.modality_project.base.backoffice.operations.entities.generic.CopyAllRequest;
import org.modality_project.base.backoffice.operations.entities.generic.CopySelectionRequest;
import org.modality_project.base.shared.domainmodel.functions.AbcNames;
import org.modality_project.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import org.modality_project.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.document.registration.*;
import org.modality_project.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsKnownRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUncheckedRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUnknownRequest;
import org.modality_project.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsVerifiedRequest;
import org.modality_project.ecommerce.backoffice.operations.routes.bookings.RouteToNewBackOfficeBookingRequest;
import org.modality_project.event.backoffice.operations.routes.cloneevent.RouteToCloneEventRequest;
import org.modality_project.base.backoffice.operations.entities.generic.AddNewSnapshotRequest;
import org.modality_project.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import org.modality_project.base.shared.entities.Document;
import org.modality_project.ecommerce.backoffice.operations.entities.document.registration.*;

import static dev.webfx.framework.client.ui.util.layout.LayoutUtil.setUnmanagedWhenInvisible;
import static dev.webfx.framework.shared.orm.dql.DqlStatement.fields;
import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;

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
                                : DqlStatement.where("person_abcNames like ?", AbcNames.evaluate(s, true)))
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
