package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.DocumentLine;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasDocumentLine {

    void setDocumentLine(Object documentLine);

    EntityId getDocumentLineId();

    DocumentLine getDocumentLine();

}
