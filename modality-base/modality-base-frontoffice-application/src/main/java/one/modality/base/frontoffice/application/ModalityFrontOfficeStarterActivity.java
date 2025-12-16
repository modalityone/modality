package one.modality.base.frontoffice.application;

import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.routing.uirouter.UiRouter;
import one.modality.base.client.application.ModalityClientStarterActivity;
import one.modality.base.frontoffice.activities.mainframe.ModalityFrontOfficeMainFrameActivity;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;

/**
 * @author Bruno Salmon
 */
final class ModalityFrontOfficeStarterActivity extends ModalityClientStarterActivity {

    private static final String DEFAULT_START_PATH = SourcesConfig.getSourcesRootConfig()
            .childConfigAt("modality.base.frontoffice.application").getString("startPath");

    ModalityFrontOfficeStarterActivity() {
        super(DEFAULT_START_PATH, ModalityFrontOfficeMainFrameActivity::new);
    }

    @Override
    protected UiRouter setupContainedRouter(UiRouter containedRouter) {
        BrowserUtil.setUiRouter(containedRouter);
        return super.setupContainedRouter(containedRouter);
    }
}
