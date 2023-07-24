package one.modality.base.backoffice.controls.masterslave;

import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;

import one.modality.base.client.entities.util.filters.FilterButtonSelectorFactoryMixin;

public interface ConventionalUiBuilderMixin extends FilterButtonSelectorFactoryMixin {

    default <
                    PM extends
                            HasGroupVisualResultProperty & HasMasterVisualResultProperty
                                    & HasSelectedMasterProperty>
            ConventionalUiBuilder createAndBindGroupMasterSlaveViewWithFilterSearchBar(
                    PM pm, String activityName, String domainClassId) {
        return ConventionalUiBuilder.createAndBindGroupMasterSlaveViewWithFilterSearchBar(
                pm, this, activityName, domainClassId);
    }
}
