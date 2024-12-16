package one.modality.crm.server.authn.gateway;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGatewayProvider;
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
import one.modality.crm.server.authn.gateway.shared.LoginLinkService;
import one.modality.crm.shared.services.authn.ModalityAuthenticationI18nKeys;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.util.Objects;


/**
 * @author Bruno Salmon
 */
public final class ModalityUsernamePasswordAuthenticationGatewayProvider implements ServerAuthenticationGatewayProvider, HasDataSourceModel {

    private static final String CREATE_ACCOUNT_ACTIVITY_PATH_PREFIX = "/create-account";
    private static final String CREATE_ACCOUNT_ACTIVITY_PATH_FULL = CREATE_ACCOUNT_ACTIVITY_PATH_PREFIX + "/:token";

    private static final String CREATE_ACCOUNT_LINK_MAIL_FROM = null;
    private static final String CREATE_ACCOUNT_LINK_MAIL_SUBJECT = "Create account";
    private static final String CREATE_ACCOUNT_LINK_MAIL_BODY = "[loginLink]";

    private static final String UPDATE_EMAIL_ACTIVITY_PATH_PREFIX = "/user-profile/email-update";
    private static final String UPDATE_EMAIL_ACTIVITY_PATH_FULL = UPDATE_EMAIL_ACTIVITY_PATH_PREFIX + "/:token";

    private static final String UPDATE_EMAIL_LINK_MAIL_FROM = null;
    private static final String UPDATE_EMAIL_LINK_MAIL_SUBJECT = "Update email";
    private static final String UPDATE_EMAIL_LINK_MAIL_BODY = "[loginLink]";

    private final DataSourceModel dataSourceModel;

    public ModalityUsernamePasswordAuthenticationGatewayProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityUsernamePasswordAuthenticationGatewayProvider(DataSourceModel dataSourceModel) {
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
        String runId = ThreadLocalStateHolder.getRunId();
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (Strings.isEmpty(username) || Strings.isEmpty(password))
            return Future.failedFuture("[%s] Username and password must not be empty".formatted(ModalityAuthenticationI18nKeys.AuthnUserOrPasswordEmptyError));
        username = username.trim(); // Ignoring leading and tailing spaces in username
        if (username.contains("@")) // If username is an email address, it shouldn't be case-sensitive
            username = username.toLowerCase(); // emails are stored in lowercase in the database
        return EntityStore.create(dataSourceModel)
            .<Person>executeQuery("select id,frontendAccount.(password, salt) from Person where frontendAccount.(corporation=? and username=?) order by id limit 1", 1, username)
            .compose(persons -> {
                if (persons.size() != 1)
                    return Future.failedFuture("[%s] Wrong user or password".formatted(ModalityAuthenticationI18nKeys.AuthnWrongUserOrPasswordError));
                Person userPerson = persons.get(0);
                FrontendAccount fa = userPerson.getFrontendAccount();
                String encryptedPassword = encryptPassword(password, fa.getSalt());
                if (Objects.equals(encryptedPassword, fa.getPassword()))
                    return Future.failedFuture("[%s] Wrong user or password".formatted(ModalityAuthenticationI18nKeys.AuthnWrongUserOrPasswordError));
                Object personId = userPerson.getPrimaryKey();
                Object accountId = Entities.getPrimaryKey(userPerson.getForeignEntityId("frontendAccount"));
                ModalityUserPrincipal modalityUserPrincipal = new ModalityUserPrincipal(personId, accountId);
                return PushServerService.pushState(StateAccessor.createUserIdState(modalityUserPrincipal), runId);
            });
    }

    private Future<Void> sendAccountCreationLink(InitiateAccountCreationCredentials credentials) {
        return LoginLinkService.storeAndSendLoginLink(
            credentials,
            CREATE_ACCOUNT_ACTIVITY_PATH_FULL,
            CREATE_ACCOUNT_LINK_MAIL_FROM,
            CREATE_ACCOUNT_LINK_MAIL_SUBJECT,
            CREATE_ACCOUNT_LINK_MAIL_BODY,
            dataSourceModel
        );
    }

    private Future<String> continueAccountCreation(ContinueAccountCreationCredentials credentials) {
        return LoginLinkService.loadLoginLinkFromTokenAndMarkAsUsed(credentials.getToken(), dataSourceModel)
            .compose(magicLink -> LoginLinkService.loadUserPersonFromLoginLink(magicLink)
                .compose(userPerson -> {
                    String email = magicLink.getEmail();
                    if (userPerson != null)
                        return Future.succeededFuture("[%s] There is already an account associated with %s".formatted(ModalityAuthenticationI18nKeys.CreateAccountAlreadyExistsError, email));
                    return Future.succeededFuture(email);
                })
            );
    }

    private Future<Object> finaliseAccountCreation(FinaliseAccountCreationCredentials credentials) {
        return LoginLinkService.loadLoginLinkFromToken(credentials.getToken(), false, dataSourceModel)
            .compose(magicLink -> {
                UpdateStore updateStore = UpdateStore.create(dataSourceModel);
                FrontendAccount fa = updateStore.insertEntity(FrontendAccount.class);
                String email = magicLink.getEmail();
                String salt = email; // like KBS2 for now
                fa.setUsername(email);
                fa.setSalt(salt);
                fa.setPassword(encryptPassword(credentials.getPassword(), salt));
                fa.setCorporation(1);
                return updateStore.submitChanges()
                    .map(ignored -> fa.getPrimaryKey());
            });
    }

    private Future<Void> sendEmailUpdateLink(InitiateEmailUpdateCredentials credentials) {
        return LoginLinkService.storeAndSendLoginLink(
            credentials,
            UPDATE_EMAIL_ACTIVITY_PATH_FULL,
            UPDATE_EMAIL_LINK_MAIL_FROM,
            UPDATE_EMAIL_LINK_MAIL_SUBJECT,
            UPDATE_EMAIL_LINK_MAIL_BODY,
            dataSourceModel
        );
    }

    private Future<Void> finaliseEmailUpdate(FinaliseEmailUpdateCredentials credentials) {
        return LoginLinkService.loadLoginLinkFromTokenAndMarkAsUsed(credentials.getToken(), dataSourceModel)
            .compose(magicLink -> LoginLinkService.loadUserPersonFromLoginLink(magicLink)
                .compose(userPerson -> {
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
        Console.log("👮👮👮👮👮 Checking userId=[%s]".formatted(userId));
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
        Object userId = ThreadLocalStateHolder.getUserId();
        if (!(userId instanceof ModalityUserPrincipal modalityUserPrincipal))
            return Future.failedFuture("[%s] This userId object is not recognized by Modality".formatted(ModalityAuthenticationI18nKeys.AuthnUnrecognizedUserIdError));
        return EntityStore.create(dataSourceModel)
            .<Person>executeQuery("select " + fields + " from Person where id=? and frontendAccount=?", modalityUserPrincipal.getUserPersonId(), modalityUserPrincipal.getUserAccountId())
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
        // 1) We first check that the passed old password matches with the one in database
        return queryModalityUserPerson("frontendAccount.(username, password, salt)")
            .compose(userPerson -> {
                FrontendAccount fa = userPerson.getFrontendAccount();
                String oldEncryptedDbPassword = fa.getPassword();
                String salt = fa.getSalt();
                String oldEncryptedUserPassword = encryptPassword(passwordUpdate.getOldPassword(), salt);
                if (!Objects.equals(oldEncryptedDbPassword, oldEncryptedUserPassword))
                    return Future.failedFuture("[%s] The old password is not matching".formatted(ModalityAuthenticationI18nKeys.AuthnOldPasswordNotMatchingError));
                String newEncryptedUserPassword = encryptPassword(passwordUpdate.getNewPassword(), salt);
                // 2) We update the password in the database
                UpdateStore updateStore = UpdateStore.createAbove(fa.getStore());
                FrontendAccount ufa = updateStore.updateEntity(fa);
                ufa.setPassword(newEncryptedUserPassword);
                return updateStore.submitChanges();
            });
    }

    @Override
    public Future<Void> logout() {
        return LogoutPush.pushLogoutMessageToClient();
    }

}
