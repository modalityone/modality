package org.modality_project.base.server.services.datasource;

import dev.webfx.platform.resource.Resource;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.platform.json.Json;
import dev.webfx.stack.platform.json.spi.impl.listmap.MapJsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class TestModalityLocalDataSourceProvider {

    public String connectionJson() {
        return "{" +
                "\"host\": \"localhost\"," +
                "\"port\": \"5432\"," +
                "\"databaseName\": \"modality\"," +
                "\"username\": \"modality\"," +
                "\"password\": \"modality\"" +
                "}";
    }

    public MapJsonObject jsonMap() {
        MapJsonObject mapJsonObject = new MapJsonObject();
        mapJsonObject.setNativeElement("host", "localhost");
        mapJsonObject.setNativeElement("port", "5432");
        mapJsonObject.setNativeElement("databaseName","modality");
        mapJsonObject.setNativeElement("username","modality");
        mapJsonObject.setNativeElement("password","modality");
        return mapJsonObject;
    }

    @Test
    public void loadsPropertyFromEnvironment() {
        try(
                MockedStatic<DataSourceModelService> mockModelSvc = mockStatic(DataSourceModelService.class);
                MockedStatic<Resource> mockResourceSvc = mockStatic(Resource.class);
                MockedStatic<Json> mockJsonParser = mockStatic(Json.class)
        ) {
            mockModelSvc.when(DataSourceModelService::getDefaultDataSourceId).thenReturn("id");
            mockResourceSvc.when(() -> Resource
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
                MockedStatic<Resource> mockResourceSvc = mockStatic(Resource.class);
                MockedStatic<Json> mockJsonParser = mockStatic(Json.class);
        ) {
            mockModelSvc.when(DataSourceModelService::getDefaultDataSourceId)
                    .thenReturn("id");

            mockResourceSvc.when(() -> Resource
                    .getText("org/modality_project/base/server/datasource/id/ConnectionDetails.json"))
                    .thenReturn(connectionJson());

            mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                    .thenReturn(jsonMap());

            ModalityLocalDataSourceProvider dataSourceProvider = new ModalityLocalDataSourceProvider();
            assertEquals("localhost", dataSourceProvider.getProperty(jsonMap(), "host"));
        }
    }

}
