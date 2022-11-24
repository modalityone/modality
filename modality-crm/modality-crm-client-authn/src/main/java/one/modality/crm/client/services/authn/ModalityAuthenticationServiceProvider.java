package one.modality.crm.client.services.authn;

import dev.webfx.stack.auth.authn.UserClaims;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.auth.authn.UsernamePasswordCredentials;
import dev.webfx.stack.auth.authn.spi.AuthenticationServiceProvider;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.platform.async.Future;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthenticationServiceProvider implements AuthenticationServiceProvider, HasDataSourceModel {

    private final DataSourceModel dataSourceModel;

    public ModalityAuthenticationServiceProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityAuthenticationServiceProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    @Override
    public Future<ModalityUserPrincipal> authenticate(Object userCredentials) {
        if (!(userCredentials instanceof UsernamePasswordCredentials))
            return Future.failedFuture(new IllegalArgumentException("ModalityAuthenticationServiceProvider requires a UsernamePasswordCredentials argument"));
        UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) userCredentials;
        return QueryService.executeQuery(QueryArgument.builder()
                .setLanguage("DQL")
                .setStatement("select id,frontendAccount.id from Person where frontendAccount.(corporation=? and username=? and (password=? or true)) order by id limit 1")
                .setParameters(1, usernamePasswordCredentials.getUsername(), usernamePasswordCredentials.getPassword()) // "or true" is temporary to bypass the password checking which is now encrypted TODO: implement encrypted version of password checking
                .setDataSourceId(getDataSourceId())
                .build()
        ).compose(result -> result.getRowCount() != 1 ? Future.failedFuture("Wrong user or password")
                : Future.succeededFuture(new ModalityUserPrincipal(result.getValue(0, 0), result.getValue(0, 1)))
        );
    }

    @Override
    public Future<?> verifyAuthenticated(Object userId) {
        if (userId instanceof ModalityUserPrincipal)
            return Future.succeededFuture(userId);
        return Future.failedFuture("Unauthenticated");
    }

    @Override
    public Future<UserClaims> getUserClaims(Object userId) {
        return Future.failedFuture("Not yet implemented");
    }
}
