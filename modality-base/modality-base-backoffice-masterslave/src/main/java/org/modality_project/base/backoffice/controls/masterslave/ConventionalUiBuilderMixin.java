package org.modality_project.base.backoffice.controls.masterslave;

import org.modality_project.base.client.entities.util.filters.FilterButtonSelectorFactoryMixin;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;

public interface ConventionalUiBuilderMixin extends FilterButtonSelectorFactoryMixin {

    default <PM extends HasGroupVisualResultProperty & HasMasterVisualResultProperty & HasSelectedMasterProperty>
    ConventionalUiBuilder createAndBindGroupMasterSlaveViewWithFilterSearchBar(PM pm, String activityName, String domainClassId) {
        return ConventionalUiBuilder.createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, this, activityName, domainClassId);
    }

}
