package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.MoneyFlow;

import java.util.Collections;
import java.util.List;

public final class DeleteMoneyAccountRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<DeleteMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "DeleteMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final List<MoneyFlow> moneyFlows;
    private final Pane parentContainer;

    public DeleteMoneyAccountRequest(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows, Pane parentContainer) {
        this.moneyAccount = moneyAccount;
        this.moneyFlows = moneyFlows;
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

    public MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    public List<MoneyFlow> getMoneyFlows() { return Collections.unmodifiableList(moneyFlows); }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<DeleteMoneyAccountRequest, Void> getOperationExecutor() {
        return DeleteMoneyAccountExecutor::executeRequest;
    }
}
