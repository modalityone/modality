package org.modality_project.base.server.services.datasource;

import dev.webfx.stack.platform.json.spi.impl.listmap.MapJsonObject;

import java.util.HashMap;
import java.util.Map;

public class TestModalityLocalDataSourceProvider {

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
                    .getText("org/modality_project/base/server/datasource/id/ConnectionDetails.json"))
                    .thenReturn(connectionJson());

            mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                    .thenReturn(jsonMap());

            System.setProperty("host", "database");

            ModalityLocalDataSourceProvider dataSourceProvider = new ModalityLocalDataSourceProvider();

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
                    .getText("org/modality_project/base/server/datasource/id/ConnectionDetails.json"))
                    .thenReturn(connectionJson());

            mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                    .thenReturn(jsonMap());

            ModalityLocalDataSourceProvider dataSourceProvider = new ModalityLocalDataSourceProvider();
            assertEquals("localhost", dataSourceProvider.getProperty(jsonMap(), "host"));
        }
    }

 */
}
