package org.modality_project.crm.client.services.authz;

import dev.webfx.stack.framework.shared.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.framework.shared.services.authz.spi.impl.AuthorizationServiceProviderBase;
import dev.webfx.stack.framework.shared.services.authz.spi.impl.UserPrincipalAuthorizationChecker;
import dev.webfx.stack.framework.shared.services.datasourcemodel.DataSourceModelService;

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
