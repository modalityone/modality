package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Label;

/**
 * @author Bruno Salmon
 */
public interface EntityHasLabel extends Entity, HasLabel {

    String label = "label";

    @Override
    default void setLabel(Object value) {
        setForeignField(label, value);
    }

    @Override
    default EntityId getLabelId() {
        return getForeignEntityId(label);
    }

    @Override
    default Label getLabel() {
        return getForeignEntity(label);
    }

}