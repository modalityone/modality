package one.modality.event.backoffice.events.ganttcanvas.spi.impl.event;

import javafx.scene.layout.Region;
import one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider;
import one.modality.event.backoffice.events.ganttcanvas.EventsGanttCanvas;

/**
 * @author Bruno Salmon
 */
public final class MainFrameEventsGanttCanvasProvider implements MainFrameGanttCanvasProvider {

    private final EventsGanttCanvas eventsGanttCanvas = new EventsGanttCanvas();
    @Override
    public Region getCanvasContainer() {
        return eventsGanttCanvas.getCanvasContainer();
    }

    @Override
    public void setupFXBindingsAndStartLogic(Object mixin) {
        eventsGanttCanvas.setupFXBindingsAndStartLogic(mixin);
    }
}
