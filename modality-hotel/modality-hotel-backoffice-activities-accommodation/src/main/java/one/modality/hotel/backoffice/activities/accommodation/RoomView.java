package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.geometry.Bounds;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.accommodation.*;

/**
 * @author Bruno Salmon
 */
public final class RoomView {

    // Style constants used for drawing bars in the canvas:
    private final static Color BAR_AVAILABLE_ONLINE_COLOR = Color.rgb(65, 186, 77);
    private final static Color BAR_AVAILABLE_OFFLINE_COLOR = Color.ORANGE;
    private final static Color BAR_SOLDOUT_COLOR = Color.rgb(255, 3, 5);
    private final static Color BAR_UNAVAILABLE_COLOR = Color.rgb(130, 135, 136);

    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final ScheduledResourceLoader scheduledResourceLoader;
    private final ScheduledResourceGantt scheduledResourceGantt;

    public RoomView(AccommodationPresentationModel pm) {
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        scheduledResourceLoader = ScheduledResourceLoader.getOrCreate(pm);
        scheduledResourceGantt = new ScheduledResourceGantt(pm, scheduledResourceLoader.getScheduledResources(), resourceConfigurationLoader.getResourceConfigurations()) {
            @Override
            protected void drawBlock(ScheduledResourceBlock block, Bounds b, GraphicsContext gc) {
                // The main info we display in the bar is a number which represents how many free beds are remaining for booking
                String remaining = String.valueOf(block.getRemaining());
                // If the bar is wide enough we show "Beds" on top and the number on bottom, but if it is too narrow, we just
                // display the number in the middle. Unavailable gray bars have no text at all by the way.
                boolean isWideBar = b.getWidth() > 40;
                barDrawer
                        .setTopText(   isWideBar && block.isAvailable() ?   "Beds"   :   null    )
                        .setMiddleText(isWideBar || !block.isAvailable() ?   null    : remaining )
                        .setBottomText(isWideBar && block.isAvailable() ?  remaining :   null    )
                        .setBackgroundFill(
                                !block.isAvailable() ?      BAR_UNAVAILABLE_COLOR :       // gray if unavailable
                                block.getRemaining() <= 0 ? BAR_SOLDOUT_COLOR :           // red if sold-out
                                block.isOnline() ?          BAR_AVAILABLE_ONLINE_COLOR :  // green if online
                                                            BAR_AVAILABLE_OFFLINE_COLOR)  // orange if offline
                        .drawBar(b, gc);
            }
        };
    }

    public Node buildCanvasContainer() {
        return scheduledResourceGantt.buildCanvasContainer();
    }

    public BooleanProperty blocksGroupingProperty() {
        return scheduledResourceGantt.blocksGroupingProperty();
    }


    public void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
        scheduledResourceLoader.startLogic(mixin);
    }

}
