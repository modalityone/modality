package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.operation.action.OperationAction;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;

import java.util.function.Function;

/**
 * Registration Dashboard activity - displays a list of registrations (Document entities)
 * with filtering, search, and management capabilities.
 * <p>
 * Features:
 * - Stats cards (Total, Confirmed, Pending, Cancelled)
 * - Search and filter functionality
 * - Quick filter chips
 * - Registration table with row selection
 * - Edit/Create modals for registration management
 * <p>
 * Based on RegistrationDashboardFull.jsx mockup.
 *
 * @author David Hello
 * @author Claude Code
 */
final class RegistrationActivity extends EventDependentViewDomainActivity implements
    OperationActionFactoryMixin,
    ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final RegistrationPresentationModel pm = new RegistrationPresentationModel();
    private RegistrationListView listView;

    @Override
    public RegistrationPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will be updated from the route
    }

    @Override
    protected void updateModelFromContextParameters() {
        super.updateModelFromContextParameters();
        // Considering the documentId parameter if present in the route
        Object documentId = getParameter("documentId");
        if (documentId != null) {
            EntityStore.create(getDataSourceModel())
                .<Document>executeQuery("select event.organization,ref from Document where id=? limit 1", documentId)
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
        Console.log("📋 RegistrationActivity.buildUi() called");

        // Create the main list view
        listView = new RegistrationListView(this, pm);
        Node mainContent = listView.buildUi();

        // Bind gantt selection for filtering
        pm.ganttSelectedObjectProperty().bind(FXGanttSelection.ganttSelectedObjectProperty());

        // Set up the mapper now that listView exists
        // (startLogic may have been called before buildUi)
        if (masterVisualMapper == null) {
            Console.log("📋 Setting up mapper in buildUi()");
            masterVisualMapper = listView.setupMasterVisualMapper();
        }

        // Create container with main content
        BorderPane container = new BorderPane();
        container.setCenter(mainContent);

        // Setting an Id for CSS styling
        container.setId("registration");

        return container;
    }

    /**
     * Creates an operation action for the currently selected document.
     */
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

    /**
     * Creates an operation action for the currently selected event.
     */
    private OperationAction newSelectedEventOperationAction(Function<Event, ?> operationRequestFactory) {
        return newOperationAction(
            () -> operationRequestFactory.apply(FXEvent.getEvent()),
            FXEvent.eventProperty()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Focus search field when activity resumes
        if (listView != null && listView.getSearchField() != null) {
            listView.getSearchField().requestFocus();
        }
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

    private ReactiveVisualMapper<Document> masterVisualMapper;

    @Override
    protected void startLogic() {
        Console.log("📋 RegistrationActivity.startLogic() called, listView=" + listView);
        // Set up the master visual mapper via the list view (if UI has been built)
        if (listView != null) {
            masterVisualMapper = listView.setupMasterVisualMapper();
        } else {
            Console.log("📋 WARNING: listView is null in startLogic!");
        }
    }

    @Override
    protected void refreshDataOnActive() {
        Console.log("📋 RegistrationActivity.refreshDataOnActive() called");
        // Ensure mapper is set up (in case startLogic was called before buildUi)
        if (masterVisualMapper == null && listView != null) {
            Console.log("📋 Setting up mapper in refreshDataOnActive (was null)");
            masterVisualMapper = listView.setupMasterVisualMapper();
        }
        if (masterVisualMapper != null) {
            Console.log("📋 Calling refreshWhenActive on mapper");
            masterVisualMapper.refreshWhenActive();
        } else {
            Console.log("📋 WARNING: masterVisualMapper is still null!");
        }
    }
}
