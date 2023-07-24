package one.modality.base.server.services.datasource;

public class TestModalityLocalDataSourceProvider {

    /*   public String connectionJson() {
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
                    //MockedStatic<DataSourceModelService> mockModelSvc = mockStatic(DataSourceModelService.class);
                    MockedStatic<Resource> mockResourceSvc = mockStatic(Resource.class);
                    MockedStatic<Json> mockJsonParser = mockStatic(Json.class)
            ) {
                //mockModelSvc.when(DataSourceModelService::getDefaultDataSourceId).thenReturn("id");
                mockResourceSvc.when(() -> Resource
                        .getText("one/modality/base/server/services/datasource/DefaultModalityDatabase.json"))
                        .thenReturn(connectionJson());

                mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                        .thenReturn(jsonMap());

                //System.setProperty("host", "database");

                assertEquals("localhost", jsonMap().getString("host"));

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
                                .getText("one/modality/base/server/services/datasource/DefaultModalityDatabase.json"))
                        .thenReturn(connectionJson());

                mockJsonParser.when(() -> Json.parseObject(connectionJson()))
                        .thenReturn(jsonMap());

                assertEquals("localhost", jsonMap().getString("host"));
            }
        }
    */
}
