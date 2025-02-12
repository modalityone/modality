package one.modality.crm.server.authn.gateway.shared;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.stack.authn.AlternativeLoginActionCredentials;
import dev.webfx.stack.mail.MailMessage;
import dev.webfx.stack.mail.MailService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.server.mail.ModalityMailMessage;
import one.modality.base.shared.context.ModalityContext;
import one.modality.base.shared.entities.MagicLink;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityAuthenticationI18nKeys;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public final class LoginLinkService {

    private static final boolean SKIP_LINK_VALIDITY_CHECK = false; // Can be set to true when debugging the magic link client
    private static final Duration LINK_EXPIRATION_DURATION = Duration.ofMinutes(10);

    // Designed to be used only from server front calls (not postponed by an async operation) in order to get the loginRunId
    public static Future<Void> storeAndSendLoginLink(
        AlternativeLoginActionCredentials request,
        String activityPath,
        String from,
        String subject,
        String body,
        DataSourceModel dataSourceModel) {
        return storeAndSendLoginLink(
            null,
            request,
            null,
            activityPath,
            from,
            subject,
            body,
            dataSourceModel
        );
    }

    public static Future<Void> storeAndSendLoginLink(
        String loginRunId,
        AlternativeLoginActionCredentials request,
        String oldEmail,
        String activityPath,
        String from,
        String subject,
        String body,
        DataSourceModel dataSourceModel) {
        return storeAndSendLoginLink(
            loginRunId,
            Strings.toSafeString(request.getLanguage()), // lang
            request.getClientOrigin(), // client origin
            request.getRequestedPath(),
            request.getEmail(),
            oldEmail,
            request.getContext(),
            activityPath,
            from,
            subject,
            body,
            dataSourceModel
        );
    }

    public static Future<Void> storeAndSendLoginLink(
        String loginRunId,
        String lang,
        String clientOrigin,
        String requestedPath,
        String email,
        String oldEmail,
        Object context,
        String activityPath,
        String from,
        String subject,
        String body,
        DataSourceModel dataSourceModel) {
        if (loginRunId == null)
            loginRunId = ThreadLocalStateHolder.getRunId(); // runId = this runId (runId of the session where the request originates)
        if (!clientOrigin.startsWith("http")) {
            clientOrigin = (clientOrigin.contains(":80") ? "http" : "https") + clientOrigin.substring(clientOrigin.indexOf("://"));
        }
        String token = Uuid.randomUuid();
        String link = clientOrigin + ActivityHashUtil.withHashPrefix(activityPath.replace(":token", token).replace(":lang", lang));
        requestedPath = ActivityHashUtil.withoutHashPrefix(requestedPath);
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        MagicLink magicLink = updateStore.insertEntity(MagicLink.class);
        magicLink.setLoginRunId(loginRunId);
        magicLink.setToken(token);
        magicLink.setLang(lang);
        magicLink.setLink(link);
        magicLink.setEmail(email);
        magicLink.setOldEmail(oldEmail);
        magicLink.setRequestedPath(requestedPath);
        return updateStore.submitChanges()
            .compose(ignoredBatch -> {
                ModalityContext modalityContext = context instanceof ModalityContext ? (ModalityContext) context
                    : new ModalityContext(1 /* default organizationId if no context is provided */, null, null, null);
                modalityContext.setMagicLinkId(magicLink.getPrimaryKey());
                String finalBody = body.replaceAll("\\[loginLink\\]", magicLink.getLink());
                return MailService.sendMail(new ModalityMailMessage(MailMessage.create(from, magicLink.getEmail(), subject, finalBody), modalityContext));
            });
    }

    public static Future<MagicLink> loadLoginLinkFromToken(String token, boolean checkValidity, DataSourceModel dataSourceModel) {
        // 1) Checking the existence of the magic link in the database, and if so, loading it with required info
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select loginRunId,email,creationDate,usageDate,requestedPath,oldEmail from MagicLink where token=? limit 1", token)
            .compose(magicLinks -> {
                MagicLink magicLink = Collections.first(magicLinks);
                if (magicLink == null)
                    return Future.failedFuture("[%s] Login link not found (token: %s)".formatted(ModalityAuthenticationI18nKeys.LoginLinkUnrecognisedError, token));
                // 2) Checking the magic link is still valid (not already used and not expired)
                if (checkValidity && !SKIP_LINK_VALIDITY_CHECK) {
                    if (magicLink.getUsageDate() != null)
                        return Future.failedFuture("[%s] Login link already used (token: %s)".formatted(ModalityAuthenticationI18nKeys.LoginLinkAlreadyUsedError, token));
                    LocalDateTime now = now();
                    if (magicLink.getCreationDate() == null || now.isAfter(magicLink.getCreationDate().plus(LINK_EXPIRATION_DURATION))) {
                        return Future.failedFuture("[%s] Login link expired (token: %s)".formatted(ModalityAuthenticationI18nKeys.LoginLinkExpiredError, token));
                    }
                }
                return Future.succeededFuture(magicLink);
            });
    }

    public static Future<Void> markLoginLinkAsUsed(MagicLink magicLink, String usageRunId) {
        // We record the usage date in the database. This will indicate that the magic link has been used, and can't be
        // reused a second time.
        UpdateStore updateStore = UpdateStore.createAbove(magicLink.getStore());
        MagicLink ml = updateStore.updateEntity(magicLink);
        ml.setUsageDate(now());
        ml.setUsageRunId(usageRunId);
        return updateStore.submitChanges().map(x -> null);
    }

    public static Future<MagicLink> loadLoginLinkFromTokenAndMarkAsUsed(String token, DataSourceModel dataSourceModel) {
        String usageRunId = ThreadLocalStateHolder.getRunId();
        return loadLoginLinkFromToken(token, true, dataSourceModel)
            .compose( magicLink -> markLoginLinkAsUsed(magicLink, usageRunId)
                .map(ignored -> magicLink)
            );
    }

    public static Future<Person> loadUserPersonFromLoginLink(MagicLink magicLink) {
        String email = Objects.coalesce(magicLink.getOldEmail(), magicLink.getEmail());
        return magicLink.getStore()
            // In most cases, only the frontendAccount id is needed, but when resetting the password from the magic link,
            // the old password (encrypted) is also needed.
            .<Person>executeQuery("select frontendAccount.password from Person p where frontendAccount.username=? order by p.id limit 1", email)
            .map(Collections::first); // the owner of the account is the first person recorded in that account.
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(Clock.systemUTC());
    }

}
