package org.modality_project.ecommerce.backoffice.operations.entities.moneytransfer;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.shared.operation.HasOperationExecutor;
import dev.webfx.stack.platform.async.AsyncFunction;

public final class AddNewTransferRequest implements HasOperationCode,
        HasOperationExecutor<AddNewTransferRequest, Void> {

    private final static String OPERATION_CODE = "AddNewTransfer";

    private final Document document;
    private final Pane parentContainer;

    public AddNewTransferRequest(Document document, Pane parentContainer) {
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
    public AsyncFunction<AddNewTransferRequest, Void> getOperationExecutor() {
        return AddNewTransferExecutor::executeRequest;
    }
}
