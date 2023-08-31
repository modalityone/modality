package one.modality.base.backoffice.application;

import one.modality.base.client.application.ModalityClientStarterActivity;

/**
 * @author Bruno Salmon
 */
final class ModalityBackOfficeStarterActivity extends ModalityClientStarterActivity {

    private static final String DEFAULT_START_PATH = "/home";

    ModalityBackOfficeStarterActivity() {
        super(DEFAULT_START_PATH, ModalityBackOfficeMainFrameContainerActivity::new);
    }

}
