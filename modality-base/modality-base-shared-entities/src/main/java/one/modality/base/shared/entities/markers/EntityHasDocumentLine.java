package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public interface EntityHasDocumentLine extends Entity, HasDocumentLine {

    String documentLine = "documentLine";

    @Override
    default void setDocumentLine(Object value) {
        setForeignField(documentLine, value);
    }

    @Override
    default EntityId getDocumentLineId() {
        return getForeignEntityId(documentLine);
    }

    @Override
    default DocumentLine getDocumentLine() {
        return getForeignEntity(documentLine);
    }
}