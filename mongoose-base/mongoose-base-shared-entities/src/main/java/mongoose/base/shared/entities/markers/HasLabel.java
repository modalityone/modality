package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Label;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasLabel {

    void setLabel(Object item);

    EntityId getLabelId();

    Label getLabel();

}
