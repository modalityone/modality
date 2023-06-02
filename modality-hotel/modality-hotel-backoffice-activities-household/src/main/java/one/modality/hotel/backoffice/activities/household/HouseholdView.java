package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.time.layout.bar.LocalDateBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.accommodation.*;

public class HouseholdView {

    private final AttendanceLoader attendanceLoader;
    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final AttendanceGantt attendanceGantt;
    private final ObservableList<LocalDateBar<AttendanceBlock>> bars = FXCollections.observableArrayList();

    public HouseholdView(AccommodationPresentationModel pm) {
        attendanceLoader = AttendanceLoader.getOrCreate(pm);
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        attendanceGantt = new AttendanceGantt(pm, attendanceLoader.getAttendances(), bars, resourceConfigurationLoader.getResourceConfigurations()) {
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
        attendanceLoader.startLogic(mixin);
        resourceConfigurationLoader.startLogic(mixin);
    }
}
