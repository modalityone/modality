package one.modality.all.frontoffice.application;

import dev.webfx.kit.statusbar.StatusBar;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.scene.paint.Color;
import one.modality.base.client.application.ModalityClientApplication;

/**
 * @author Bruno Salmon
 */
public class ModalityFrontOfficeApplication extends ModalityClientApplication {

    public ModalityFrontOfficeApplication() {
        super(new ModalityFrontOfficeStarterActivity());
        // Setting the status bar color to black on mobiles (this call needs to be in the UI thread)
        UiScheduler.runInUiThread(() -> StatusBar.setColor(Color.BLACK));
    }
}
