package mongoose.base.server.services.datasource;

import dev.webfx.platform.shared.services.json.spi.impl.listmap.MapJsonObject;

import java.util.HashMap;
import java.util.Map;

public class TestMongooseLocalDataSourceProvider {

    public String connectionJson() {
        return "{" +
                "\"host\": \"localhost\"," +
                "\"port\": \"5432\"," +
                "\"databaseName\": \"mongoose\"," +
                "\"username\": \"mongoose\"," +
                "\"password\": \"mongoose\"" +
                "}";
    }

    public MapJsonObject jsonMap() {
        Map<String,String> map = new HashMap<>();
        MapJsonObject mapJsonObject = new MapJsonObject();
        mapJsonObject.setNativeElement("host", "localhost");
        mapJsonObject.setNativeElement("port", "5432");
        mapJsonObject.setNativeElement("databaseName","mongoose");
        mapJsonObject.setNativeElement("username","mongoose");
        mapJsonObject.setNativeElement("password","mongoose");
        return mapJsonObject;
    }
/* Temporarily commented for Modality refactoring (was causing build error otherwise)
    @Test
    public void loadsPropertyFromEnvironment() {
        try(
                MockedStatic<DataSourceModelService> mockModelSvc = mockStatic(DataSourceModelService.class);
                MockedStatic<ResourceService> mockResourceSvc = mockStatic(ResourceService.class);
                MockedStatic<Json> mockJsonParser = mockStatic(Json.class);
        ) {
            mockModelSvc.when(DataSourceModelService::getDefaultDataSourceId).thenReturn("id");
            mockResourceSvc.when(() -> ResourceService
                    .getText("mongoose/base/server/datasource/id/ConnectionDetails.json"))
                    .thenReturn(connectionJson());

            mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                    .thenReturn(jsonMap());

            System.setProperty("host", "database");

            MongooseLocalDataSourceProvider dataSourceProvider = new MongooseLocalDataSourceProvider();

            assertEquals("database", dataSourceProvider.getProperty(jsonMap(), "host"));

        }
    }

    @Test
    public void loadsPropertyFromConfig() {
        try(
                MockedStatic<DataSourceModelService> mockModelSvc = mockStatic(DataSourceModelService.class);
                MockedStatic<ResourceService> mockResourceSvc = mockStatic(ResourceService.class);
                MockedStatic<Json> mockJsonParser = mockStatic(Json.class);
        ) {
            mockModelSvc.when(DataSourceModelService::getDefaultDataSourceId)
                    .thenReturn("id");

            mockResourceSvc.when(() -> ResourceService
                    .getText("mongoose/base/server/datasource/id/ConnectionDetails.json"))
                    .thenReturn(connectionJson());

            mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                    .thenReturn(jsonMap());

            MongooseLocalDataSourceProvider dataSourceProvider = new MongooseLocalDataSourceProvider();
            assertEquals("localhost", dataSourceProvider.getProperty(jsonMap(), "host"));
        }
    }

 */
}
