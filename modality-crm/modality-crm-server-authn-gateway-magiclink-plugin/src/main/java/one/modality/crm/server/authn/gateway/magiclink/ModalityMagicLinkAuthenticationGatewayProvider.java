package one.modality.crm.server.authn.gateway.magiclink;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGatewayProvider;
import dev.webfx.stack.mail.MailMessage;
import dev.webfx.stack.mail.MailService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.StateAccessor;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.server.mail.ModalityMailMessage;
import one.modality.base.shared.context.ModalityContext;
import one.modality.base.shared.entities.MagicLink;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityGuestPrincipal;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class ModalityMagicLinkAuthenticationGatewayProvider implements ServerAuthenticationGatewayProvider, HasDataSourceModel {

    private static final boolean SKIP_LINK_VALIDITY_CHECK = false; // Can be set to true when debugging the magic link client
    private static final Duration LINK_EXPIRATION_DURATION = Duration.ofMinutes(10);

    private static final String MAGIC_LINK_ACTIVITY_PATH_PREFIX = "/magic-link";
    private static final String MAGIC_LINK_ACTIVITY_PATH_FULL = MAGIC_LINK_ACTIVITY_PATH_PREFIX + "/:token";
    private static final String HASH_PATH = "/#";

    private final DataSourceModel dataSourceModel;

    public ModalityMagicLinkAuthenticationGatewayProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityMagicLinkAuthenticationGatewayProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    @Override
    public boolean acceptsUserCredentials(Object userCredentials) {
        return userCredentials instanceof SendMagicLinkCredentials
               || userCredentials instanceof MagicLinkCredentials
               || userCredentials instanceof RenewMagicLinkCredentials
            ;
    }

    @Override
    public Future<?> authenticate(Object userCredentials) {
        if (userCredentials instanceof SendMagicLinkCredentials)
            return createAndSendMagicLink((SendMagicLinkCredentials) userCredentials);
        if (userCredentials instanceof RenewMagicLinkCredentials)
            return renewAndSendMagicLink((RenewMagicLinkCredentials) userCredentials);
        if (userCredentials instanceof MagicLinkCredentials)
            return authenticateWithMagicLink((MagicLinkCredentials) userCredentials);
        return Future.failedFuture(getClass().getSimpleName() + ".authenticate() requires a " + SendMagicLinkCredentials.class.getSimpleName() + ", " + RenewMagicLinkCredentials.class.getSimpleName() + " or " + MagicLinkCredentials.class.getSimpleName() + " argument");
    }

    private Future<Void> createAndSendMagicLink(SendMagicLinkCredentials request) {
        return storeAndSendMagicLink(
            ThreadLocalStateHolder.getRunId(), // runId = this runId (runId of the session where the request originates)
            Strings.toSafeString(request.getLanguage()), // lang
            request.getClientOrigin(), // client origin
            request.getRequestedPath(),
            request.getEmail(),
            request.getContext()
        );
    }

    private Future<Void> renewAndSendMagicLink(RenewMagicLinkCredentials request) {
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select loginRunId, lang, link, email, requestedPath from MagicLink where token=? order by id desc limit 1", request.getPreviousToken())
            .map(Collections::first)
            .compose(magicLink -> {
                if (magicLink == null)
                    return Future.failedFuture("Magic link token not found");
                String link = magicLink.getLink();
                String clientOrigin = withoutHashSuffix(link.substring(0, link.indexOf(MAGIC_LINK_ACTIVITY_PATH_PREFIX)));
                return storeAndSendMagicLink(
                    magicLink.getLoginRunId(),
                    magicLink.getLang(),
                    clientOrigin,
                    magicLink.getRequestedPath(),
                    magicLink.getEmail(),
                    null
                );
            });
    }

    private Future<Void> storeAndSendMagicLink(String loginRunId, String lang, String clientOrigin, String requestedPath, String email, Object context) {
        if (!clientOrigin.startsWith("http")) {
            clientOrigin = (clientOrigin.contains(":80") ? "http" : "https") + clientOrigin.substring(clientOrigin.indexOf("://"));
        }
        String token = Uuid.randomUuid();
        String link = clientOrigin + withHashPrefix(MAGIC_LINK_ACTIVITY_PATH_FULL.replace(":token", token).replace(":lang", lang));
        requestedPath = withoutHashPrefix(requestedPath);
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        MagicLink magicLink = updateStore.insertEntity(MagicLink.class);
        magicLink.setLoginRunId(loginRunId);
        magicLink.setToken(token);
        magicLink.setLang(lang);
        magicLink.setLink(link);
        magicLink.setEmail(email);
        magicLink.setRequestedPath(requestedPath);
        return updateStore.submitChanges()
            .compose(ignoredBatch -> {
                ModalityContext modalityContext = context instanceof ModalityContext ? (ModalityContext) context
                    : new ModalityContext(1 /* default organizationId if no context is provided */, null, null, null);
                modalityContext.setMagicLinkId(magicLink.getPrimaryKey());
                return MailService.sendMail(new ModalityMailMessage(MailMessage.create(null, magicLink.getEmail(), "Magic link", magicLink.getLink()), modalityContext));
            });
    }

    private Future<String> authenticateWithMagicLink(MagicLinkCredentials credentials) {
        String usageRunId = ThreadLocalStateHolder.getRunId();
        // 1) Checking the existence of the magic link in the database, and if so, loading it with required info
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select loginRunId,email,creationDate,usageDate,requestedPath from MagicLink where token=? limit 1", credentials.getToken())
            .compose(magicLinks -> {
                MagicLink magicLink = Collections.first(magicLinks);
                if (magicLink == null)
                    return Future.failedFuture("Magic link not found (token: " + credentials.getToken() + ")");
                // 2) Checking the magic link is still valid (not already used and not expired)
                LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                if (!SKIP_LINK_VALIDITY_CHECK) {
                    if (magicLink.getUsageDate() != null)
                        return Future.failedFuture("Magic link already used (token: " + credentials.getToken() + ")");
                    if (magicLink.getCreationDate() == null || now.isAfter(magicLink.getCreationDate().plus(LINK_EXPIRATION_DURATION))) {
                        return Future.failedFuture("Magic link expired (token: " + credentials.getToken() + ")");
                    }
                }
                // 3) The magic link is valid, so we memorise its usage date, and also check if the request comes from
                // a registered or unregistered user (with or without an account)
                return magicLink.getStore()
                    .<Person>executeQuery("select frontendAccount from Person p where frontendAccount.username=? order by p.id limit 1", magicLink.getEmail())
                    .compose(persons -> {
                        // 4) Preparing the userId = ModalityUserPrincipal for registered users, ModalityGuestPrincipal for unregistered users
                        Object userId;
                        if (!persons.isEmpty()) {
                            Person userPerson = persons.get(0);
                            userId = new ModalityUserPrincipal(userPerson.getPrimaryKey(), userPerson.getForeignEntity("frontendAccount").getPrimaryKey());
                        } else {
                            userId = new ModalityGuestPrincipal(magicLink.getEmail());
                        }
                        // 5) Pushing the userId to the magic link client which is identified by runId = usageRunId.
                        // Pushing the userId will cause a login, and subsequently a push of the authorizations.

                        return PushServerService.pushState(StateAccessor.createUserIdState(userId), usageRunId)
                            .compose(ignored -> { // indicates that the magic link client acknowledged this login push
                                // 6) Now that we managed to reach the magic link client and have a successful login,
                                // we record the usage date in the database. This will indicate that the magic link has
                                // been used, and can't be reused a second time.
                                UpdateStore updateStore = UpdateStore.createAbove(magicLink.getStore());
                                MagicLink ml = updateStore.updateEntity(magicLink);
                                ml.setUsageDate(now);
                                ml.setUsageRunId(usageRunId);
                                return updateStore.submitChanges()
                                    .map(ignored2 -> magicLink.getRequestedPath())
                                    .onFailure(Console::log)
                                    .onSuccess(ignored2 -> {
                                        // 7) We also finally push the userId to the original login client here, and
                                        // this should be followed by a subsequent push of the authorizations to that
                                        // same client (as explained above).
                                        String loginRunId = magicLink.getLoginRunId();
                                        PushServerService.pushState(StateAccessor.createUserIdState(userId), loginRunId);
                                    })
                                    ;
                            });
                    });
            });
    }

    @Override
    public boolean acceptsUserId() {
        return false;
    }

    @Override
    public Future<?> verifyAuthenticated() {
        return Future.failedFuture(getClass().getSimpleName() + ".verifyAuthenticated() is not supported");
    }

    @Override
    public Future<UserClaims> getUserClaims() {
        return Future.failedFuture(getClass().getSimpleName() + ".getUserClaims() is not supported");
    }

    @Override
    public boolean acceptsUpdateCredentialsArgument(Object updateCredentialsArgument) {
        return updateCredentialsArgument instanceof UpdatePasswordMagicLinkCredentials;
    }

    @Override
    public Future<?> updateCredentials(Object updateCredentialsArgument) {
        if (!(updateCredentialsArgument instanceof UpdatePasswordMagicLinkCredentials)) {
            return Future.failedFuture(getClass().getSimpleName() + ".updateCredentials() requires a " + UpdatePasswordMagicLinkCredentials.class.getSimpleName() + " argument");
        }
        UpdatePasswordMagicLinkCredentials update = (UpdatePasswordMagicLinkCredentials) updateCredentialsArgument;
        String usageRunId = ThreadLocalStateHolder.getRunId();
        // 1) Loading the email for the magic link normally associated with this magic link app userId from the database
        // This will be used to identify the account we need to change the password for.
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select email from MagicLink where usageRunId=? limit 1", usageRunId)
            .compose(magicLinks -> {
                if (magicLinks.isEmpty())
                    return Future.failedFuture("Magic link not found!");
                MagicLink magicLink = magicLinks.get(0);
                // 3) Reading the
                return magicLink.getStore()
                    .<Person>executeQuery("select frontendAccount.password from Person p where frontendAccount.username=? order by p.id limit 1", magicLink.getEmail())
                    .compose(persons -> {
                        if (persons.isEmpty())
                            return Future.failedFuture("This is not a registered user");
                        // 4) Preparing the userId = ModalityUserPrincipal for registered users, ModalityGuestPrincipal for unregistered users
                        Person userPerson = persons.get(0);
                        ModalityUserPrincipal targetUserId = new ModalityUserPrincipal(userPerson.getPrimaryKey(), userPerson.getForeignEntity("frontendAccount").getPrimaryKey());
                        // 5) Pushing the userId to the original client from which the magic link request was made.
                        // The original client is identified by runId. Pushing the userId will cause a login, and
                        // subsequently a push of the authorizations.
                        UpdatePasswordCredentials updatePasswordCredentials = new UpdatePasswordCredentials(
                            userPerson.evaluate("frontendAccount.password"), // old password
                            update.getNewPassword() // new password
                        );
                        Promise<Void> promise = Promise.promise();
                        ThreadLocalStateHolder.runAsUser(targetUserId,
                            () -> promise.handle(AuthenticationService.updateCredentials(updatePasswordCredentials).mapEmpty())
                        );
                        return promise.future();
                    });
            });
    }

    @Override
    public Future<Void> logout() {
        return LogoutPush.pushLogoutMessageToClient();
    }

    private static String withHashPrefix(String path) {
        return path.startsWith(HASH_PATH) ? path : HASH_PATH + path;
    }

    private static String withoutHashPrefix(String path) {
        String p = Strings.removePrefix(path, HASH_PATH);
        return Objects.equals(p, path) ? p : withoutHashPrefix(p);
    }

    private static String withoutHashSuffix(String path) {
        String p = Strings.removeSuffix(path, HASH_PATH);
        return Objects.equals(p, path) ? p : withoutHashSuffix(p);
    }

}
