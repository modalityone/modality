package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Document;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasDocument extends Entity, HasDocument {

    @Override
    default void setDocument(Object document) {
        setForeignField("document", document);
    }

    @Override
    default EntityId getDocumentId() {
        return getForeignEntityId("document");
    }

    @Override
    default Document getDocument() {
        return getForeignEntity("document");
    }

}
