package mongoose.base.server.services.datasource;

import dev.webfx.framework.shared.services.datasourcemodel.DataSourceModelService;
import dev.webfx.platform.shared.services.datasource.ConnectionDetails;
import dev.webfx.platform.shared.services.datasource.DBMS;
import dev.webfx.platform.shared.services.datasource.LocalDataSource;
import dev.webfx.platform.shared.services.datasource.spi.LocalDataSourceProvider;
import dev.webfx.platform.shared.services.datasource.spi.simple.SimpleLocalDataSource;
import dev.webfx.platform.shared.services.json.Json;
import dev.webfx.platform.shared.services.json.JsonObject;
import dev.webfx.platform.shared.services.log.Logger;
import dev.webfx.platform.shared.services.resource.ResourceService;

/**
 * @author Bruno Salmon
 */
public final class MongooseLocalDataSourceProvider implements LocalDataSourceProvider {

    private final LocalDataSource MONGOOSE_DATA_SOURCE;

    public MongooseLocalDataSourceProvider() {
        Object dataSourceId = DataSourceModelService.getDefaultDataSourceId();
        DBMS dbms = DBMS.POSTGRES;
        String connectionPath = "mongoose/base/server/datasource/" + dataSourceId + "/ConnectionDetails.json";
        String connectionContent = ResourceService.getText(connectionPath);
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
            Logger.log("WARNING: No connection details found for Mongoose data source (please check " + connectionPath + ")");
        MONGOOSE_DATA_SOURCE = new SimpleLocalDataSource(dataSourceId, dbms, connectionDetails);
    }

    protected String getProperty(JsonObject json, String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty())
             value = json.getString(key);
        return value;
    }

    @Override
    public LocalDataSource getLocalDataSource(Object dataSourceId) {
        return MONGOOSE_DATA_SOURCE.getId().equals(dataSourceId) ? MONGOOSE_DATA_SOURCE : null;
    }
}
