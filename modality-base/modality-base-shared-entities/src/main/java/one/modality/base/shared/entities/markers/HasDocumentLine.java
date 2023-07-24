package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public interface HasDocumentLine {

  void setDocumentLine(Object documentLine);

  EntityId getDocumentLineId();

  DocumentLine getDocumentLine();
}
