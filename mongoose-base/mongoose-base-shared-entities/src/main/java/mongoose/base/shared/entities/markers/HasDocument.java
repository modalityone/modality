package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Document;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocument {

    void setDocument(Object document);

    EntityId getDocumentId();

    Document getDocument();

}
