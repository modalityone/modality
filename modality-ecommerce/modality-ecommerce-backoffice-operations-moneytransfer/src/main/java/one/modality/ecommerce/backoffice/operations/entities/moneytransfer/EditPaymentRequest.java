package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.MoneyTransfer;

public final class EditPaymentRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<EditPaymentRequest, Void> {

    private final static String OPERATION_CODE = "EditPayment";

    private final MoneyTransfer payment;
    private final Pane parentContainer;

    public EditPaymentRequest(MoneyTransfer payment) {
        this(payment, FXMainFrameDialogArea.getDialogArea());
    }

    public EditPaymentRequest(MoneyTransfer payment, Pane parentContainer) {
        this.payment = payment;
        this.parentContainer = parentContainer;
    }

    MoneyTransfer getPayment() {
        return payment;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(BaseI18nKeys.Edit);
    }

    @Override
    public AsyncFunction<EditPaymentRequest, Void> getOperationExecutor() {
        return EditPaymentExecutor::executeRequest;
    }
}
