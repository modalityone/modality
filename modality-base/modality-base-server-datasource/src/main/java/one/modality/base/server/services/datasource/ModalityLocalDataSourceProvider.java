package one.modality.base.server.services.datasource;

import dev.webfx.stack.db.datasource.LocalDataSource;
import dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;

/**
 * @author Bruno Salmon
 */
public final class ModalityLocalDataSourceProvider implements LocalDataSourceProvider {

  // Set by ModalityLocalDataSourceConfigurationConsumer on boot
  static LocalDataSource MODALITY_DATA_SOURCE;

  static Object getModalityDataSourceId() {
    return DataSourceModelService.getDefaultDataSourceId();
  }

  @Override
  public LocalDataSource getLocalDataSource(Object dataSourceId) {
    return getModalityDataSourceId().equals(dataSourceId) ? MODALITY_DATA_SOURCE : null;
  }
}
