package one.modality.crm.backoffice.operations.entities.mail;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Mail;

public final class OpenMailRequest
        implements HasOperationCode, HasOperationExecutor<OpenMailRequest, Void> {

    private static final String OPERATION_CODE = "OpenMail";

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
