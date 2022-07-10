package org.modality_project.base.client.entities.util.filters;

import dev.webfx.stack.framework.client.orm.reactive.dql.statement.conventions.HasConditionDqlStatementProperty;
import dev.webfx.stack.framework.client.ui.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.framework.shared.orm.dql.DqlStatement;
import dev.webfx.stack.framework.shared.orm.dql.DqlStatementBuilder;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.converters.FilterToDqlStatementConverter;
import org.modality_project.base.shared.entities.Filter;

public class FilterConditionChain extends HBox {

    private final ObservableList<EntityButtonSelector<Filter>> conditionSelectorChain = FXCollections.observableArrayList();

    private final FilterButtonSelectorFactoryMixin mixin;
    private final String activityName;
    private final String domainClassId;
    private final Pane parent;
    private final HasConditionDqlStatementProperty pm;
    private final Button newButton;

    public FilterConditionChain(FilterButtonSelectorFactoryMixin mixin, String activityName, String domainClassId, Pane parent, HasConditionDqlStatementProperty pm) {
        this.mixin = mixin;
        this.activityName = activityName;
        this.domainClassId = domainClassId;
        this.parent = parent;
        this.pm = pm;
        newButton = buildNewButton();
        getChildren().add(newButton);
        addConditionSelector();
    }

    private void addConditionSelector() {
        Property<DqlStatement> dqlStatementProperty = new SimpleObjectProperty<>();
        dqlStatementProperty.addListener(e -> updateDqlStatementProperty());

        EntityButtonSelector<Filter> conditionSelector = mixin.createConditionFilterButtonSelectorAndBind(activityName, domainClassId, parent, dqlStatementProperty);
        conditionSelectorChain.add(conditionSelector);
        conditionSelector.selectedItemProperty().addListener(e -> updateDqlStatementProperty());
        addToLeftOf(conditionSelector.getButton(), newButton);

        if (conditionSelectorChain.size() > 1) {
            Button removeButton = buildRemoveButton(conditionSelector);
            addToLeftOf(removeButton, newButton);
        }
    }

    private void addToLeftOf(Node nodeToAdd, Node nodeToLeftOf) {
        int index = getChildren().indexOf(nodeToLeftOf);
        getChildren().add(index, nodeToAdd);
    }

    private Button buildRemoveButton(EntityButtonSelector<Filter> filterToRemove) {
        Button removeButton = new Button("-");
        removeButton.prefWidthProperty().bind(newButton.widthProperty());
        removeButton.setOnAction(e -> {
            conditionSelectorChain.remove(filterToRemove);
            getChildren().remove(filterToRemove.getButton());
            getChildren().remove(removeButton);
            updateDqlStatementProperty();
        });
        return removeButton;
    }

    private Button buildNewButton() {
        Button newButton = new Button("+");
        newButton.setOnAction(e -> addConditionSelector());
        return newButton;
    }

    public void updateDqlStatementProperty() {
        Object domainClassId = conditionSelectorChain.get(0).getSelectedItem().getClassId();
        DqlStatementBuilder mergeBuilder = new DqlStatementBuilder(domainClassId);
        for (EntityButtonSelector<Filter> filterSelector : conditionSelectorChain) {
            Filter filter = filterSelector.selectedItemProperty().getValue();
            DqlStatement dqlStatement = FilterToDqlStatementConverter.toDqlStatement(filter);
            mergeBuilder.merge(dqlStatement);
        }
        DqlStatement result = mergeBuilder.build();
        pm.conditionDqlStatementProperty().setValue(result);
    }
}
