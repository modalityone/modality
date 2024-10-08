package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class SendLetterRequest implements HasOperationCode,
        HasOperationExecutor<SendLetterRequest, Void> {

    private final static String OPERATION_CODE = "SendLetter";

    private final Document document;
    private final Pane parentContainer;

    public SendLetterRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public SendLetterRequest(Document document, Pane parentContainer) {
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
    public AsyncFunction<SendLetterRequest, Void> getOperationExecutor() {
        return SendLetterExecutor::executeRequest;
    }
}
