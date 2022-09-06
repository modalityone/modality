package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.DocumentLine;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocumentLine {

    void setDocumentLine(Object documentLine);

    EntityId getDocumentLineId();

    DocumentLine getDocumentLine();

}
