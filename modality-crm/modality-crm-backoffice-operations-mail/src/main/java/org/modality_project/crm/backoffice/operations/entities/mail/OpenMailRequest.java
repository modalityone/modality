package org.modality_project.crm.backoffice.operations.entities.mail;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Mail;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;

public final class OpenMailRequest implements HasOperationCode,
        HasOperationExecutor<OpenMailRequest, Void> {

    private final static String OPERATION_CODE = "OpenMail";

    private final Mail mail;
    private final Pane parentContainer;

    public OpenMailRequest(Mail mail, Pane parentContainer) {
        this.mail = mail;
        this.parentContainer = parentContainer;
    }

    Mail getMail() {
        return mail;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<OpenMailRequest, Void> getOperationExecutor() {
        return OpenMailExecutor::executeRequest;
    }
}
