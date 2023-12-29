package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;
import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasLocalDate {

    default ScheduledResource getScheduledResource() {
        return getForeignEntity("scheduledResource");
    }

}
