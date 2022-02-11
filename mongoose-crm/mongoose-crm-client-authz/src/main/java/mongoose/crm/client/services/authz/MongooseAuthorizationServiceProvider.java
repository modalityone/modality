package mongoose.crm.client.services.authz;

import dev.webfx.framework.shared.orm.domainmodel.DataSourceModel;
import dev.webfx.framework.shared.services.authz.spi.impl.AuthorizationServiceProviderBase;
import dev.webfx.framework.shared.services.authz.spi.impl.UserPrincipalAuthorizationChecker;
import dev.webfx.framework.shared.services.datasourcemodel.DataSourceModelService;

/**
 * @author Bruno Salmon
 */
public final class MongooseAuthorizationServiceProvider extends AuthorizationServiceProviderBase {

    private final DataSourceModel dataSourceModel;

    public MongooseAuthorizationServiceProvider() {
        this(DataSourceModelService.getDefaultDataSourceModel());
    }

    public MongooseAuthorizationServiceProvider(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    protected UserPrincipalAuthorizationChecker createUserPrincipalAuthorizationChecker(Object userPrincipal) {
        return new MongooseInMemoryUserPrincipalAuthorizationChecker(userPrincipal, dataSourceModel);
    }
}
