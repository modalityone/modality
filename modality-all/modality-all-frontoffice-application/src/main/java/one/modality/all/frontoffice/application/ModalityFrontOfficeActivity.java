package one.modality.all.frontoffice.application;

import one.modality.base.client.application.ModalityClientActivity;

/**
 * @author Bruno Salmon
 */
final class ModalityFrontOfficeActivity extends ModalityClientActivity {

    private static final String DEFAULT_START_PATH = "/book/event/357/start";

    ModalityFrontOfficeActivity() {
        super(DEFAULT_START_PATH);
    }

}
