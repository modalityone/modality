package one.modality.base.server.services.datasource;

import static one.modality.base.server.services.datasource.ModalityLocalDataSourceProvider.MODALITY_DATA_SOURCE;
import static one.modality.base.server.services.datasource.ModalityLocalDataSourceProvider.getModalityDataSourceId;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.keyobject.ReadOnlyKeyObject;
import dev.webfx.stack.conf.spi.impl.resource.DefaultResourceConfigurationConsumer;
import dev.webfx.stack.db.datasource.ConnectionDetails;
import dev.webfx.stack.db.datasource.DBMS;
import dev.webfx.stack.db.datasource.spi.simple.SimpleLocalDataSource;

/**
 * @author Bruno Salmon
 */
public final class ModalityLocalDataSourceConfigurationConsumer
        extends DefaultResourceConfigurationConsumer {

    private static final String CONFIGURATION_NAME = "ModalityDatabase";
    private static final String DEFAULT_CONFIGURATION_RESOURCE_FILE_NAME =
            "ModalityDatabase.default.json";

    public ModalityLocalDataSourceConfigurationConsumer() {
        super(CONFIGURATION_NAME, DEFAULT_CONFIGURATION_RESOURCE_FILE_NAME);
    }

    @Override
    public Future<Void> boot(ReadOnlyKeyObject config) {
        if (config == null)
            return Future.failedFuture(
                    "WARNING: No connection details found for Modality data source");
        ConnectionDetails connectionDetails =
                new ConnectionDetails(
                        config.getString("host"),
                        config.getInteger("port", -1),
                        config.getString("filePath"),
                        config.getString("databaseName"),
                        config.getString("url"),
                        config.getString("username"),
                        config.getString("password"));
        Object dataSourceId = getModalityDataSourceId();
        DBMS dbms = DBMS.POSTGRES;
        MODALITY_DATA_SOURCE = new SimpleLocalDataSource(dataSourceId, dbms, connectionDetails);
        return Future.succeededFuture();
    }
}
