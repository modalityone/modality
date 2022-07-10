package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasDate;
import org.modality_project.base.shared.entities.markers.EntityHasDocumentLine;
import dev.webfx.stack.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasDate {

}
