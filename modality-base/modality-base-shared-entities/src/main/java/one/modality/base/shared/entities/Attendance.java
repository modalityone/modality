package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasDate {

  default ScheduledResource getScheduledResource() {
    return getForeignEntity("scheduledResource");
  }
}
