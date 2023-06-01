package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.TimeBarUtil;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.*;

import java.util.List;
import java.util.stream.Collectors;

public class GuestView {

    private final ScheduledResourceLoader scheduledResourceLoader;
    private final AttendanceLoader attendanceLoader;
    private final AccommodationGantt<AttendanceBlock> accommodationGantt;

    public GuestView(AccommodationPresentationModel pm, AccommodationStatusBarUpdater statusBarUpdater) {
        scheduledResourceLoader = ScheduledResourceLoader.getOrCreate(pm);
        attendanceLoader = new AttendanceLoader(pm);
        accommodationGantt = new AccommodationGantt<>(pm, 10) {
            {
                TimeBarUtil.convertToBlocksThenGroupToBars(
                        attendanceLoader.getAttendances(), // the observable list of Attendance entities to take as input
                        Attendance::getDate, // the entity date reader that will be used to date each block
                        AttendanceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                        barsLayout.getChildren()); // the final list of bars that will receive the result of grouping blocks

                // Updating the legend with new colours when the entities change
                attendanceLoader.getAttendances().addListener((ListChangeListener<Attendance>) change -> statusBarUpdater.setEntities(attendanceLoader.getAttendances()));

                // Updating the status bar when scheduled resources change
                scheduledResourceLoader.getScheduledResources().addListener((ListChangeListener<ScheduledResource>) change -> {
                    statusBarUpdater.setAllScheduledResource(scheduledResourceLoader.getScheduledResources());
                    List<ResourceConfiguration> parents = scheduledResourceLoader.getScheduledResources().stream()
                            .map(ScheduledResource::getResourceConfiguration)
                            .distinct()
                            .collect(Collectors.toList());
                    barsLayout.getParents().setAll(parents);
                });

                showBeds();
            }

            @Override
            protected void drawBlock(AttendanceBlock block, Bounds b, GraphicsContext gc) {
                barDrawer
                        .setBackgroundFill(block.getBlockColor())
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
        };
    }

    public AccommodationGantt<AttendanceBlock> getAccommodationGantt() {
        return accommodationGantt;
    }

    public void startLogic(Object mixin) {
        scheduledResourceLoader.startLogic(mixin);
        attendanceLoader.startLogic(mixin);
    }
}
