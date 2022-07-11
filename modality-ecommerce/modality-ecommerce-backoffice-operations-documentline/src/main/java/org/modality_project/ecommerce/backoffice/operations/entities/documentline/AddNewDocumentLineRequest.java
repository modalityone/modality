package org.modality_project.ecommerce.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.shared.operation.HasOperationExecutor;
import dev.webfx.stack.async.AsyncFunction;

public final class AddNewDocumentLineRequest implements HasOperationCode,
        HasOperationExecutor<AddNewDocumentLineRequest, Void> {

    private final static String OPERATION_CODE = "AddNewDocumentLine";

    private final Document document;
    private final Pane parentContainer;

    public AddNewDocumentLineRequest(Document document, Pane parentContainer) {
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
    public AsyncFunction<AddNewDocumentLineRequest, Void> getOperationExecutor() {
        return AddNewDocumentLineExecutor::executeRequest;
    }
}
