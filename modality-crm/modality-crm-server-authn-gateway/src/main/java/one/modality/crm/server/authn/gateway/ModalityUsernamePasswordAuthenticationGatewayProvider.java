package one.modality.crm.server.authn.gateway;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.authn.UsernamePasswordCredentials;
import dev.webfx.stack.authn.logout.server.LogoutPush;
import dev.webfx.stack.authn.server.gateway.spi.ServerAuthenticationGatewayProvider;
import dev.webfx.stack.authn.spi.AuthenticatorInfo;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.StateAccessor;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;

import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class ModalityUsernamePasswordAuthenticationGatewayProvider
        implements ServerAuthenticationGatewayProvider, HasDataSourceModel {

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
    public AuthenticatorInfo getAuthenticatorInfo() {
        return null;
    }

    @Override
    public boolean acceptsUserCredentials(Object userCredentials) {
        return userCredentials instanceof UsernamePasswordCredentials;
    }

    @Override
    public Future<Void> authenticate(Object userCredentials) {
        if (!acceptsUserCredentials(userCredentials))
            return Future.failedFuture(
                    getClass().getSimpleName()
                            + " requires a "
                            + UsernamePasswordCredentials.class.getSimpleName()
                            + " argument");
        String runId = ThreadLocalStateHolder.getRunId();
        UsernamePasswordCredentials usernamePasswordCredentials =
                (UsernamePasswordCredentials) userCredentials;
        return QueryService.executeQuery(
                        QueryArgument.builder()
                                .setLanguage("DQL")
                                .setStatement(
                                        "select id,frontendAccount.id from Person where frontendAccount.(corporation=? and username=? and 'password'=?) order by id limit 1")
                                .setParameters(
                                        1,
                                        usernamePasswordCredentials.getUsername(),
                                        usernamePasswordCredentials
                                                .getPassword()) // "or true" is temporary to bypass
                                // the password checking which is
                                // now encrypted TODO: implement
                                // encrypted version of password
                                // checking
                                .setDataSourceId(getDataSourceId())
                                .build())
                .compose(
                        result -> {
                            if (result.getRowCount() != 1)
                                return Future.failedFuture("Wrong user or password");
                            Object personId = result.getValue(0, 0);
                            Object accountId = result.getValue(0, 1);
                            ModalityUserPrincipal modalityUserPrincipal =
                                    new ModalityUserPrincipal(personId, accountId);
                            return PushServerService.pushState(
                                    StateAccessor.setUserId(null, modalityUserPrincipal), runId);
                        });
    }

    @Override
    public boolean acceptsUserId() {
        Object userId = ThreadLocalStateHolder.getUserId();
        return userId instanceof ModalityUserPrincipal;
    }

    @Override
    public Future<?> verifyAuthenticated() {
        Object userId = ThreadLocalStateHolder.getUserId();
        return queryModalityUserInfo("id").map(ignoredQueryResult -> userId);
    }

    @Override
    public Future<UserClaims> getUserClaims() {
        return queryModalityUserInfo("frontendAccount.username,email,phone")
                .map(
                        queryResult -> {
                            // First 2 columns are person.id and frontendAccount.id, so we start
                            // collecting data from column 2
                            String username = queryResult.getValue(0, 2);
                            String email = queryResult.getValue(0, 3);
                            String phone = queryResult.getValue(0, 4);
                            return new UserClaims(username, email, phone, null);
                        });
    }

    private Future<QueryResult> queryModalityUserInfo(String fields) {
        Object userId = ThreadLocalStateHolder.getUserId();
        if (!(userId instanceof ModalityUserPrincipal))
            return Future.failedFuture("This userId object is not recognized by Modality");
        ModalityUserPrincipal modalityUserPrincipal = (ModalityUserPrincipal) userId;
        return QueryService.executeQuery(
                        QueryArgument.builder()
                                .setLanguage("DQL")
                                .setStatement(
                                        "select "
                                                + fields
                                                + " from Person where id=? and frontendAccount=?")
                                .setParameters(
                                        modalityUserPrincipal.getUserPersonId(),
                                        modalityUserPrincipal.getUserAccountId())
                                .setDataSourceId(getDataSourceId())
                                .build())
                .compose(
                        result -> {
                            if (result.getRowCount() != 1)
                                return Future.failedFuture("No such user in Modality database");
                            return Future.succeededFuture(result);
                        });
    }

    @Override
    public Future<Void> logout() {
        return LogoutPush.pushLogoutMessageToClient();
    }
}
