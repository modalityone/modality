package one.modality.crm.client.services.authz;

import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.authz.spi.impl.AuthorizationServiceProviderBase;
import dev.webfx.stack.authz.spi.impl.UserPrincipalAuthorizationChecker;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationServiceProvider extends AuthorizationServiceProviderBase {

    private final DataSourceModel dataSourceModel;

    public ModalityAuthorizationServiceProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public ModalityAuthorizationServiceProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    protected UserPrincipalAuthorizationChecker createUserPrincipalAuthorizationChecker(Object userPrincipal) {
        return new ModalityInMemoryUserPrincipalAuthorizationChecker(userPrincipal, dataSourceModel);
    }
}
