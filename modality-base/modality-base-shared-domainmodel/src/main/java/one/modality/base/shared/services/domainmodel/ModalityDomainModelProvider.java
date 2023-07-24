package one.modality.base.shared.services.domainmodel;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.domainmodel.DomainModel;
import dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider;

import one.modality.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;

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
