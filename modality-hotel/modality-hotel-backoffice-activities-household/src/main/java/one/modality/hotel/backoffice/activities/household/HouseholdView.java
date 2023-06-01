package one.modality.hotel.backoffice.activities.household;

import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.accommodation.*;

public class HouseholdView {

    private final ScheduledResourceLoader scheduledResourceLoader;
    private final AttendanceLoader attendanceLoader;
    private final AttendanceGantt attendanceGantt;

    public HouseholdView(AccommodationPresentationModel pm) {
        scheduledResourceLoader = ScheduledResourceLoader.getOrCreate(pm);
        attendanceLoader = new AttendanceLoader(pm);
        attendanceGantt = new AttendanceGantt(pm, attendanceLoader.getAttendances()) {
            protected Color getBarColor(AttendanceBlock block) {
                if (block.isCheckedIn())
                    return Color.GRAY;
                return super.getBarColor(block);
            }
        };
    }

    public AttendanceGantt getAttendanceGantt() {
        return attendanceGantt;
    }

    public void startLogic(Object mixin) {
        scheduledResourceLoader.startLogic(mixin);
        attendanceLoader.startLogic(mixin);
    }
}
