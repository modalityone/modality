package one.modality.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;

public final class AddNewMoneyFlowRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<AddNewMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyFlow";

    private final Pane parentContainer;

    public AddNewMoneyFlowRequest(Pane parentContainer) {
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


    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewMoneyFlowRequest, Void> getOperationExecutor() {
        return AddNewMoneyFlowExecutor::executeRequest;
    }
}
