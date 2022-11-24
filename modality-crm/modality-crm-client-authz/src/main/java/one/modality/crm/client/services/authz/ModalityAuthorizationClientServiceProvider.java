package one.modality.crm.client.services.authz;

import dev.webfx.stack.auth.authz.client.spi.impl.AuthorizationClientServiceProviderBase;
import dev.webfx.stack.auth.authz.client.spi.impl.UserPrincipalAuthorizationChecker;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.session.state.client.fx.FXUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationClientServiceProvider extends AuthorizationClientServiceProviderBase {

    private final DataSourceModel dataSourceModel;

    public ModalityAuthorizationClientServiceProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityAuthorizationClientServiceProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    protected UserPrincipalAuthorizationChecker createUserPrincipalAuthorizationChecker(Object userPrincipal) {
        return new ModalityInMemoryUserPrincipalAuthorizationChecker(userPrincipal, dataSourceModel);
    }

    @Override
    public Void onAuthorizationsPush(Object pushObject) {
        ModalityInMemoryUserPrincipalAuthorizationChecker checker = (ModalityInMemoryUserPrincipalAuthorizationChecker)
                getOrCreateUserPrincipalAuthorizationChecker(FXUserId.getUserId());
        checker.onAuthorizationPush(pushObject);
        FXUserPrincipal.setUserPrincipal(FXUserId.getUserId());
        return null;
    }
}
