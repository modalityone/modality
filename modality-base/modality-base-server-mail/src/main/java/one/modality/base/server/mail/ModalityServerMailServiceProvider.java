package one.modality.base.server.mail;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.mail.MailMessage;
import dev.webfx.stack.mail.spi.MailServiceProvider;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.context.ModalityContext;
import one.modality.base.shared.entities.Mail;

/**
 * @author Bruno Salmon
 */
public class ModalityServerMailServiceProvider implements MailServiceProvider {

    private final DataSourceModel dataSourceModel;

    public ModalityServerMailServiceProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityServerMailServiceProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public Future<Void> sendMail(MailMessage mailMessage) {
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        Mail mail = updateStore.insertEntity(Mail.class);
        mail.setFromEmail(mailMessage.getFrom());
        mail.setSubject(mailMessage.getSubject());
        mail.setContent(mailMessage.getBody());
        mail.setOut(true);
        if (mailMessage instanceof ModalityMailMessage) {
            ModalityContext modalityContext = ((ModalityMailMessage) mailMessage).getModalityContext();
            if (modalityContext != null) {
                mail.setDocument(modalityContext.getDocumentId());
                mail.setOrganization(modalityContext.getOrganizationId());
                mail.setMagicLink(modalityContext.getMagicLinkId());
            }
        }
        // Temporarily hardcoding the account to be used for sending mails, because otherwise, if the organization is
        // set to a centre where no mail account is configured, the database will raise this constraint error:
        // null value in column "account_id" violates not-null constraint (23502)
        mail.setAccount(27); // Hardcoded value for kbs@kadampa.net account, because so far only LoginLinkService is sending mails in KBS3
        return updateStore.submitChanges().map(ignored -> null);
    }
}
