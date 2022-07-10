package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Label;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasLabel {

    void setLabel(Object item);

    EntityId getLabelId();

    Label getLabel();

}
