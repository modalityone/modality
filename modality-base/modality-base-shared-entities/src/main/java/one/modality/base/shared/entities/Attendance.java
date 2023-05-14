package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;
import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasDate {

    default ResourceConfiguration getResourceConfiguration() {
        return getForeignEntity("scheduledResource").getForeignEntity("configuration");
    }
}
