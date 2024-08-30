package one.modality.base.server.mail;

import dev.webfx.stack.mail.MailMessage;
import dev.webfx.stack.mail.MailMessageDelegate;
import one.modality.base.shared.context.ModalityContext;

/**
 * @author Bruno Salmon
 */
public class ModalityMailMessage extends MailMessageDelegate {

    private final ModalityContext modalityContext;

    public ModalityMailMessage(MailMessage delegate, ModalityContext modalityContext) {
        super(delegate);
        this.modalityContext = modalityContext;
    }

    public ModalityContext getModalityContext() {
        return modalityContext;
    }
}
