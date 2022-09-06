package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Document;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocument {

    void setDocument(Object document);

    EntityId getDocumentId();

    Document getDocument();

}
