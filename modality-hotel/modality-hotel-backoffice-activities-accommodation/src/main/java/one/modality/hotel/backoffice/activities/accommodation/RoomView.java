package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.TimeBarUtil;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.ScheduledResource;
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
    private final AccommodationGantt<ScheduledResourceBlock> accommodationGantt;

    // The user has the option to enable/disable the blocks grouping (when disabled, TimeBarUtil will not group the
    // blocks, but simply map each block to a 1-day-long bar, so the user will see all these blocks)
    final BooleanProperty blocksGroupingProperty = new SimpleBooleanProperty();


    public RoomView(AccommodationPresentationModel pm) {
        resourceConfigurationLoader = new ResourceConfigurationLoader(pm);
        scheduledResourceLoader = ScheduledResourceLoader.getOrCreate(pm);
        accommodationGantt = new AccommodationGantt<>(pm, 13) {
            { // Finishing the setup
                TimeBarUtil.convertToBlocksThenGroupToBars(
                        scheduledResourceLoader.getScheduledResources(), // the observable list of ScheduledResource entities to take as input
                        ScheduledResource::getDate, // the entity date reader that will be used to date each block
                        ScheduledResourceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                        barsLayout.getChildren(), // the final list of bars that will receive the result of grouping blocks
                        blocksGroupingProperty); // optional property to eventually disable the blocks grouping (=> 1 bar per block if disabled)
                barsLayout
                        .setChildFixedHeight(40)
                        .setParentsProvided(true);
                ObservableLists.bind(barsLayout.getParents(), resourceConfigurationLoader.getResourceConfigurations());
                parentsCanvasDrawer
                        .setHorizontalStroke(Color.BLACK)
                        .setVerticalStroke(Color.BLACK);
            }

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
        return accommodationGantt.buildCanvasContainer();
    }

    public void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
        scheduledResourceLoader.startLogic(mixin);
    }

}
