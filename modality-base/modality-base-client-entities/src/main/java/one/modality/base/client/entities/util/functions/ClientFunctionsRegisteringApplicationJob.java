package one.modality.base.client.entities.util.functions;

import dev.webfx.platform.boot.spi.ApplicationJob;

/**
 * @author Bruno Salmon
 */
public class ClientFunctionsRegisteringApplicationJob implements ApplicationJob {

    @Override
    public void onInit() {
        new I18nFunction().register();
        new DateIntervalFormat().register();
    }
}
