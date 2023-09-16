package one.modality.base.frontoffice.application;

import dev.webfx.platform.conf.SourcesConfig;
import one.modality.base.client.application.ModalityClientStarterActivity;

/**
 * @author Bruno Salmon
 */
final class ModalityFrontOfficeStarterActivity extends ModalityClientStarterActivity {

    private static final String DEFAULT_START_PATH = SourcesConfig.getSourcesRootConfig()
            .childConfigAt("modality.base.frontoffice.application").getString("startPath");

    ModalityFrontOfficeStarterActivity() {
        super(DEFAULT_START_PATH, ModalityFrontOfficeMainFrameActivity::new);
    }

}
