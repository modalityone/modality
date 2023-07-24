package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Label;

/**
 * @author Bruno Salmon
 */
public interface HasLabel {

  void setLabel(Object item);

  EntityId getLabelId();

  Label getLabel();
}
