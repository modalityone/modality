package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.shared.entities.MoneyAccount;

public final class EditMoneyAccountRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<EditMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "EditMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final Pane parentContainer;

    public EditMoneyAccountRequest(MoneyAccount moneyAccount, Pane parentContainer) {
        this.moneyAccount = moneyAccount;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(ModalityI18nKeys.Edit);
    }

    public MoneyAccount getMoneyAccount() { return moneyAccount; }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditMoneyAccountRequest, Void> getOperationExecutor() {
        return EditMoneyAccountExecutor::executeRequest;
    }
}
