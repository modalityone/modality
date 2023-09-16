package one.modality.crm.client.services.authz;

import dev.webfx.stack.authz.client.spi.impl.AuthorizationClientServiceProviderBase;
import dev.webfx.stack.authz.client.spi.impl.UserAuthorizationChecker;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

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
    protected UserAuthorizationChecker createUserAuthorizationChecker() {
        return new ModalityInMemoryUserAuthorizationChecker(dataSourceModel);
    }

    @Override
    public Void onAuthorizationsPush(Object pushObject) {
        //FXUserPrincipal.setUserPrincipal(FXUserId.getUserId());
        ModalityInMemoryUserAuthorizationChecker checker = (ModalityInMemoryUserAuthorizationChecker)
                getOrCreateUserAuthorizationChecker();
        checker.onAuthorizationPush(pushObject);
        return null;
    }
}
