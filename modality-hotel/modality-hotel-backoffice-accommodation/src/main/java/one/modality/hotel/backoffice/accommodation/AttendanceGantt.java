package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.bar.TimeBarUtil;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ResourceConfiguration;

/**
 * @author Bruno Salmon
 */
public class AttendanceGantt extends AccommodationGantt<AttendanceBlock> {

    public AttendanceGantt(AccommodationPresentationModel pm, ObservableList<Attendance> attendances, ObservableList<ResourceConfiguration> providedParentRooms) {
        this(pm, attendances, null, providedParentRooms);
    }

    public AttendanceGantt(AccommodationPresentationModel pm, ObservableList<Attendance> attendances, ObservableList<LocalDateBar<AttendanceBlock>> bars, ObservableList<ResourceConfiguration> providedParentRooms) {
        super(pm, bars, providedParentRooms, 10);
        if (bars == null)
            bars = barsLayout.getChildren();
        TimeBarUtil.convertToBlocksThenGroupToBars(
                attendances, // the observable list of Attendance entities to take as input
                Attendance::getDate, // the entity date reader that will be used to date each block
                AttendanceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                bars); // the final list of bars that will receive the result of grouping blocks
        showBeds();
    }

    @Override
    protected void drawBlock(AttendanceBlock block, Bounds b, GraphicsContext gc) {
        barDrawer
                .setBackgroundFill(getBarColor(block))
                .setMiddleText(block.getPersonName())
                // First draw the un-clipped text in a dark colour which contrasts with the background of the chart
                .setClipText(false)
                .setTextFill(Color.GRAY)
                .drawBar(b, gc)
                // Second draw the clipped text in a light colour which contrasts with the background of the bar
                .setClipText(true)
                .setTextFill(Color.WHITE)
                .drawTexts(b, gc);
    }

    protected Color getBarColor(AttendanceBlock block) {
        return block.getBlockColor();
    }

}
