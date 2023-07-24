package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.hotel.backoffice.accommodation.*;

public class GuestView {

  private final ResourceConfigurationLoader resourceConfigurationLoader;
  private final AttendanceLoader attendanceLoader;
  private final AttendanceGantt attendanceGantt;

  public GuestView(AccommodationPresentationModel pm) {
    resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
    attendanceLoader = AttendanceLoader.getOrCreate(pm);
    attendanceGantt =
        new AttendanceGantt(
            pm,
            attendanceLoader.getAttendances(),
            resourceConfigurationLoader.getResourceConfigurations());
  }

  public AttendanceGantt getAttendanceGantt() {
    return attendanceGantt;
  }

  public void startLogic(Object mixin) {
    resourceConfigurationLoader.startLogic(mixin);
    attendanceLoader.startLogic(mixin);
  }
}
