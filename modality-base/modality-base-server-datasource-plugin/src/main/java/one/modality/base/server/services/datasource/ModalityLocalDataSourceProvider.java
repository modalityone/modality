package one.modality.base.server.services.datasource;

import dev.webfx.stack.db.datasource.LocalDataSource;
import dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ModalityLocalDataSourceProvider implements LocalDataSourceProvider {

    private static LocalDataSource MODALITY_DATA_SOURCE;

    private static final List<Runnable> PENDING_RUNNABLES = new ArrayList<>();

    static Object getModalityDataSourceId() {
        return DataSourceModelService.getDefaultDataSourceId();
    }

    static void setModalityDataSource(LocalDataSource modalityDataSource) {
        MODALITY_DATA_SOURCE = modalityDataSource;
        PENDING_RUNNABLES.forEach(Runnable::run);
        PENDING_RUNNABLES.clear();
    }

    @Override
    public LocalDataSource getLocalDataSource(Object dataSourceId) {
        return getModalityDataSourceId().equals(dataSourceId) ? MODALITY_DATA_SOURCE : null;
    }

    @Override
    public boolean isInitialised() {
        return MODALITY_DATA_SOURCE != null;
    }

    @Override
    public void onInitialised(Runnable runnable) {
        if (isInitialised())
            runnable.run();
        else
            PENDING_RUNNABLES.add(runnable);
    }
}
