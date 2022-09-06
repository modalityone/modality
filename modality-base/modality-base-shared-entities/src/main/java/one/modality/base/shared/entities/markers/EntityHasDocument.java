package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Document;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

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
