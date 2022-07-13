package org.modality_project.base.backoffice.activities.filters;

import dev.webfx.extras.cell.renderer.TextRenderer;
import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.controls.grid.SkinnedVisualGrid;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DomainClass;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.modality_project.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import org.modality_project.base.backoffice.operations.entities.filters.*;
import org.modality_project.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import org.modality_project.base.shared.entities.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.*;

final class FiltersActivity extends EventDependentViewDomainActivity implements OperationActionFactoryMixin, ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final FiltersPresentationModel pm = new FiltersPresentationModel();

    @Override
    public FiltersPresentationModel getPresentationModel() { return pm; }

    private final ObjectProperty<Filter> selectedFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<Filter> selectedFields = new SimpleObjectProperty<>();

    private final Label statusLabel = new Label();
    private final StackPane container = new StackPane();

    @Override
    public Node buildUi() {
        // FilterPane components
        Label filterSearchLabel = new Label("Select search filter");
        Label classLabel = new Label("Class: ");
        ButtonSelector<DomainClass> classSelector = new ButtonSelector<>(this, container) {
            private final List<DomainClass> allClasses = getDomainModel().getAllClasses();
            private List<DomainClass> searchedClasses;
            private final VisualGrid dialogVisualGrid = new SkinnedVisualGrid(); // Better rendering in desktop JavaFX (but might be slower in web version)
            {
                dialogVisualGrid.setHeaderVisible(false);
                dialogVisualGrid.setCursor(Cursor.HAND);
                BorderPane.setAlignment(dialogVisualGrid, Pos.TOP_LEFT);
                updateDialogVisualGrid();
                dialogVisualGrid.visualSelectionProperty().addListener((observable, oldValue, vs) -> {
                    setSelectedItem(vs == null || vs.getSelectedRow() == 0 ? null : searchedClasses.get(vs.getSelectedRow() - 1));
                    pm.filterClassProperty().set(toText(getSelectedItem()));
                    closeDialog();
                });
                FXProperties.runOnPropertiesChange(this::updateDialogVisualGrid, searchTextProperty());
            }
            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                return new Label(toText(getSelectedItem()));
            }

            @Override
            protected void startLoading() {}

            @Override
            protected Region getOrCreateDialogContent() {
                return dialogVisualGrid;
            }

            private String toText(DomainClass domainClass) {
                return domainClass == null ? "<Any>" : domainClass.getName();
            }

            private void updateDialogVisualGrid() {
                String classSearch = searchTextProperty().get();
                searchedClasses = allClasses.stream().filter(c -> classSearch == null || c.getName().toLowerCase().contains(classSearch.toLowerCase())).collect(Collectors.toList());
                VisualResultBuilder vsb = VisualResultBuilder.create(searchedClasses.size() + 1, VisualColumn.create(TextRenderer.SINGLETON));
                vsb.setValue(0, 0, toText(null));
                for (int i = 0; i < searchedClasses.size(); i++)
                    vsb.setValue(i + 1, 0, toText(searchedClasses.get(i)));
                dialogVisualGrid.setVisualResult(vsb.build());
            }
        };
        TextField filterSearchField = new TextField();
        pm.searchTextProperty().bind(filterSearchField.textProperty());
        Button addNewFilterButton = newButton(newOperationAction(() -> new AddNewFilterRequest(getEventStore(), container)));
        HBox filterSearchRow = new HBox(classLabel, classSelector.getButton(), filterSearchField, addNewFilterButton);
        HBox.setHgrow(filterSearchField, Priority.ALWAYS);
        filterSearchRow.setAlignment(Pos.CENTER);

        VisualGrid filterGrid = new VisualGrid();
        filterGrid.visualResultProperty().bind(pm.filtersVisualResultProperty());
        setUpContextMenu(filterGrid, this::createFilterGridContextMenuActionGroup);

        VBox filterPane = new VBox(filterSearchLabel, filterSearchRow, filterGrid);
        filterPane.setSpacing(10);
        VBox.setMargin(filterPane, new Insets(20, 20, 20, 20));

        // FieldPane components
        Label fieldSearchLabel = new Label("Search fields");
        TextField fieldSearchField = new TextField();
        //BooleanBinding isSelectedFilterNull = selectedFilter.isNull(); // Not yet emulated by WebFX
        ObservableValue<Boolean> isSelectedFilterNull = FXProperties.compute(selectedFilter, Objects::isNull); // WebFX replacement
        fieldSearchField.disableProperty().bind(isSelectedFilterNull);
        Button addNewFieldsButton = newButton(newOperationAction(() -> new AddNewFieldsRequest(getEventStore(), container)));
        HBox fieldsSearchRow = new HBox(fieldSearchField, addNewFieldsButton);
        HBox.setHgrow(fieldSearchField, Priority.ALWAYS);
        pm.fieldsSearchTextProperty().bind(fieldSearchField.textProperty());

        VisualGrid fieldGrid = new VisualGrid();
        fieldGrid.visualResultProperty().bind(pm.fieldsVisualResultProperty());
        fieldGrid.disableProperty().bind(isSelectedFilterNull);
        pm.filtersVisualSelectionProperty().bind(filterGrid.visualSelectionProperty());
        pm.fieldsVisualSelectionProperty().bind(fieldGrid.visualSelectionProperty());
        fieldGrid.visualSelectionProperty().addListener(e -> populateFilterTable());
        setUpContextMenu(fieldGrid, this::createFieldsGridContextMenuActionGroup);

        // The FieldPane container of components
        VBox fieldPane = new VBox(fieldSearchLabel, fieldsSearchRow, fieldGrid);
        fieldPane.setSpacing(10);
        VBox.setMargin(fieldPane, new Insets(20, 20, 20, 20));

        // Place the FilterPane and FieldPane side-by-side
        GridPane filterAndFieldPanes = new GridPane();
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);

        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);

        filterAndFieldPanes.getColumnConstraints().addAll(leftColumn, rightColumn);
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
        statusLabel.setPadding(new Insets(0, 0, 0, 8));
        HBox resultAndStatusRow = new HBox(resultLabel, statusLabel);
        VisualGrid resultsGrid = new VisualGrid();
        resultsGrid.visualResultProperty().bind(pm.filterFieldsVisualResultProperty());

        // The ResultPane container of components
        VBox resultPane = new VBox();
        resultPane.getChildren().add(resultAndStatusRow);
        resultPane.getChildren().add(resultsGrid);
        resultPane.setSpacing(10);
        VBox.setMargin(resultPane, new Insets(20, 20, 20, 20));

        // The FilterPane border wrapper
        VBox resultPaneBorder = new VBox();
        resultPaneBorder.getChildren().add(resultPane);
        resultPaneBorder.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        VBox.setMargin(resultPaneBorder, new Insets(20, 20, 20, 20));

        SplitPane splitPane = new SplitPane(filterAndFieldPaneBorder, resultPaneBorder);
        splitPane.setOrientation(Orientation.VERTICAL);
        container.getChildren().add(splitPane);
        return container;
    }

/*
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
*/
    private ActionGroup createFilterGridContextMenuActionGroup() {
        return newActionGroup(
                newOperationAction(() -> new EditFilterRequest(selectedFilter.get(), container)),
                newOperationAction(() -> new DeleteFilterRequest(selectedFilter.get(), container))
        );
    }

    private ActionGroup createFieldsGridContextMenuActionGroup() {
        return newActionGroup(
                newOperationAction(() -> new EditFieldsRequest(selectedFields.get(), container)),
                newOperationAction(() -> new DeleteFieldsRequest(selectedFields.get(), container))
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
                        //"{label: 'Is Columns', expression: 'isColumns'}," +
                        //"{label: 'Is Condition', expression: 'isCondition'}," +
                        //"{label: 'Is Group', expression: 'isGroup'}," +
                        //"{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        "{label: 'Class', expression: 'class'}," +
                        //"{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        "{label: 'Having', expression: 'havingClause'}," +
                        //"{label: 'OrderBy', expression: 'orderByClause'}," +
                        "{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .ifTrimNotEmpty(pm.filterClassProperty(), s -> where("lower(class) = ?", s.toLowerCase()))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filtersVisualResultProperty())
                .setVisualSelectionProperty(pm.filtersVisualSelectionProperty())
                .setSelectedEntityHandler(selectedFilter::set)
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
                        //"{label: 'Is Columns', expression: 'isColumns'}," +
                        //"{label: 'Is Condition', expression: 'isCondition'}," +
                        "{label: 'Is Group', expression: 'isGroup'}," +
                        //"{label: 'Is Active', expression: 'active'}," +
                        "{label: 'Activity Name', expression: 'activityName'}," +
                        //"{label: 'Class', expression: 'class'}," +
                        "{label: 'Columns', expression: 'columns'}," +
                        "{label: 'Where', expression: 'whereClause'}," +
                        "{label: 'GroupBy', expression: 'groupByClause'}," +
                        //"{label: 'Having', expression: 'havingClause'}," +
                        "{label: 'OrderBy', expression: 'orderByClause'}" +
                        //"{label: 'Limit', expression: 'limitClause'}" +
                        "]")
                .ifTrimNotEmpty(pm.fieldsSearchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .always(selectedFilter, s -> s != null ? where("lower(class) = ?", s.getClassId().toString().toLowerCase()) : where("1 = 0"))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.fieldsVisualResultProperty())
                .setVisualSelectionProperty(pm.fieldsVisualSelectionProperty())
                .setSelectedEntityHandler(selectedFields::set)
                .addEntitiesHandler(entities -> {
                    // If the fields' table is cleared (e.g. by the user entering text in the search field) set the selected fields property to null
                    if (entities.isEmpty()) {
                        selectedFields.set(null);
                    }
                })
                .start();
    }

    private void populateFilterTable() {
        String selectedClass = selectedFilter.get().getClassId().toString();
        String columns = selectedFields.get().getColumns();
        String orderBy = buildOrderByClause();

        String status = "Loading " + selectedFields.get().getName() + " columns for " + selectedClass + ".";
        displayStatus(status);

        filterFieldsResultVisualMapper = ReactiveVisualMapper.<Entity>createPushReactiveChain(this)
                .always("{class: '" + selectedClass + "', alias: 'ma', columns: '" + columns + "', fields: 'id'" + orderBy + ", limit: 100}")
                .ifNotNull(selectedFilter, s -> where(s.getWhereClause()))
                .ifNotNull(selectedFilter, s -> limit(s.getLimitClause()))
                .ifNotNull(selectedFilter, s -> s.getOrderByClause() != null && !s.getOrderByClause().isEmpty() ? parse("orderBy: '" + s.getOrderByClause() + "'") : null)
                .ifNotNull(pm.organizationIdProperty(), organization -> where("organization=?", organization))
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("name like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .visualizeResultInto(pm.filterFieldsVisualResultProperty())
                .addEntitiesHandler(entities -> displayStatus(entities.size() + " rows displayed."))
                .start();
    }

    private String buildOrderByClause() {
        if (selectedFields.get() == null) {
            return "";
        }

        String orderBy = selectedFields.get().getOrderByClause();
        if (orderBy != null && !orderBy.isEmpty()) {
            return ", orderBy: '" + orderBy + "'";
        } else {
            return "";
        }
    }

    private void displayStatus(String status) {
        statusLabel.setText(status);
    }

/*
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
*/

    @Override
    protected void refreshDataOnActive() {
        filtersVisualMapper.refreshWhenActive();
        fieldsVisualMapper.refreshWhenActive();
        filterFieldsResultVisualMapper.refreshWhenActive();
    }
}
