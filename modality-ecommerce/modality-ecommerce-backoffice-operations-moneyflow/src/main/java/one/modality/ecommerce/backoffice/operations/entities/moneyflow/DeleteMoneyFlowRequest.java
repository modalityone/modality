package one.modality.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.MoneyFlow;

public final class DeleteMoneyFlowRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<DeleteMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "DeleteMoneyFlow";

    private final MoneyFlow moneyFlow;
    private final Pane parentContainer;

    public DeleteMoneyFlowRequest(MoneyFlow moneyFlow, Pane parentContainer) {
        this.moneyFlow = moneyFlow;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(BaseI18nKeys.Delete);
    }

    public MoneyFlow getMoneyFlow() {
        return moneyFlow;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteMoneyFlowRequest, Void> getOperationExecutor() {
        return DeleteMoneyFlowExecutor::executeRequest;
    }
}
