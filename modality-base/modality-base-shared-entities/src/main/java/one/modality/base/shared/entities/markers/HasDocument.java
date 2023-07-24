package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public interface HasDocument {

  void setDocument(Object document);

  EntityId getDocumentId();

  Document getDocument();
}
