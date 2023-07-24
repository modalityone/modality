package one.modality.base.shared.services.datasourcemodel;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.datasourcemodel.service.spi.DataSourceModelProvider;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.service.DomainModelService;
import dev.webfx.stack.orm.dql.sqlcompiler.sql.dbms.PostgresSyntax;

/**
 * @author Bruno Salmon
 */
public final class ModalityDataSourceModelProvider implements DataSourceModelProvider {

  public static final Object MODALITY_DATA_SOURCE_ID = "MDS";

  private static final DataSourceModel MODALITY_DATA_SOURCE_MODEL =
      new DataSourceModel(
          MODALITY_DATA_SOURCE_ID,
          PostgresSyntax.get(),
          DomainModelService.loadDomainModel(MODALITY_DATA_SOURCE_ID).result());

  @Override
  public Object getDefaultDataSourceId() {
    return MODALITY_DATA_SOURCE_ID;
  }

  @Override
  public DataSourceModel getDefaultDataSourceModel() {
    return MODALITY_DATA_SOURCE_MODEL;
  }

  @Override
  public DataSourceModel getDataSourceModel(Object dataSourceId) {
    return MODALITY_DATA_SOURCE_ID.equals(dataSourceId) ? MODALITY_DATA_SOURCE_MODEL : null;
  }

  @Override
  public Future<DataSourceModel> loadDataSourceModel(Object dataSourceId) {
    return MODALITY_DATA_SOURCE_ID.equals(dataSourceId)
        ? Future.succeededFuture(MODALITY_DATA_SOURCE_MODEL)
        : Future.failedFuture("Unknown data source model id=" + dataSourceId);
  }
}
