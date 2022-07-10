package org.modality_project.crm.backoffice.operations.entities.mail;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.shared.operation.HasOperationExecutor;
import dev.webfx.stack.platform.async.AsyncFunction;

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
