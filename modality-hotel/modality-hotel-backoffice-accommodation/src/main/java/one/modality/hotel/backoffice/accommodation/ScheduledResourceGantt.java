package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.time.layout.bar.TimeBarUtil;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;

/**
 * @author Bruno Salmon
 */
public abstract class ScheduledResourceGantt extends AccommodationGantt<ScheduledResourceBlock> {

    // The user has the option to enable/disable the blocks grouping (when disabled, TimeBarUtil
    // will not group the
    // blocks, but simply map each block to a 1-day-long bar, so the user will see all these blocks)
    private final BooleanProperty blocksGroupingProperty = new SimpleBooleanProperty();

    public ScheduledResourceGantt(
            AccommodationPresentationModel pm,
            ObservableList<ScheduledResource> scheduledResources,
            ObservableList<ResourceConfiguration> providedParentRooms) {
        super(pm, null, providedParentRooms, 13);
        TimeBarUtil.convertToBlocksThenGroupToBars(
                scheduledResources, // the observable list of ScheduledResource entities to take as
                                    // input
                ScheduledResource
                        ::getDate, // the entity date reader that will be used to date each block
                ScheduledResourceBlock
                        ::new, // the factory that creates blocks, initially 1 instance per entity,
                               // but then grouped into bars
                barsLayout
                        .getChildren(), // the final list of bars that will receive the result of
                                        // grouping blocks
                blocksGroupingProperty); // optional property to eventually disable the blocks
                                         // grouping (=> 1 bar per block if disabled)
        barsLayout.setChildFixedHeight(40);
        parentsCanvasDrawer.setHorizontalStroke(Color.BLACK).setVerticalStroke(Color.BLACK);
    }

    public BooleanProperty blocksGroupingProperty() {
        return blocksGroupingProperty;
    }
}
