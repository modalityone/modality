package one.modality.base.shared.services.domainmodel;

import one.modality.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;
import dev.webfx.stack.orm.domainmodel.DomainModel;
import dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider;
import dev.webfx.platform.async.Future;

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
