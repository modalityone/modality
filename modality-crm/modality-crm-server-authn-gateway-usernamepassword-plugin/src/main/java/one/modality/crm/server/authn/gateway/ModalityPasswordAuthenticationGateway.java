package one.modality.crm.server.authn.gateway;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGateway;
import dev.webfx.stack.hash.md5.Md5;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.StateAccessor;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;
import one.modality.crm.server.authn.gateway.shared.MagicLinkService;
import one.modality.crm.shared.services.authn.ModalityAuthenticationI18nKeys;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.server.authn.gateway.magiclink.ModalityMagicLinkAuthenticationGateway;

import java.util.Objects;


/**
 * @author Bruno Salmon
 */
public final class ModalityPasswordAuthenticationGateway implements ServerAuthenticationGateway, HasDataSourceModel {

    private static final boolean LOG_INCORRECT_TYPED_PASSWORDS = true; // Used for debugging purposes only

    private static final String CREATE_ACCOUNT_ACTIVITY_PATH_PREFIX = "/create-account";
    private static final String CREATE_ACCOUNT_ACTIVITY_PATH_FULL = CREATE_ACCOUNT_ACTIVITY_PATH_PREFIX + "/:token";

    // Temporarily hardcoded (to replace with database letters)
    private static final String CREATE_ACCOUNT_MAIL_FROM = "kbs@kadampa.net";
    private static final String CREATE_ACCOUNT_MAIL_SUBJECT = "Account creation - Kadampa Booking System";
    private static final String CREATE_ACCOUNT_MAIL_BODY = Resource.getText(Resource.toUrl("AccountCreationMailBody.html", ModalityPasswordAuthenticationGateway.class));

    private static final String CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_FROM = "kbs@kadampa.net";
    private static final String CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_SUBJECT = "Account already exists - Kadampa Booking System";
    private static final String CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_BODY = Resource.getText(Resource.toUrl("AccountCreationAlreadyExistsMailBody.html", ModalityPasswordAuthenticationGateway.class));

    private static final String UPDATE_EMAIL_ACTIVITY_PATH_PREFIX = "/user-profile/email-update";
    private static final String UPDATE_EMAIL_ACTIVITY_PATH_FULL = UPDATE_EMAIL_ACTIVITY_PATH_PREFIX + "/:token";

    private static final String UPDATE_EMAIL_MAIL_FROM = "kbs@kadampa.net";
    private static final String UPDATE_EMAIL_MAIL_SUBJECT = "Account email change - Kadampa Booking System";
    private static final String UPDATE_EMAIL_MAIL_BODY = Resource.getText(Resource.toUrl("AccountEmailUpdateMailBody.html", ModalityPasswordAuthenticationGateway.class));

    private final DataSourceModel dataSourceModel;

    public ModalityPasswordAuthenticationGateway() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityPasswordAuthenticationGateway(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    @Override
    public boolean acceptsUserCredentials(Object userCredentials) {
        return userCredentials instanceof AuthenticateWithUsernamePasswordCredentials
               || userCredentials instanceof InitiateAccountCreationCredentials
               || userCredentials instanceof ContinueAccountCreationCredentials
               || userCredentials instanceof FinaliseAccountCreationCredentials
               || userCredentials instanceof InitiateEmailUpdateCredentials
               || userCredentials instanceof FinaliseEmailUpdateCredentials
            ;
    }

    @Override
    public Future<?> authenticate(Object credentials) {
        if (credentials instanceof AuthenticateWithUsernamePasswordCredentials cred) {
            return authenticateWithUsernamePassword(cred);
        }
        if (credentials instanceof InitiateAccountCreationCredentials cred) {
            return sendAccountCreationLink(cred);
        }
        if (credentials instanceof ContinueAccountCreationCredentials cred) {
            return continueAccountCreation(cred);
        }
        if (credentials instanceof FinaliseAccountCreationCredentials cred) {
            return finaliseAccountCreation(cred);
        }
        if (credentials instanceof InitiateEmailUpdateCredentials cred) {
            return sendEmailUpdateLink(cred);
        }
        if (credentials instanceof FinaliseEmailUpdateCredentials cred) {
            return finaliseEmailUpdate(cred);
        }
        return Future.failedFuture("[%s] requires a %s argument".formatted(getClass().getSimpleName(), AuthenticateWithUsernamePasswordCredentials.class.getSimpleName()));
    }

    private Future<Void> authenticateWithUsernamePassword(AuthenticateWithUsernamePasswordCredentials credentials) {
        // Capturing the required client state info from thread local (before it will be wiped out by the async call)
        String runId = ThreadLocalStateHolder.getRunId();
        boolean isBackofficeAuthentication = ThreadLocalStateHolder.isBackoffice();
        // Capturing the parameters from the credentials
        String username = credentials.username();
        String password = credentials.password();
        if (Strings.isEmpty(username) || Strings.isEmpty(password))
            return Future.failedFuture("[%s] Username and password must not be empty".formatted(ModalityAuthenticationI18nKeys.AuthnUserOrPasswordEmptyError));
        username = username.trim(); // Ignoring leading and tailing spaces in username
        if (username.contains("@")) // If the username is an email address, it shouldn't be case-sensitive
            username = username.toLowerCase(); // emails are stored in lowercase in the database
        return EntityStore.create(dataSourceModel)
            // Note: accounts are created from the front-office and can first be used only to log in in the front-office,
            // but an admin or superadmin can mark them as back-office and can then be used to log in in the back-office as well.
            .<Person>executeQuery("select id,frontendAccount.(password, salt) from Person where frontendAccount.(corporation=? and username=? and !removed and !disabled and (?=false or backoffice)) order by owner desc, id limit 1", 1, username, isBackofficeAuthentication)
            .compose(persons -> {
                if (persons.size() != 1)
                    return Future.failedFuture("[%s] Wrong user or password".formatted(ModalityAuthenticationI18nKeys.AuthnWrongUserOrPasswordError));
                Person userPerson = persons.get(0);
                FrontendAccount fa = userPerson.getFrontendAccount();
                if (!isTypedPasswordCorrect(password, fa.getPassword(), fa.getSalt())) {
                    if (LOG_INCORRECT_TYPED_PASSWORDS)
                        Console.log("🚫🚫🚫🚫🚫 User %s typed this incorrect password: %s".formatted(credentials.username(), password));
                    return Future.failedFuture("[%s] Wrong user or password".formatted(ModalityAuthenticationI18nKeys.AuthnWrongUserOrPasswordError));
                }
                Object personId = userPerson.getPrimaryKey();
                Object accountId = Entities.getPrimaryKey(userPerson.getForeignEntityId("frontendAccount"));
                ModalityUserPrincipal modalityUserPrincipal = new ModalityUserPrincipal(personId, accountId);
                return PushServerService.pushState(StateAccessor.createUserIdState(modalityUserPrincipal), runId);
            });
    }

    private boolean isTypedPasswordCorrect(String typedPassword, String storedEncryptedPassword, String salt) {
        // The typed password is not encrypted, so we encrypt it to compare it with the stored one
        String typedEncryptedPassword = encryptPassword(typedPassword, salt);
        // If both encrypted passwords are equals (the typed one and the stored one), then the password is correct
        if (Objects.equals(typedEncryptedPassword, storedEncryptedPassword))
            return true;
        // We provide an alternative possibility is to type the encrypted password (only the staff can know it from the
        // back-office). This allows the support team to log in as a specific user to check what he or she sees in the
        // front-office.
        if (Objects.equals(typedPassword, storedEncryptedPassword))
            return true;
        // Otherwise, the password is incorrect
        return false;
    }

    private Future<Void> sendAccountCreationLink(InitiateAccountCreationCredentials credentials) {
        // We check that the requested account doesn't exist in the database. If it doesn't exist, we send an
        // "Account creation" email as requested. But if it exists, we send an "Account already exists" email instead.
        // For this later case, we still create a login link that will actually act as a magic link.
        String loginRunId = ThreadLocalStateHolder.getRunId(); // Capturing the loginRunId before async operation
        return EntityStore.create(dataSourceModel)
            .<FrontendAccount>executeQuery("select FrontendAccount where corporation=? and username=? limit 1", 1, credentials.getEmail())
            .compose(accounts -> {
                    boolean doesntExists = accounts.isEmpty();
                    return MagicLinkService.createAndSendMagicLink(
                        loginRunId,
                        credentials,
                        null,
                        doesntExists ? CREATE_ACCOUNT_ACTIVITY_PATH_FULL : ModalityMagicLinkAuthenticationGateway.MAGIC_LINK_ACTIVITY_PATH_FULL,
                        doesntExists ? CREATE_ACCOUNT_MAIL_FROM : CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_FROM,
                        doesntExists ? CREATE_ACCOUNT_MAIL_SUBJECT : CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_SUBJECT,
                        doesntExists ? CREATE_ACCOUNT_MAIL_BODY : CREATE_ACCOUNT_ALREADY_EXISTS_MAIL_BODY,
                        dataSourceModel
                    );
                }
            );
    }

    private Future<String> continueAccountCreation(ContinueAccountCreationCredentials credentials) {
        return MagicLinkService.loadMagicLinkFromTokenAndMarkAsUsed(credentials.magicLinkTokenOrVerificationCode(), dataSourceModel)
            .compose(magicLink -> MagicLinkService.loadUserPersonFromMagicLink(magicLink)
                .compose(userPerson -> {
                    String email = magicLink.getEmail();
                    if (userPerson != null)
                        return Future.succeededFuture("[%s] There is already an account associated with %s".formatted(ModalityAuthenticationI18nKeys.CreateAccountAlreadyExistsError, email));
                    return Future.succeededFuture(email);
                })
            );
    }

    private Future<Object> finaliseAccountCreation(FinaliseAccountCreationCredentials credentials) {
        return MagicLinkService.loadMagicLinkFromTokenOrVerificationCode(credentials.magicLinkTokenOrVerificationCode(), false, dataSourceModel)
            .compose(magicLink -> {
                UpdateStore updateStore = UpdateStore.create(dataSourceModel);
                FrontendAccount fa = updateStore.insertEntity(FrontendAccount.class);
                String email = magicLink.getEmail();
                String salt = email; // like KBS2 for now
                fa.setUsername(email);
                fa.setSalt(salt);
                fa.setPassword(encryptPassword(credentials.password(), salt));
                fa.setCorporation(1);
                return updateStore.submitChanges()
                    .map(ignored -> fa.getPrimaryKey());
            });
    }

    private Future<Void> sendEmailUpdateLink(InitiateEmailUpdateCredentials credentials) {
        // Capturing the required client state info from thread local (before it will be wiped out by the async call)
        String runId = ThreadLocalStateHolder.getRunId();
        return getUserClaims() // to get the old email (the passed credential contains the new email)
            .compose(userClaims -> MagicLinkService.createAndSendMagicLink(
                    runId,
                    credentials, // contains the new email (the one to send the link to)
                    userClaims.email(), // current email for this account (= old email)
                    UPDATE_EMAIL_ACTIVITY_PATH_FULL,
                UPDATE_EMAIL_MAIL_FROM,
                UPDATE_EMAIL_MAIL_SUBJECT,
                UPDATE_EMAIL_MAIL_BODY,
                    dataSourceModel
                )
            );
    }

    private Future<Void> finaliseEmailUpdate(FinaliseEmailUpdateCredentials credentials) {
        // We check the validity of the token, and if valid, we load the user person
        return MagicLinkService.loadMagicLinkFromTokenAndMarkAsUsed(credentials.magicLinkTokenOrVerificationCode(), dataSourceModel)
            .compose(magicLink -> MagicLinkService.loadUserPersonFromMagicLink(magicLink)
                .compose(userPerson -> {
                    // We change the email in both the account, and the user person
                    UpdateStore updateStore = UpdateStore.create(dataSourceModel);
                    updateStore.updateEntity(userPerson.getFrontendAccount()).setUsername(magicLink.getEmail());
                    updateStore.updateEntity(userPerson).setEmail(magicLink.getEmail());
                    return updateStore.submitChanges()
                        .map(ignored -> null);
                }));
    }

    private String encryptPassword(String password, String salt) {
        // KBS2 way of encrypting the password
        String toEncrypt = salt + ":" + Md5.hash(password);
        return Md5.hash(toEncrypt); // encrypted
    }

    @Override
    public boolean acceptsUserId() {
        Object userId = ThreadLocalStateHolder.getUserId();
        return userId instanceof ModalityUserPrincipal;
    }

    @Override
    public Future<?> verifyAuthenticated() {
        Object userId = ThreadLocalStateHolder.getUserId();
        Console.log("👮 Checking userId=[%s]".formatted(userId));
        return queryModalityUserPerson("id")
            .map(ignoredQueryResult -> userId);
    }

    @Override
    public Future<UserClaims> getUserClaims() {
        return queryModalityUserPerson("frontendAccount.username,email,phone")
            .map(userPerson -> {
                String username = userPerson.evaluate("frontendAccount.username");
                String email = userPerson.getEmail();
                String phone = userPerson.getPhone();
                return new UserClaims(username, email, phone, null);
            });
    }

    private Future<Person> queryModalityUserPerson(String fields) {
        // Capturing the required client state info from thread local (before it will be wiped out by the async call)
        Object userId = ThreadLocalStateHolder.getUserId();
        boolean isBackofficeAuthentication = ThreadLocalStateHolder.isBackoffice();
        if (!(userId instanceof ModalityUserPrincipal modalityUserPrincipal))
            return Future.failedFuture("[%s] This userId object is not recognized by Modality".formatted(ModalityAuthenticationI18nKeys.AuthnUnrecognizedUserIdError));
        return EntityStore.create(dataSourceModel)
            .<Person>executeQuery("select " + fields + " from Person where id=? and frontendAccount.(id=? and !disabled and (?=false or backoffice)", modalityUserPrincipal.getUserPersonId(), modalityUserPrincipal.getUserAccountId(), isBackofficeAuthentication)
            .compose(persons -> {
                if (persons.size() != 1)
                    return Future.failedFuture("[%s] No such user account".formatted(ModalityAuthenticationI18nKeys.AuthnNoSuchUserAccountError));
                return Future.succeededFuture(persons.get(0));
            });
    }

    @Override
    public boolean acceptsUpdateCredentialsArgument(Object updateCredentialsArgument) {
        return updateCredentialsArgument instanceof UpdatePasswordCredentials;
    }

    @Override
    public Future<?> updateCredentials(Object updateCredentialsArgument) {
        if (!acceptsUpdateCredentialsArgument(updateCredentialsArgument))
            return Future.failedFuture(getClass().getSimpleName() + ".updateCredentials() requires a " + UpdatePasswordCredentials.class.getSimpleName() + " argument");
        UpdatePasswordCredentials passwordUpdate = (UpdatePasswordCredentials) updateCredentialsArgument;
        // 1) We first check that the passed old password matches with the one in the database
        return queryModalityUserPerson("frontendAccount.(username, password, salt)")
            .compose(userPerson -> {
                String oldPassword = passwordUpdate.oldPassword();
                // Note: in case of resetting the password from a magic link, the old password is not typed by the user
                // but loaded again from the database (by the MagicLink gateway) and is therefore already encrypted.
                // Otherwise (when the password reset originates from the user profile), the old password is in clear
                // (what the user directly typed).
                FrontendAccount fa = userPerson.getFrontendAccount();
                // isTypedPasswordCorrect() is handling both cases (oldPassword in clear or already encrypted)
                if (!isTypedPasswordCorrect(oldPassword, fa.getPassword(), fa.getSalt()))
                    return Future.failedFuture("[%s] The old password is not matching".formatted(ModalityAuthenticationI18nKeys.AuthnOldPasswordNotMatchingError));
                // 2) We update the password in the database
                String storedEncryptedPassword = encryptPassword(passwordUpdate.newPassword(), fa.getSalt());
                UpdateStore updateStore = UpdateStore.createAbove(fa.getStore());
                FrontendAccount ufa = updateStore.updateEntity(fa);
                ufa.setPassword(storedEncryptedPassword);
                return updateStore.submitChanges();
            });
    }

    @Override
    public Future<Void> logout() {
        return LogoutPush.pushLogoutMessageToClient();
    }

}
