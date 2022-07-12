package org.modality_project.base.backoffice.operations.entities.generic;

import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.entity.Entity;

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
