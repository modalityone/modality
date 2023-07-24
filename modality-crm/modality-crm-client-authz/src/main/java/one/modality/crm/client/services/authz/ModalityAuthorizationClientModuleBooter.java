package one.modality.crm.client.services.authz;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.stack.authz.client.AuthorizationClientService;
import dev.webfx.stack.com.bus.call.BusCallService;

/**
 * @author Bruno Salmon
 */
public class ModalityAuthorizationClientModuleBooter implements ApplicationModuleBooter {

  // To be shared by the server counterpart.
  private static final String CLIENT_AUTHZ_SERVICE_ADDRESS = "modality/service/authz";

  @Override
  public String getModuleName() {
    return "modality-crm-client-authz";
  }

  @Override
  public int getBootLevel() {
    return COMMUNICATION_REGISTER_BOOT_LEVEL;
  }

  @Override
  public void bootModule() {
    BusCallService.registerBusCallEndpoint(
        CLIENT_AUTHZ_SERVICE_ADDRESS,
        AuthorizationClientService.getProvider()::onAuthorizationsPush);
  }
}
