package mongoose.base.backoffice.activities.filters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import mongoose.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import mongoose.ecommerce.backoffice.operations.entities.document.SendLetterRequest;
import mongoose.ecommerce.backoffice.operations.entities.document.registration.*;
import mongoose.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsKnownRequest;
import mongoose.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUncheckedRequest;
import mongoose.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsUnknownRequest;
import mongoose.ecommerce.backoffice.operations.entities.document.security.ToggleMarkDocumentAsVerifiedRequest;
import mongoose.base.backoffice.operations.entities.generic.CopyAllRequest;
import mongoose.base.backoffice.operations.entities.generic.CopySelectionRequest;
import mongoose.event.backoffice.operations.routes.cloneevent.RouteToCloneEventRequest;
import mongoose.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import mongoose.base.shared.domainmodel.functions.AbcNames;
import mongoose.base.shared.entities.Document;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.framework.client.ui.action.operation.OperationActionFactoryMixin;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.util.layout.LayoutUtil;

import static dev.webfx.framework.shared.orm.dql.DqlStatement.fields;
import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;
import static dev.webfx.framework.client.ui.util.layout.LayoutUtil.setUnmanagedWhenInvisible;

final class FiltersActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin,
        ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final FiltersPresentationModel pm = new FiltersPresentationModel();

    @Override
    public FiltersPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    private Pane moneyAccountTableContainer;

    @Override
    public Node buildUi() {

        /*
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "filters", "Document");
        Pane container = ui.buildUi();
        setUpContextMenu(LayoutUtil.lookupChild(ui.getGroupMasterSlaveView().getMasterView(), n -> n instanceof VisualGrid), () -> newActionGroup(
                newOperationAction(() -> new SendLetterRequest(pm.getSelectedDocument(), container)),
                newSeparatorActionGroup("Registration",
                        newOperationAction(() -> new ToggleMarkDocumentAsReadRequest(pm.getSelectedDocument(), container), pm.selectedDocumentProperty()), // to update the i18n text when the selection change ->
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
        */

        // FilterPane components
        Label searchLabel = new Label("Search filters");
        TextField filterSearchField = new TextField();
        VisualGrid filterGrid = new VisualGrid();

        // The FilterPane button bar
        HBox filterPaneButtonBar = new HBox();
        filterPaneButtonBar.getChildren().add(new Button("Add"));
        filterPaneButtonBar.getChildren().add(new Button("Edit"));
        filterPaneButtonBar.getChildren().add(new Button("Delete"));
        filterPaneButtonBar.setSpacing(5);
        filterPaneButtonBar.setAlignment(Pos.BASELINE_RIGHT);

        // The FilterPane container of components
        VBox filterPane = new VBox();
        filterPane.getChildren().add(searchLabel);
        filterPane.getChildren().add(filterSearchField);
        filterPane.getChildren().add(filterGrid);
        filterPane.getChildren().add(filterPaneButtonBar);
        filterPane.setSpacing(10);
        VBox.setMargin(filterPane, new Insets(20, 20, 20, 20));

        // The FilterPane border wrapper
        VBox filterPaneBorder = new VBox();
        filterPaneBorder.getChildren().add(filterPane);
        filterPaneBorder.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        VBox.setMargin(filterPaneBorder, new Insets(20, 20, 20, 20));

        // ResultPane components
        Label resultLabel = new Label("Returned results");
        VisualGrid resultsGrid = new VisualGrid();

        // The ResultPane container of components
        VBox resultPane = new VBox();
        resultPane.getChildren().add(resultLabel);
        resultPane.getChildren().add(resultsGrid);
        resultPane.setSpacing(10);
        VBox.setMargin(resultPane, new Insets(20, 20, 20, 20));
        // resultPane.setPrefSize(300,300);
        // resultPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));

        // The FilterPane border wrapper
        VBox resultPaneBorder = new VBox();
        resultPaneBorder.getChildren().add(resultPane);
        resultPaneBorder.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        VBox.setMargin(resultPaneBorder, new Insets(20, 20, 20, 20));

        // The outer box of components.
        VBox outerVerticalBox = new VBox();
        outerVerticalBox.getChildren().add(filterPaneBorder);
        outerVerticalBox.getChildren().add(resultPaneBorder);

        /*
        VisualGrid moneyAccountTable = new VisualGrid();
        moneyAccountTableContainer = new HBox(moneyAccountTable);
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(new Tab("Filters", moneyAccountTableContainer));
        return tabPane;
        */
        return outerVerticalBox;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ui.onResume(); // activate search text focus on activity resume
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
        /*
        groupVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
        */
        //moneyAccountVisualMapper.refreshWhenActive();
    }
}
