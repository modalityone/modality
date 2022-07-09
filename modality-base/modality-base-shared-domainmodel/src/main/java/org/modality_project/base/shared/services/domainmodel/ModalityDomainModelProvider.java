package org.modality_project.base.shared.services.domainmodel;

import org.modality_project.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;
import dev.webfx.framework.shared.orm.domainmodel.DomainModel;
import dev.webfx.framework.shared.services.domainmodel.spi.DomainModelProvider;
import dev.webfx.platform.shared.async.Future;

/**
 * @author Bruno Salmon
 */
public class ModalityDomainModelProvider implements DomainModelProvider {

    @Override
    public Future<DomainModel> loadDomainModel(Object dataSourceId) {
        if (!ModalityDataSourceModelProvider.MODALITY_DATA_SOURCE_ID.equals(dataSourceId))
            return Future.failedFuture("Unknown data source " + dataSourceId);
        return Future.succeededFuture(DomainModelSnapshotLoader.getOrLoadDomainModel());
    }
}
