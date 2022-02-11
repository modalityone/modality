package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.DocumentLine;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasDocumentLine extends Entity, HasDocumentLine {

    @Override
    default void setDocumentLine(Object documentLine) {
        setForeignField("documentLine", documentLine);
    }

    @Override
    default EntityId getDocumentLineId() {
        return getForeignEntityId("documentLine");
    }

    @Override
    default DocumentLine getDocumentLine() {
        return getForeignEntity("documentLine");
    }

}
