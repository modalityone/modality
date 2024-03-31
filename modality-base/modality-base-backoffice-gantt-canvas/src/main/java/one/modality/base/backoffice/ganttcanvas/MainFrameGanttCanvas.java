package one.modality.base.backoffice.ganttcanvas;

import dev.webfx.platform.service.SingleServiceProvider;
import javafx.scene.layout.Region;
import one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class MainFrameGanttCanvas {

    public static MainFrameGanttCanvasProvider getProvider() {
        return SingleServiceProvider.getProvider(MainFrameGanttCanvasProvider.class, () -> ServiceLoader.load(MainFrameGanttCanvasProvider.class));
    }

    public static Region getCanvasContainer() {
        return getProvider().getCanvasContainer();
    }

    public static void setupFXBindingsAndStartLogic(Object mixin) {
        getProvider().setupFXBindingsAndStartLogic(mixin);
    }

}
