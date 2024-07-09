package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class AddNewPaymentRequest implements HasOperationCode,
        HasOperationExecutor<AddNewPaymentRequest, Void> {

    private final static String OPERATION_CODE = "AddNewPayment";

    private final Document document;
    private final Pane parentContainer;

    public AddNewPaymentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public AddNewPaymentRequest(Document document, Pane parentContainer) {
        this.document = document;
        this.parentContainer = parentContainer;
    }

    Document getDocument() {
        return document;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<AddNewPaymentRequest, Void> getOperationExecutor() {
        return AddNewPaymentExecutor::executeRequest;
    }
}
