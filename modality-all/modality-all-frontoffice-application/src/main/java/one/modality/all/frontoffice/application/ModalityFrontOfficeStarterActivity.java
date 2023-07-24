package one.modality.all.frontoffice.application;

import one.modality.base.client.application.ModalityClientStarterActivity;

/**
 * @author Bruno Salmon
 */
final class ModalityFrontOfficeStarterActivity extends ModalityClientStarterActivity {

  private static final String DEFAULT_START_PATH = "/book/event/357/start";

  ModalityFrontOfficeStarterActivity() {
    super(DEFAULT_START_PATH);
  }
}
