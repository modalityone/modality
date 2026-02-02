package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.client.i18n.BaseI18nKeys;

import java.util.Collection;

public final class CopySelectionRequest extends CopyRequest implements HasI18nKey {

    private final static String OPERATION_CODE = "CopySelection";

    public CopySelectionRequest(Collection<? extends Entity> entities, EntityColumn... columns) {
        super(entities, columns);
    }

    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return BaseI18nKeys.CopySelection;
    }

}
