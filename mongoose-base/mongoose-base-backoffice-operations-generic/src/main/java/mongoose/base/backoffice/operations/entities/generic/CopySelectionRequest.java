package mongoose.base.backoffice.operations.entities.generic;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_grid.EntityColumn;
import dev.webfx.framework.shared.orm.entity.Entity;

import java.util.Collection;

public final class CopySelectionRequest extends CopyRequest {

    private final static String OPERATION_CODE = "CopySelection";

    public CopySelectionRequest(Collection<? extends Entity> entities, EntityColumn... columns) {
        super(entities, columns);
    }

    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
