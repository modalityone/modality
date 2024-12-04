package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public interface EntityHasDocument extends Entity, HasDocument {

    String document = "document";

    @Override
    default void setDocument(Object value) {
        setForeignField(document, value);
    }

    @Override
    default EntityId getDocumentId() {
        return getForeignEntityId(document);
    }

    @Override
    default Document getDocument() {
        return getForeignEntity(document);
    }

}