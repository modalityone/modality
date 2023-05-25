package one.modality.base.server.services.datasource;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;

/**
 * @author Bruno Salmon
 */
public class ModalityLocalDataSourceModuleBooter implements ApplicationModuleBooter {
    @Override
    public String getModuleName() {
        return "modality-base-server-datasource";
    }

    @Override
    public int getBootLevel() {
        return APPLICATION_BOOT_LEVEL;
    }

    @Override
    public void bootModule() {
        QueryService.executeQuery(new QueryArgumentBuilder()
                        .setDataSourceId(ModalityLocalDataSourceProvider.getModalityDataSourceId())
                        .setStatement("select count(*) from document")
                        .build())
                .onSuccess(r -> log("✅ Successfully connected to Modality database"))
                .onFailure(e -> log("❌ Failed to connect to Modality database"));
    }
}
