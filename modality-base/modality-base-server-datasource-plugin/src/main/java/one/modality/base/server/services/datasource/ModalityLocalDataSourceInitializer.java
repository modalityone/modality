package one.modality.base.server.services.datasource;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.stack.db.datasource.ConnectionDetails;
import dev.webfx.stack.db.datasource.DBMS;
import dev.webfx.stack.db.datasource.spi.simple.SimpleLocalDataSource;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;

/**
 * @author Bruno Salmon
 */
public class ModalityLocalDataSourceInitializer implements ApplicationJob {

    private final static String CONFIG_PATH = "modality.base.server.datasource";

    @Override
    public void onInit() {
        // 1) Configuring the database connection
        ConfigLoader.onConfigLoaded(CONFIG_PATH, config -> {

            if (config == null) {
                log("❌ No configuration found for Modality datasource (check " + CONFIG_PATH + ")");
                return;
            }

            ConnectionDetails connectionDetails = new ConnectionDetails(
                    config.getString("host"),
                    config.getInteger("port", -1),
                    config.getString("filePath"),
                    config.getString("databaseName"),
                    config.getString("url"),
                    config.getString("username"),
                    config.getString("password")
            );

            Object dataSourceId = ModalityLocalDataSourceProvider.getModalityDataSourceId();
            DBMS dbms = DBMS.POSTGRES;
            ModalityLocalDataSourceProvider.setModalityDataSource(new SimpleLocalDataSource(dataSourceId, dbms, connectionDetails));

            // 2) Testing the database connection
            QueryService.executeQuery(new QueryArgumentBuilder()
                            .setDataSourceId(ModalityLocalDataSourceProvider.getModalityDataSourceId())
                            .setStatement( // language=SQL
                                "select count(*) from document")
                            .build())
                    .onSuccess(r -> log("✅ Successfully connected to Modality database"))
                    .onFailure(e -> log("❌ Failed to connect to Modality database"));
        });

    }
}
