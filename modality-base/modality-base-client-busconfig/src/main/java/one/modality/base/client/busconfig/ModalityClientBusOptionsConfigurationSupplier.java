package one.modality.base.client.busconfig;

import dev.webfx.stack.conf.spi.impl.resource.SingleResourceConfigurationSupplier;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientBusOptionsConfigurationSupplier
    extends SingleResourceConfigurationSupplier {

  private static final String BUS_OPTIONS_CONFIGURATION_NAME = "ClientBusOptions";
  private static final String BUS_OPTIONS_RESOURCE_FILE_NAME = "ClientBusOptions.default.json";

  public ModalityClientBusOptionsConfigurationSupplier() {
    super(BUS_OPTIONS_CONFIGURATION_NAME, BUS_OPTIONS_RESOURCE_FILE_NAME);
  }
}
