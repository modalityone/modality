package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.client.i18n.ModalityI18nKeys;

import java.util.Collection;

public final class CopyAllRequest extends CopyRequest implements HasI18nKey {

    private final static String OPERATION_CODE = "CopyAll";

    public CopyAllRequest(Collection<? extends Entity> entities, EntityColumn... columns) {
        super(entities, columns);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return ModalityI18nKeys.CopyAll;
    }

}
