package mongoose.base.backoffice.activities.filters;

import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.framework.client.ui.action.ActionGroup;
import dev.webfx.framework.client.ui.action.operation.OperationActionFactoryMixin;
import dev.webfx.framework.shared.orm.domainmodel.DomainClass;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.dql.DqlStatement;
import dev.webfx.framework.shared.orm.dql.DqlStatementBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import mongoose.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import mongoose.base.backoffice.operations.entities.filters.AddNewFieldsRequest;
import mongoose.base.backoffice.operations.entities.filters.AddNewFilterRequest;
import mongoose.base.backoffice.operations.entities.filters.DeleteFilterRequest;
import mongoose.base.backoffice.operations.entities.filters.EditFilterRequest;
import mongoose.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import mongoose.base.client.presentationmodel.HasSearchTextProperty;
import mongoose.base.shared.entities.Filter;

import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.framework.shared.orm.dql.DqlStatement.where;

final class FiltersActivity extends EventDependentViewDomainActivity implements OperationActionFactoryMixin, ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final FiltersPresentationModel pm = new FiltersPresentationModel();

    @Override
    public FiltersPresentationModel getPresentationModel() { return pm; }

    private ObjectProperty<Filter> selectedFilter = new SimpleObjectProperty<>();

    private VBox outerVerticalBox;

    @Override
    public Node buildUi() {

        // FilterPane components
        Label filterSearchLabel = new Label("Select search filter");
        Label classLabel = new Label("Class");
        ComboBox<String> classComboBox = buildClassComboBox();
        pm.filterClassProperty().bind(classComboBox.valueProperty());
        TextField filterSearchField = new TextField();
        ((HasSearchTextProperty) pm).searchTextProperty().bind(filterSearchField.textProperty());
        Button addNewFilterButton = newButton(newOperationAction(() -> new AddNewFilterRequest(getEventStore(), outerVerticalBox)));
        HBox filterSearchRow = new HBox(classLabel, classComboBox, filterSearchField, addNewFilterButton);
        HBox.setHgrow(filterSearchField, Priority.ALWAYS);

        VisualGrid filterGrid = new VisualGrid();
        filterGrid.visualResultProperty().bind(pm.filtersVisualResultProperty());
        setUpContextMenu(filterGrid, this::createFilterGridContextMenuActionGroup);

        VBox filterPane = new VBox();
        filterPane.getChildren().add(filterSearchLabel);
        filterPane.getChildren().add(filterSearchRow);
        filterPane.getChildren().add(filterGrid);
        filterPane.setSpacing(10);
        VBox.setMargin(filterPane, new Insets(20, 20, 20, 20));

        // FieldPane components
        Label fieldSearchLabel = new Label("Search fields");
        TextField fieldSearchField = new TextField();
        Button addNewFieldsButton = newButton(newOperationAction(() -> new AddNewFieldsRequest(getEventStore(), outerVerticalBox)));
        HBox fieldsSearchRow = new HBox(fieldSearchField, addNewFieldsButton);
        HBox.setHgrow(fieldSearchField, Priority.ALWAYS);
        //((HasSearchTextProperty) pm).searchTextProperty().bind(fieldSearchField.textProperty());

        VisualGrid fieldGrid = new VisualGrid();
        fieldGrid.visualResultProperty().bind(pm.fieldsVisualResultProperty());
        /*selectedFilter.addListener(e -> {
            boolean disabled = selectedFilter.get() == null;
            fieldGrid.setDisable(disabled);
            addNewFilterButton.setDisable(disabled);
        });*/
        fieldGrid.disableProperty().bind(selectedFilter.isNull());
        pm.filtersVisualSelectionProperty().bind(filterGrid.visualSelectionProperty());
        pm.fieldsVisualSelectionProperty().bind(fieldGrid.visualSelectionProperty());
        fieldGrid.visualSelectionProperty().addListener(e -> populateFilterTable());

        // The FieldPane container of components
        VBox fieldPane = new VBox();
        fieldPane.getChildren().add(fieldSearchLabel);
        fieldPane.getChildren().add(fieldsSearchRow);
        fieldPane.getChildren().add(fieldGrid);
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
        resultsGrid.visualResultProperty().bind(pm.filterFieldsVisualResultProperty());

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
        outerVerticalBox = new VBox();
        outerVerticalBox.getChildren().add(filterAndFieldPaneBorder);
        outerVerticalBox.getChildren().add(resultPaneBorder);
        return outerVerticalBox;
    }

    private ComboBox<String> buildClassComboBox() {
        ComboBox<String> classComboBox = new ComboBox<>();
        classComboBox.setItems(FXCollections.observableList(listClasses()));
        classComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String s) {
                return s != null ? s : "<any>";
            }

            @Override
            public String fromString(String s) {
                return s;
            }
        });
        return classComboBox;
    }

    private List<String> listClasses() {
        List<String> allClassNames = getDomainModel().getAllClasses().stream()
                .map(DomainClass::getName)
                .collect(Collectors.toList());

        allClassNames.add(0, null);
        return allClassNames;
    }

    private ActionGroup createFilterGridContextMenuActionGroup() {
        return newActionGroup(
                newOperationAction(() -> new EditFilterRequest(selectedFilter.get(), outerVerticalBox)),
                newOperationAction(() -> new DeleteFilterRequest(selectedFilter.get(), outerVerticalBox))
        );
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
    private ReactiveVisualMapper<Entity> filterFieldsResultVisualMapper;

    @Override
    protected void startLogic() {

        // Setting up the filter mapper that builds the content displayed in the filters listing
        filtersVisualMapper = ReactiveVisualMapper.<Filter>createPushReactiveChain(this)
                .always("{class: 'Filter', alias: 'fil', fields: 'id', where: '!isColumns', orderBy: 'name asc'}")
                .setEntityColumns("[" +
                        "{label: 'Name', expression: 'name'}," +
                        "{label: 'Is Columns', expression: 'isColumns'}," +
                        //"{label: 'Is Condition', expression: 'isCondition'}," +
                        "{label: 'Is Group', expression: 'isGroup'}," +
                        //"{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        "{label: 'Class', expression: 'class'}," +
                        "{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        //"{label: 'Having', expression: 'havingClause'}," +
                        //"{label: 'OrderBy', expression: 'orderByClause'}," +
                        //"{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .ifTrimNotEmpty(pm.filterClassProperty(), s -> where("lower(class) = ?", s.toLowerCase()))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filtersVisualResultProperty())
                .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                .setSelectedEntityHandler(entity -> selectedFilter.set(entity))
                .addEntitiesHandler(entities -> {
                    // If the filter table is cleared (e.g. by the user entering text in the search field) set the selected filter property to null
                    if (entities.isEmpty()) {
                        selectedFilter.set(null);
                    }
                })
                .start();

        fieldsVisualMapper = ReactiveVisualMapper.<Filter>createPushReactiveChain(this)
                .always("{class: 'Filter', alias: 'fil', fields: 'id', where: 'isColumns', orderBy: 'name asc'}")
                .setEntityColumns("[" +
                        "{label: 'Name', expression: 'name'}," +
                        "{label: 'Is Columns', expression: 'isColumns'}," +
                        //"{label: 'Is Condition', expression: 'isCondition'}," +
                        "{label: 'Is Group', expression: 'isGroup'}," +
                        //"{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        "{label: 'Class', expression: 'class'}," +
                        "{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        //"{label: 'Having', expression: 'havingClause'}," +
                        //"{label: 'OrderBy', expression: 'orderByClause'}," +
                        //"{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .always(selectedFilter, s -> s != null ? where("lower(class) = ?", s.getClassId().toString().toLowerCase()) : where("1 = 0"))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.fieldsVisualResultProperty())
                // .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                // .setSelectedEntityHandler(entity -> graph.selectedMoneyAccount().set(entity))
                .start();
    }

    private void populateFilterTable() {
        String selectedClass = selectedFilter.get().getClassId().toString();
        String columns = getSelectedColumns();

        filterFieldsResultVisualMapper = ReactiveVisualMapper.<Entity>createPushReactiveChain(this)
                .always("{class: '" + selectedClass + "', alias: 'ma', columns: '" + columns + "', fields: 'id', orderBy: 'name desc'}")
                .always(selectedFilter, s -> s != null ? where(s.getWhereClause()) : null)
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("name like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filterFieldsVisualResultProperty())
                .start();
    }

    private int getColumnIndex(String columnName, VisualResult visualResultProperty) {
        int classColumnIndex = 0;
        for (VisualColumn column : visualResultProperty.getColumns()) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                break;
            }
            classColumnIndex++;
        }
        return classColumnIndex;
    }

    private String getSelectedColumns() {
        VisualSelection selection = pm.fieldsVisualSelectionProperty().get();
        if (selection != null && !selection.getSelectedRows().isEmpty()) {
            int selectedRow = selection.getSelectedRow();
            VisualResult visualResultProperty = pm.fieldsVisualResultProperty().get();
            int classColumnIndex = getColumnIndex("columns", visualResultProperty);
            return visualResultProperty.getValue(selectedRow, classColumnIndex).toString();
        } else {
            return "";
        }
    }

    protected DqlStatement getFiltersResultDqlStatementBuilder() {
        System.out.print("GOT HERE 1------");
        DomainClass domainClass = filtersVisualMapper.getSelectedEntity().getDomainClass();
        DqlStatementBuilder builder = new DqlStatementBuilder(domainClass);
        builder.setColumns(filtersVisualMapper.getSelectedEntity().getColumns());
        return builder.build();
    }

    protected DqlStatement getFieldsResultDqlStatementBuilder() {
        System.out.print("GOT HERE 2------");
        DomainClass domainClass = fieldsVisualMapper.getSelectedEntity().getDomainClass();
        DqlStatementBuilder builder = new DqlStatementBuilder(domainClass);
        builder.setColumns(fieldsVisualMapper.getSelectedEntity().getColumns());
        return builder.build();
        // return new DqlStatementBuilder(fieldsVisualMapper.getSelectedEntity().getDomainClass()).setColumns(fieldsVisualMapper.getSelectedEntity().getColumns()).build()
    }

    @Override
    protected void refreshDataOnActive() {
        filtersVisualMapper.refreshWhenActive();
        fieldsVisualMapper.refreshWhenActive();
        filterFieldsResultVisualMapper.refreshWhenActive();
    }
}
