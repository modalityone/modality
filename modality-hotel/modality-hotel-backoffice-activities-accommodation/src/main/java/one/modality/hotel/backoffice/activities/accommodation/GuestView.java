package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.hotel.backoffice.accommodation.*;

public class GuestView {

    private final ScheduledResourceLoader scheduledResourceLoader;
    private final AttendanceLoader attendanceLoader;
    private final AttendanceGantt attendanceGantt;

    public GuestView(AccommodationPresentationModel pm) {
        scheduledResourceLoader = ScheduledResourceLoader.getOrCreate(pm);
        attendanceLoader = new AttendanceLoader(pm);
        attendanceGantt = new AttendanceGantt(pm, attendanceLoader.getAttendances());
    }

    public AttendanceGantt getAttendanceGantt() {
        return attendanceGantt;
    }

    public void startLogic(Object mixin) {
        scheduledResourceLoader.startLogic(mixin);
        attendanceLoader.startLogic(mixin);
    }
}
