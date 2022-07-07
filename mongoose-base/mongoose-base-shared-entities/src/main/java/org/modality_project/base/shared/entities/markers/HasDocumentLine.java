package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.DocumentLine;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocumentLine {

    void setDocumentLine(Object documentLine);

    EntityId getDocumentLineId();

    DocumentLine getDocumentLine();

}
