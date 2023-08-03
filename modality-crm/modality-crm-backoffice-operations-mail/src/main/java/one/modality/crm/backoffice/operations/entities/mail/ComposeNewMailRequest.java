package one.modality.crm.backoffice.operations.entities.mail;

import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;

public final class ComposeNewMailRequest implements HasOperationCode,
        HasOperationExecutor<ComposeNewMailRequest, Void> {

    private final static String OPERATION_CODE = "ComposeNewMail";

    private final Document document;
    private final Pane parentContainer;

    public ComposeNewMailRequest(Document document, Pane parentContainer) {
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
    public AsyncFunction<ComposeNewMailRequest, Void> getOperationExecutor() {
        return ComposeNewMailExecutor::executeRequest;
    }
}
