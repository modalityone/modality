package one.modality.crm.server.authn.gateway.magiclink;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGatewayProvider;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.StateAccessor;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.MagicLink;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityGuestPrincipal;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public class ModalityMagicLinkAuthenticationGatewayProvider implements ServerAuthenticationGatewayProvider, HasDataSourceModel {

    private static final String MAGIC_LINK_APP_ROUTE = "/magic-link/#/:lang/:token";

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
        return userCredentials instanceof MagicLinkRequest || userCredentials instanceof MagicLinkCredentials;
    }

    @Override
    public Future<?> authenticate(Object userCredentials) {
        if (userCredentials instanceof MagicLinkRequest)
            return createAndSendMagicLink((MagicLinkRequest) userCredentials);
        if (userCredentials instanceof MagicLinkCredentials)
            return authenticateWithMagicLink((MagicLinkCredentials) userCredentials);
        return Future.failedFuture(getClass().getSimpleName() + ".authenticate() requires a " + MagicLinkRequest.class.getSimpleName() + " or " + MagicLinkCredentials.class.getSimpleName() + " argument");
    }

    private Future<Void> createAndSendMagicLink(MagicLinkRequest request) {
        String runId = ThreadLocalStateHolder.getRunId();
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        MagicLink magicLink = updateStore.insertEntity(MagicLink.class);
        magicLink.setRunId(runId);
        String token = Uuid.randomUuid();
        magicLink.setToken(token);
        String link = request.getClientOrigin() + MAGIC_LINK_APP_ROUTE.replace(":token", token).replace(":lang", Strings.toSafeString(request.getLanguage()));
        if (link.startsWith(":")) // temporary workaround for requests coming from desktops & mobiles
            link = "http" + link;
        magicLink.setLink(link);
        magicLink.setEmail(request.getEmail());
        return updateStore.submitChanges()
            .map(ignoredBatch -> null);
    }

    private Future<Void> authenticateWithMagicLink(MagicLinkCredentials credentials) {
        String magicLinkAppRunId = ThreadLocalStateHolder.getRunId();
        // 1) Checking the existence of the magic link in the database, and if so, loading it with required info
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select runId,email,creationDate,usageDate from MagicLink where token=? limit 1", credentials.getToken())
            .compose(magicLinks -> {
                if (magicLinks.isEmpty())
                    return Future.failedFuture("Magic link not found (token: " + credentials.getToken() + ")");
                // 2) Checking the magic link is still valid (not already used and not expired)
                MagicLink magicLink = magicLinks.get(0);
                if (magicLink.getUsageDate() != null)
                    return Future.failedFuture("Magic link already used (token: " + credentials.getToken() + ")");
                LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                if (magicLink.getCreationDate() == null || now.isAfter(magicLink.getCreationDate().plusMinutes(10))) {
                    return Future.failedFuture("Magic link expired (token: " + credentials.getToken() + ")");
                }
                // 3) The magic link is valid, so we memorise its usage date, and also check if the request comes from
                // a registered or unregistered user (with or without an account)
                return magicLink.getStore()
                    .<Person>executeQuery("select frontendAccount from Person p where frontendAccount.username=? order by p.id limit 1", magicLink.getEmail())
                    .compose(persons -> {
                        // 4) Preparing the userId = ModalityUserPrincipal for registered users, ModalityGuestPrincipal for unregistered users
                        String targetRunId = magicLink.getRunId();
                        Object targetUserId;
                        if (!persons.isEmpty()) {
                            Person userPerson = persons.get(0);
                            targetUserId = new ModalityUserPrincipal(userPerson.getPrimaryKey(), userPerson.getForeignEntity("frontendAccount").getPrimaryKey());
                        } else {
                            targetUserId = new ModalityGuestPrincipal(magicLink.getEmail());
                        }
                        // 5) Pushing the userId to the original client from which the magic link request was made.
                        // The original client is identified by runId. Pushing the userId will cause a login, and
                        // subsequently a push of the authorizations.
                        Object targetState = StateAccessor.createUserIdState(targetUserId);

                        // We push the userId to the original login client directly here, and indirectly this should be
                        // followed by a subsequent push of the authorizations to that same client (as explained above).
                        return PushServerService.pushState(targetState, targetRunId)
                            .compose(ignored -> { // indicates that the original client acknowledged this login push
                                // 6) Now that we managed to reach the original client and have a successful login,
                                // we record the usage date in the database. This will indicate that the magic link has
                                // been used, and can't be reused a second time.
                                UpdateStore updateStore = UpdateStore.createAbove(magicLink.getStore());
                                MagicLink ml = updateStore.updateEntity(magicLink);
                                ml.setUsageDate(now);
                                ml.setAppRunId(magicLinkAppRunId);
                                return updateStore.submitChanges().map(ignored2 -> (Void) null)
                                    .onFailure(Console::log);
                            });
                    });
            });
    }

    @Override
    public boolean acceptsUserId() {
        return true;
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
        return updateCredentialsArgument instanceof MagicLinkPasswordUpdate;
    }

    @Override
    public Future<?> updateCredentials(Object updateCredentialsArgument) {
        if (!(updateCredentialsArgument instanceof MagicLinkPasswordUpdate)) {
            return Future.failedFuture(getClass().getSimpleName() + ".updateCredentials() requires a " + MagicLinkPasswordUpdate.class.getSimpleName() + " argument");
        }
        MagicLinkPasswordUpdate update = (MagicLinkPasswordUpdate) updateCredentialsArgument;
        String magicLinkAppRunId = ThreadLocalStateHolder.getRunId();
        // 1) Loading the email for the magic link normally associated with this magic link app userId from the database
        // This will be used to identify the account we need to change the password for.
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select email from MagicLink where appRunId=? limit 1", magicLinkAppRunId)
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
                        PasswordUpdate passwordUpdate = new PasswordUpdate(userPerson.evaluate("frontendAccount.password"), update.getNewPassword());
                        Promise<Void> promise = Promise.promise();
                        ThreadLocalStateHolder.runAsUser(targetUserId,
                            () -> promise.handle(AuthenticationService.updateCredentials(passwordUpdate).map(x -> (Void) null))
                        );
                        return promise.future();
                    });
            });
    }

    @Override
    public Future<Void> logout() {
        return LogoutPush.pushLogoutMessageToClient();
    }

}
