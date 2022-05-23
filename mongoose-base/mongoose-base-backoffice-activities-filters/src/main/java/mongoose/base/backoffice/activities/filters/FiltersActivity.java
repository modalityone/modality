package mongoose.base.backoffice.activities.filters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import mongoose.base.backoffice.operations.entities.generic.CopySelectionRequest;
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

final class FiltersActivity extends EventDependentViewDomainActivity implements OperationActionFactoryMixin, ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final FiltersPresentationModel pm = new FiltersPresentationModel();

    @Override
    public FiltersPresentationModel getPresentationModel() { return pm; }

    private ReactiveVisualMapper<MoneyAccount> filtersVisualMapper;

    @Override
    public Node buildUi() {

        // FilterPane components
        Label searchLabel = new Label("Search filters");
        TextField filterSearchField = new TextField();

        VisualGrid filterGrid = new VisualGrid();
        filterGrid.visualResultProperty().bind(pm.filtersVisualResultProperty());

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

        // The FilterPane border wrapper
        VBox resultPaneBorder = new VBox();
        resultPaneBorder.getChildren().add(resultPane);
        resultPaneBorder.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        VBox.setMargin(resultPaneBorder, new Insets(20, 20, 20, 20));

        // The outer box of components.
        VBox outerVerticalBox = new VBox();
        outerVerticalBox.getChildren().add(filterPaneBorder);
        outerVerticalBox.getChildren().add(resultPaneBorder);
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
        /*
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
        */

        // Setting up the filter mapper that builds the content displayed in the filters listing
        filtersVisualMapper = ReactiveVisualMapper.<MoneyAccount>createPushReactiveChain(this)
                .always("{class: 'Filter', alias: 'fil', columns: 'id,name,description,isColumns,isCondition,isGroup,active,activityName,class,alias,columns,whereClause,groupByClause,havingClause,orderByClause,limitClause,ord', fields: 'id', orderBy: 'name desc'}")
                // .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                // .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("name like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filtersVisualResultProperty())
                // .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                // .setSelectedEntityHandler(entity -> graph.selectedMoneyAccount().set(entity))
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
