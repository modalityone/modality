package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Label;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasLabel extends Entity, HasLabel {

    @Override
    default void setLabel(Object label) {
        setForeignField("label", label);
    }

    @Override
    default EntityId getLabelId() {
        return getForeignEntityId("label");
    }

    @Override
    default Label getLabel() {
        return getForeignEntity("label");
    }

}
