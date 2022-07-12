package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocument {

    void setDocument(Object document);

    EntityId getDocumentId();

    Document getDocument();

}
