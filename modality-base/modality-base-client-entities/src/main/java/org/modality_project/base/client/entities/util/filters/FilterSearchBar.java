package org.modality_project.base.client.entities.util.filters;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasColumnsDqlStatementProperty;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasConditionDqlStatementProperty;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasGroupDqlStatementProperty;
import org.modality_project.base.client.presentationmodel.HasSearchTextProperty;
import org.modality_project.base.shared.entities.Filter;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.util.scene.SceneUtil;

import static dev.webfx.stack.ui.util.layout.LayoutUtil.setHGrowable;
import static dev.webfx.stack.ui.util.layout.LayoutUtil.setMaxHeightToInfinite;

public final class FilterSearchBar {

    private final FilterConditionChain conditionChain;
    private final EntityButtonSelector<Filter> groupSelector, columnsSelector;
    private final TextField searchBox; // Keeping this reference to activate focus on activity resume

    public FilterSearchBar(FilterButtonSelectorFactoryMixin mixin, String activityName, String domainClassId, Pane parent, Object pm) {
        if (pm instanceof HasConditionDqlStatementProperty)
            conditionChain = new FilterConditionChain(mixin, activityName, domainClassId, parent, (HasConditionDqlStatementProperty) pm);
        else
            conditionChain = null;
        if (pm instanceof HasGroupDqlStatementProperty)
            groupSelector = mixin.createGroupFilterButtonSelectorAndBind(activityName,domainClassId, parent, (HasGroupDqlStatementProperty) pm);
        else
            groupSelector = null;
        if (pm instanceof HasColumnsDqlStatementProperty)
            columnsSelector = mixin.createColumnsFilterButtonSelectorAndBind(activityName,domainClassId, parent, (HasColumnsDqlStatementProperty) pm);
        else
            columnsSelector = null;
        if (pm instanceof HasSearchTextProperty) {
            searchBox = mixin.newTextField("GenericSearch"); // Will set the prompt
            ((HasSearchTextProperty) pm).searchTextProperty().bind(searchBox.textProperty());
        } else
            searchBox = null;
    }

    public HBox buildUi() {
        HBox bar = new HBox(10);
        ObservableList<Node> children = bar.getChildren();
        if (conditionChain != null)
            children.add(conditionChain);
        if (groupSelector != null)
            children.add(groupSelector.getButton());
        if (columnsSelector != null)
            children.add(columnsSelector.getButton());
        if (searchBox != null)
            children.add(setMaxHeightToInfinite(setHGrowable(searchBox)));
        return bar;
    }

    public void onResume() {
        if (searchBox != null)
            SceneUtil.autoFocusIfEnabled(searchBox);
    }

}
