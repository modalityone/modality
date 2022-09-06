package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Label;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasLabel {

    void setLabel(Object item);

    EntityId getLabelId();

    Label getLabel();

}
