package one.modality.base.client.entities;

import dev.webfx.platform.boot.spi.ApplicationJob;
import one.modality.base.client.entities.functions.DateIntervalFormat;
import one.modality.base.client.entities.functions.I18nFunction;

/**
 * @author Bruno Salmon
 */
public class ClientEntitiesAdditionalRegisteringJob implements ApplicationJob {

    @Override
    public void onInit() {
        // Registering functions
        new I18nFunction().register();
        new DateIntervalFormat().register();
    }
}
