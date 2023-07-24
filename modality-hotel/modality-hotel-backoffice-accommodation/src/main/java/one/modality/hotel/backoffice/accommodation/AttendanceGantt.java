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

  public AttendanceGantt(
      AccommodationPresentationModel pm,
      ObservableList<Attendance> attendancesToConvertToBars,
      ObservableList<ResourceConfiguration> providedParentRooms) {
    this(pm, attendancesToConvertToBars, null, null, providedParentRooms);
  }

  public AttendanceGantt(
      AccommodationPresentationModel pm,
      ObservableList<Attendance> attendancesToConvertToBars,
      ObservableList<LocalDateBar<AttendanceBlock>> convertedBars,
      ObservableList<LocalDateBar<AttendanceBlock>> barsLayoutChildren,
      ObservableList<ResourceConfiguration> providedParentRooms) {
    super(pm, barsLayoutChildren, providedParentRooms, 10);
    if (convertedBars == null) convertedBars = barsLayout.getChildren();
    TimeBarUtil.convertToBlocksThenGroupToBars(
        attendancesToConvertToBars, // the observable list of Attendance entities to take as input
        Attendance::getDate, // the entity date reader that will be used to date each block
        AttendanceBlock
            ::new, // the factory that creates blocks, initially 1 instance per entity, but then
                   // grouped into bars
        convertedBars); // the final list of bars that will receive the result of grouping blocks
    showBeds();
  }

  @Override
  protected void drawBar(LocalDateBar<AttendanceBlock> bar, Bounds b, GraphicsContext gc) {
    // The bar wraps a block over 1 or several days (or always 1 day if the user hasn't ticked the
    // grouping block
    // checkbox). So the bar instance is that block that was repeated over that period.
    barDrawer
        .setBackgroundFill(getBarColor(bar, b))
        .setMiddleText(bar.getInstance().getPersonName())
        // First draw the un-clipped text in a dark colour which contrasts with the background of
        // the chart
        .setClipText(false)
        .setTextFill(Color.GRAY)
        .drawBar(b, gc)
        // Second draw the clipped text in a light colour which contrasts with the background of the
        // bar
        .setClipText(true)
        .setTextFill(Color.WHITE)
        .drawTexts(b, gc);
  }

  protected Color getBarColor(LocalDateBar<AttendanceBlock> bar, Bounds b) {
    return bar.getInstance().getBlockColor();
  }
}
