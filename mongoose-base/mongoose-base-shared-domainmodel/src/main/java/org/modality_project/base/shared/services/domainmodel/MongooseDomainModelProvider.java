package org.modality_project.base.shared.services.domainmodel;

import org.modality_project.base.shared.services.datasourcemodel.MongooseDataSourceModelProvider;
import dev.webfx.framework.shared.orm.domainmodel.DomainModel;
import dev.webfx.framework.shared.services.domainmodel.spi.DomainModelProvider;
import dev.webfx.platform.shared.async.Future;

/**
 * @author Bruno Salmon
 */
public class MongooseDomainModelProvider implements DomainModelProvider {

    @Override
    public Future<DomainModel> loadDomainModel(Object dataSourceId) {
        if (!MongooseDataSourceModelProvider.MONGOOSE_DATA_SOURCE_ID.equals(dataSourceId))
            return Future.failedFuture("Unknown data source " + dataSourceId);
        return Future.succeededFuture(DomainModelSnapshotLoader.getOrLoadDomainModel());
    }
}
