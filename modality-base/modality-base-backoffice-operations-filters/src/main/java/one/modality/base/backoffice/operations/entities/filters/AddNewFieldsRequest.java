package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;

public final class AddNewFieldsRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<AddNewFieldsRequest, Void> {

    private final static String OPERATION_CODE = "AddNewFields";

    private final EntityStore entityStore;
    private final Pane parentContainer;

    public AddNewFieldsRequest(EntityStore entityStore, Pane parentContainer) {
        this.entityStore = entityStore;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(BaseI18nKeys.Add);
    }


    public EntityStore getEntityStore() {
        return entityStore;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewFieldsRequest, Void> getOperationExecutor() {
        return AddNewFieldsExecutor::executeRequest;
    }
}
