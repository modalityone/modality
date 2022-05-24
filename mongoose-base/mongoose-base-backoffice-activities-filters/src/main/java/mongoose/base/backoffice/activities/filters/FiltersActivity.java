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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;
import mongoose.base.shared.entities.Filter;
import mongoose.base.client.presentationmodel.HasSearchTextProperty;
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

    @Override
    public Node buildUi() {

        // FilterPane components
        Label filterSearchLabel = new Label("Select search filter");
        TextField filterSearchField = new TextField();
        ((HasSearchTextProperty) pm).searchTextProperty().bind(filterSearchField.textProperty());

        VisualGrid filterGrid = new VisualGrid();
        filterGrid.visualResultProperty().bind(pm.filtersVisualResultProperty());

        HBox filterPaneButtonBar = new HBox();
        filterPaneButtonBar.getChildren().add(new Button("Add"));
        filterPaneButtonBar.getChildren().add(new Button("Edit"));
        filterPaneButtonBar.getChildren().add(new Button("Delete"));
        filterPaneButtonBar.setSpacing(5);
        filterPaneButtonBar.setAlignment(Pos.BASELINE_RIGHT);

        VBox filterPane = new VBox();
        filterPane.getChildren().add(filterSearchLabel);
        filterPane.getChildren().add(filterSearchField);
        filterPane.getChildren().add(filterGrid);
        filterPane.getChildren().add(filterPaneButtonBar);
        filterPane.setSpacing(10);
        VBox.setMargin(filterPane, new Insets(20, 20, 20, 20));


        // FieldPane components
        Label fieldSearchLabel = new Label("Search fields");
        TextField fieldSearchField = new TextField();
        //((HasSearchTextProperty) pm).searchTextProperty().bind(fieldSearchField.textProperty());

        VisualGrid fieldGrid = new VisualGrid();
        fieldGrid.visualResultProperty().bind(pm.fieldsVisualResultProperty());

        // The FieldPane button bar
        HBox fieldPaneButtonBar = new HBox();
        fieldPaneButtonBar.getChildren().add(new Button("Add"));
        fieldPaneButtonBar.getChildren().add(new Button("Edit"));
        fieldPaneButtonBar.getChildren().add(new Button("Delete"));
        fieldPaneButtonBar.setSpacing(5);
        fieldPaneButtonBar.setAlignment(Pos.BASELINE_RIGHT);

        // The FieldPane container of components
        VBox fieldPane = new VBox();
        fieldPane.getChildren().add(fieldSearchLabel);
        fieldPane.getChildren().add(fieldSearchField);
        fieldPane.getChildren().add(fieldGrid);
        fieldPane.getChildren().add(fieldPaneButtonBar);
        fieldPane.setSpacing(10);
        VBox.setMargin(fieldPane, new Insets(20, 20, 20, 20));


        // Place the FilterPane and FieldPane side-by-side
        GridPane filterAndFieldPanes = new GridPane();
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);

        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);

        filterAndFieldPanes.getColumnConstraints().addAll(leftColumn,rightColumn);
        filterAndFieldPanes.setHgap(30);
        filterAndFieldPanes.add(filterPane, 0, 0);
        filterAndFieldPanes.add(fieldPane, 1, 0);


        // Place the filterAndFieldPanes in a border wrapper
        VBox filterAndFieldPaneBorder = new VBox();
        filterAndFieldPaneBorder.getChildren().add(filterAndFieldPanes);
        filterAndFieldPaneBorder.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        VBox.setMargin(filterAndFieldPaneBorder, new Insets(20, 20, 20, 20));


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
        outerVerticalBox.getChildren().add(filterAndFieldPaneBorder);
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

    private ReactiveVisualMapper<Filter> filtersVisualMapper;
    private ReactiveVisualMapper<Filter> fieldsVisualMapper;

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
        filtersVisualMapper = ReactiveVisualMapper.<Filter>createPushReactiveChain(this)
                .always("{class: 'Filter', alias: 'fil', fields: 'id', where: '!isColumns', orderBy: 'name asc'}")
                .setEntityColumns("[" +
                        "{label: 'Name', expression: 'name'}," +
                        "{label: 'Is Columns', expression: 'isColumns'}," +
                        "{label: 'Is Condition', expression: 'isCondition'}," +
                        "{label: 'Is Group', expression: 'isGroup'}," +
                        "{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        "{label: 'Class', expression: 'class'}," +
                        "{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        "{label: 'Having', expression: 'havingClause'}," +
                        "{label: 'OrderBy', expression: 'orderByClause'}," +
                        "{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                //.ifFalse(??, ??)
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filtersVisualResultProperty())
                // .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                // .setSelectedEntityHandler(entity -> graph.selectedMoneyAccount().set(entity))
                .start();

        fieldsVisualMapper = ReactiveVisualMapper.<Filter>createPushReactiveChain(this)
                .always("{class: 'Filter', alias: 'fil', fields: 'id', where: 'isColumns', orderBy: 'name asc'}")
                .setEntityColumns("[" +
                        "{label: 'Name', expression: 'name'}," +
                        "{label: 'Is Columns', expression: 'isColumns'}," +
                        "{label: 'Is Condition', expression: 'isCondition'}," +
                        "{label: 'Is Group', expression: 'isGroup'}," +
                        "{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        "{label: 'Class', expression: 'class'}," +
                        "{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        "{label: 'Having', expression: 'havingClause'}," +
                        "{label: 'OrderBy', expression: 'orderByClause'}," +
                        "{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.fieldsVisualResultProperty())
                // .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                // .setSelectedEntityHandler(entity -> graph.selectedMoneyAccount().set(entity))
                .start();
    }

    @Override
    protected void refreshDataOnActive() {
        filtersVisualMapper.refreshWhenActive();
        fieldsVisualMapper.refreshWhenActive();
    }
}
