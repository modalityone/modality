package one.modality.crm.server.authn.gateway.magiclink;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGateway;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.StateAccessor;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.MagicLink;
import one.modality.base.shared.util.ActivityHashUtil;
import one.modality.crm.server.authn.gateway.shared.MagicLinkService;
import one.modality.crm.shared.services.authn.ModalityAuthenticationI18nKeys;
import one.modality.crm.shared.services.authn.ModalityGuestPrincipal;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class ModalityMagicLinkAuthenticationGateway implements ServerAuthenticationGateway, HasDataSourceModel {

    private static final String MAGIC_LINK_ACTIVITY_PATH_PREFIX = "/magic-link";
    public static final String MAGIC_LINK_ACTIVITY_PATH_FULL = MAGIC_LINK_ACTIVITY_PATH_PREFIX + "/:token";
    // 👆 public because used by ModalityPasswordAuthenticationGateway in case a user requests an account creation
    // on an existing account. In this case, ModalityPasswordAuthenticationGateway emails him a magic link.

    // Temporarily hardcoded (to replace with database letters)
    private static final String MAIL_FROM = "kbs@kadampa.net";
    private static final String RECOVERY_MAIL_SUBJECT = "Password recovery - Kadampa Booking System";
    private static final String RECOVERY_VERIFICATION_CODE_OR_MAGIC_LINK_MAIL_BODY = Resource.getText(Resource.toUrl("RecoveryWithVerificationCodeOrMagicLinkMailBody.html", ModalityMagicLinkAuthenticationGateway.class));
    private static final String RECOVERY_VERIFICATION_CODE_ONLY_MAIL_BODY = Resource.getText(Resource.toUrl("RecoveryWithVerificationCodeOnlyMailBody.html", ModalityMagicLinkAuthenticationGateway.class));
    private static final String UNKNOWN_ACCOUNT_MAIL_SUBJECT = "Assistance with your Kadampa booking account";
    private static final String UNKNOWN_ACCOUNT_MAIL_BODY = Resource.getText(Resource.toUrl("UnknownAccountMailBody.html", ModalityMagicLinkAuthenticationGateway.class));

    private final DataSourceModel dataSourceModel;

    public ModalityMagicLinkAuthenticationGateway() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityMagicLinkAuthenticationGateway(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    @Override
    public boolean acceptsUserCredentials(Object userCredentials) {
        return userCredentials instanceof SendMagicLinkCredentials
               || userCredentials instanceof RenewMagicLinkCredentials
               || userCredentials instanceof AuthenticateWithMagicLinkCredentials
               || userCredentials instanceof AuthenticateWithVerificationCodeCredentials
            ;
    }

    @Override
    public Future<?> authenticate(Object userCredentials) {
        if (userCredentials instanceof SendMagicLinkCredentials sendMagicLinkCredentials)
            return createAndSendMagicLink(sendMagicLinkCredentials);
        if (userCredentials instanceof RenewMagicLinkCredentials renewMagicLinkCredentials)
            return renewAndSendMagicLink(renewMagicLinkCredentials);
        if (userCredentials instanceof AuthenticateWithMagicLinkCredentials authenticateWithMagicLinkCredentials)
            return authenticateWithMagicLink(authenticateWithMagicLinkCredentials);
        if (userCredentials instanceof AuthenticateWithVerificationCodeCredentials authenticateWithVerificationCodeCredentials)
            return authenticateWithVerificationCode(authenticateWithVerificationCodeCredentials);
        return Future.failedFuture("%s.authenticate() requires a %s, %s or %s argument".formatted(getClass().getSimpleName(), SendMagicLinkCredentials.class.getSimpleName(), RenewMagicLinkCredentials.class.getSimpleName(), AuthenticateWithMagicLinkCredentials.class.getSimpleName()));
    }

    private Future<Void> createAndSendMagicLink(SendMagicLinkCredentials request) {
        // We check that the requested account exists in the database. If it exists, we send a "Password recovery" email
        // as requested. But if it doesn't exist, we send an "Unknown account" email instead. For this later case, we
        // still create a login link in the database for history purpose, even though it's not technically necessary as
        // the "Unknown account" email doesn't propose any further action.
        String loginRunId = ThreadLocalStateHolder.getRunId(); // Capturing the loginRunId before async operation
        return EntityStore.create(dataSourceModel)
            .<FrontendAccount>executeQuery("select FrontendAccount where corporation=$1 and lower(username)=lower($2) limit 1", 1, request.getEmail())
            .compose(accounts -> {
                    boolean unknown = accounts.isEmpty();
                    return MagicLinkService.createAndSendMagicLink(
                        loginRunId,
                        request,
                        null,
                        MAGIC_LINK_ACTIVITY_PATH_FULL,
                        MAIL_FROM,
                        unknown ? UNKNOWN_ACCOUNT_MAIL_SUBJECT : RECOVERY_MAIL_SUBJECT,
                        unknown ? UNKNOWN_ACCOUNT_MAIL_BODY : request.isVerificationCodeOnly() ? RECOVERY_VERIFICATION_CODE_ONLY_MAIL_BODY : RECOVERY_VERIFICATION_CODE_OR_MAGIC_LINK_MAIL_BODY,
                        dataSourceModel
                    );
                }
            );
    }

    private Future<Void> renewAndSendMagicLink(RenewMagicLinkCredentials request) {
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select loginRunId, lang, link, email, requestedPath from MagicLink where token=$1 order by id desc limit 1", request.previousToken())
            .map(Collections::first)
            .compose(magicLink -> {
                if (magicLink == null)
                    return Future.failedFuture("[%s] Magic link token not found".formatted(ModalityAuthenticationI18nKeys.LoginLinkUnrecognisedError));
                String link = magicLink.getLink();
                String clientOrigin = ActivityHashUtil.withoutHashSuffix(link.substring(0, link.indexOf(MAGIC_LINK_ACTIVITY_PATH_PREFIX)));
                return MagicLinkService.createAndSendMagicLink(
                    magicLink.getLoginRunId(),
                    magicLink.getLang(),
                    clientOrigin,
                    magicLink.getRequestedPath(),
                    magicLink.getEmail(),
                    null,
                    null,
                    MAGIC_LINK_ACTIVITY_PATH_FULL,
                    MAIL_FROM,
                    RECOVERY_MAIL_SUBJECT,
                    RECOVERY_VERIFICATION_CODE_OR_MAGIC_LINK_MAIL_BODY,
                    dataSourceModel
                );
            });
    }

    private Future<String> authenticateWithMagicLink(AuthenticateWithMagicLinkCredentials credentials) {
        return authenticateWithMagicLink(credentials.token());
    }

    private Future<String> authenticateWithVerificationCode(AuthenticateWithVerificationCodeCredentials credentials) {
        return authenticateWithMagicLink(credentials.verificationCode());
    }

    private Future<String> authenticateWithMagicLink(String tokenOrVerificationCode) {
        String usageRunId = ThreadLocalStateHolder.getRunId();
        // 1) Checking the existence of the magic link in the database, and if so, loading it with required info
        return MagicLinkService.loadMagicLinkFromTokenOrVerificationCode(tokenOrVerificationCode, true, dataSourceModel)
            .compose(magicLink -> {
                // 2) The magic link is valid, so we memorize its usage date and also check if the request comes from
                // a registered or unregistered user (with or without an account)
                return MagicLinkService.loadUserPersonFromMagicLink(magicLink)
                    .compose(userPerson -> {
                        // 3) Preparing the userId = ModalityUserPrincipal for registered users, ModalityGuestPrincipal for unregistered users
                        Object userId;
                        if (userPerson != null) {
                            userId = new ModalityUserPrincipal(userPerson.getPrimaryKey(), userPerson.getForeignEntity("frontendAccount").getPrimaryKey());
                        } else {
                            userId = new ModalityGuestPrincipal(magicLink.getEmail());
                        }
                        // 4) Pushing the userId to the magic link client which is identified by runId = usageRunId.
                        // Pushing the userId will cause a login, and subsequently a push of the authorizations.

                        return PushServerService.pushState(StateAccessor.createUserIdState(userId), usageRunId)
                            .compose(ignored -> { // indicates that the magic link client acknowledged this login push
                                // 5) Now that we managed to reach the magic link client and have a successful login,
                                // we mark the magic link as used (to prevent using it again).
                                return MagicLinkService.markMagicLinkAsUsed(magicLink, usageRunId)
                                    .map(ignored2 -> magicLink.getRequestedPath())
                                    .onFailure(Console::log)
                                    .onSuccess(ignored2 -> {
                                        // 6) We also finally push the userId to the original login client here, and
                                        // this should be followed by a later push of the authorizations to that same
                                        // client (as explained above).
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
        return Future.failedFuture("%s.verifyAuthenticated() is not supported".formatted(getClass().getSimpleName()));
    }

    @Override
    public Future<UserClaims> getUserClaims() {
        return Future.failedFuture("%s.getUserClaims() is not supported".formatted(getClass().getSimpleName()));
    }

    @Override
    public boolean acceptsUpdateCredentialsArgument(Object updateCredentialsArgument) {
        return updateCredentialsArgument instanceof UpdatePasswordFromMagicLinkCredentials;
    }

    @Override
    public Future<?> updateCredentials(Object updateCredentialsArgument) {
        if (!(updateCredentialsArgument instanceof UpdatePasswordFromMagicLinkCredentials update)) {
            return Future.failedFuture("%s.updateCredentials() requires a %s argument".formatted(getClass().getSimpleName(), UpdatePasswordFromMagicLinkCredentials.class.getSimpleName()));
        }
        String usageRunId = ThreadLocalStateHolder.getRunId();
        // 1) Loading the email for the magic link normally associated with this magic link app userId from the database
        // This will be used to identify the account we need to change the password for.
        return EntityStore.create(dataSourceModel)
            .<MagicLink>executeQuery("select email from MagicLink where usageRunId=$1 limit 1", usageRunId)
            .compose(magicLinks -> {
                if (magicLinks.isEmpty())
                    return Future.failedFuture("[%s] Magic link not found!".formatted(ModalityAuthenticationI18nKeys.LoginLinkUnrecognisedError));
                MagicLink magicLink = magicLinks.get(0);
                // 3) Reading the user person
                return MagicLinkService.loadUserPersonFromMagicLink(magicLink)
                    .compose(userPerson -> {
                        if (userPerson == null)
                            return Future.failedFuture("[%s] No such user account".formatted(ModalityAuthenticationI18nKeys.AuthnNoSuchUserAccountError));
                        // 4) Preparing the userId = ModalityUserPrincipal for registered users, ModalityGuestPrincipal for unregistered users
                        ModalityUserPrincipal targetUserId = new ModalityUserPrincipal(userPerson.getPrimaryKey(), userPerson.getForeignEntity("frontendAccount").getPrimaryKey());
                        // 5) Pushing the userId to the original client from which the magic link request was made.
                        // The original client is identified by runId. Pushing the userId will cause a login, and
                        // subsequently a push of the authorizations.
                        UpdatePasswordCredentials updatePasswordCredentials = new UpdatePasswordCredentials(
                            userPerson.evaluate("frontendAccount.password"), // old password
                            update.newPassword() // new password
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

}
