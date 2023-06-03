package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HouseholdView {

    private final static Color OCCUPIED_COLOR = Color.rgb(130, 135, 136);
    private final static Color CLEANING_NEEDED_COLOR = Color.rgb(255, 3, 5);
    private final static Color CLEANED_DONE_COLOR = Color.rgb(65, 186, 77);

    private final AttendanceLoader attendanceLoader;
    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final AttendanceGantt attendanceGantt;
    private final ObservableList<LocalDateBar<AttendanceBlock>> attendeesBars = FXCollections.observableArrayList();
    private final ObservableList<LocalDateBar<AttendanceBlock>> attendeesAndCleaningBars = FXCollections.observableArrayList();

    public HouseholdView(AccommodationPresentationModel pm) {
        attendanceLoader = AttendanceLoader.getOrCreate(pm);
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        attendanceGantt = new AttendanceGantt(
                pm, // Presentation model
                attendanceLoader.getAttendances(), // attendances observable list that we provide as input to AttendanceGantt for bar conversion
                attendeesBars, // AttendanceGantt will put the result of the conversion (the attendees bars) into this observable list
                attendeesAndCleaningBars, // We will then add the cleaning bars into this observable list that will be the final input for the Gantt canvas
                resourceConfigurationLoader.getResourceConfigurations()) // the provided parent rooms
        {  // We also override getBarColor() to show checked-in attendees as gray
            {
                barsLayout.setSelectionEnabled(true);
                barsLayout.selectedChildProperty().addListener((observable, oldValue, bar) -> {
                    AttendanceBlock block = bar.getInstance();
                    if (block.isCheckedIn()) {
                        if (bar.getEndTime().isAfter(FXToday.getToday()))
                            return;
                        ResourceConfiguration roomConfiguration = block.getRoomConfiguration();
                        UpdateStore updateStore = UpdateStore.createAbove(roomConfiguration.getStore());
                        ResourceConfiguration rc = updateStore.updateEntity(roomConfiguration);

                        LocalDate lastCleaningDate = roomConfiguration.getLastCleaningDate();
                        boolean needsCleaning = (lastCleaningDate == null || lastCleaningDate.isBefore(bar.getEndTime()));
                        if (needsCleaning) {
                            rc.setLastCleaningDate(FXToday.getToday());
                        } else
                            rc.setLastCleaningDate(null);
                        updateStore.submitChanges();
                    }
                });
            }
            @Override
            protected Color getBarColor(LocalDateBar<AttendanceBlock> bar) {
                AttendanceBlock block = bar.getInstance();
                if (block.isCheckedIn()) {
                    if (bar.getEndTime().isAfter(FXToday.getToday()))
                        return OCCUPIED_COLOR;
                    LocalDate lastCleaningDate = block.getRoomConfiguration().getLastCleaningDate();
                    if (lastCleaningDate == null || lastCleaningDate.isBefore(bar.getEndTime()))
                        return CLEANING_NEEDED_COLOR;
                    return CLEANED_DONE_COLOR;
                }
                return super.getBarColor(bar);
            }
        };
        // Building attendees + cleaning bars from attendees bars
        ObservableLists.bindTransformed(attendeesAndCleaningBars, attendeesBars, this::addCleaningBars);
    }

    private List<LocalDateBar<AttendanceBlock>> addCleaningBars(List<LocalDateBar<AttendanceBlock>> attendeesBars) {
        // First, making a copy of these attendees bars, as they need to be in the resulting list too
        List<LocalDateBar<AttendanceBlock>> attendeesAndCleaningBars = new ArrayList<>(attendeesBars);
/*
        attendeesBars.stream()
                //.filter( b -> b.getInstance().isCheckedIn())
                .collect(Collectors.groupingBy(b -> b.getInstance().getRoomConfiguration()))
                .forEach((rc, bars) -> {
                    //bars.stream().sorted()
                });
*/
        return attendeesAndCleaningBars;
    }

    public AttendanceGantt getAttendanceGantt() {
        return attendanceGantt;
    }

    public void startLogic(Object mixin) {
        attendanceLoader.startLogic(mixin);
        resourceConfigurationLoader.startLogic(mixin);
    }
}
