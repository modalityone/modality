package org.modality_project.base.server.services.datasource;

import dev.webfx.stack.framework.shared.services.datasourcemodel.DataSourceModelService;
import dev.webfx.stack.db.datasource.ConnectionDetails;
import dev.webfx.stack.db.datasource.DBMS;
import dev.webfx.stack.db.datasource.LocalDataSource;
import dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider;
import dev.webfx.stack.db.datasource.spi.simple.SimpleLocalDataSource;
import dev.webfx.stack.platform.json.Json;
import dev.webfx.stack.platform.json.JsonObject;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;

/**
 * @author Bruno Salmon
 */
public final class ModalityLocalDataSourceProvider implements LocalDataSourceProvider {

    private final LocalDataSource MODALITY_DATA_SOURCE;

    public ModalityLocalDataSourceProvider() {
        Object dataSourceId = DataSourceModelService.getDefaultDataSourceId();
        DBMS dbms = DBMS.POSTGRES;
        String connectionPath = "org/modality_project/base/server/datasource/" + dataSourceId + "/ConnectionDetails.json";
        String connectionContent = Resource.getText(connectionPath);
        JsonObject json = connectionContent == null ? null : Json.parseObject(connectionContent);
        //getProperty(json);

        ConnectionDetails connectionDetails = json == null ? null : new ConnectionDetails(
                getProperty(json, "host"),
                json.getInteger("port", -1),
                json.getString("filePath"),
                json.getString("databaseName"),
                json.getString("url"),
                json.getString("username"),
                json.getString("password")
        );
        if (connectionDetails == null)
            Console.log("WARNING: No connection details found for Modality data source (please check " + connectionPath + ")");
        MODALITY_DATA_SOURCE = new SimpleLocalDataSource(dataSourceId, dbms, connectionDetails);
    }

    protected String getProperty(JsonObject json, String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty())
             value = json.getString(key);
        return value;
    }

    @Override
    public LocalDataSource getLocalDataSource(Object dataSourceId) {
        return MODALITY_DATA_SOURCE.getId().equals(dataSourceId) ? MODALITY_DATA_SOURCE : null;
    }
}
