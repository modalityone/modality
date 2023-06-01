package one.modality.hotel.backoffice.accommodation;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class TodayAccommodationStatus {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    private final TodayScheduledResourceLoader todayScheduledResourceLoader;
    private final TodayAttendanceLoader todayAttendanceLoader;

    public TodayAccommodationStatus(AccommodationPresentationModel pm) {
        todayScheduledResourceLoader = TodayScheduledResourceLoader.getOrCreate(pm);
        todayAttendanceLoader = new TodayAttendanceLoader(pm);
    }

    public void startLogic(Object mixin) {
        todayScheduledResourceLoader.startLogic(mixin);
        todayAttendanceLoader.startLogic(mixin);
    }

    private ObservableList<Attendance> todayAttendances() {
        return todayAttendanceLoader.getTodayAttendances();
    }

    private ObservableList<ScheduledResource> todayScheduledResources() {
        return todayScheduledResourceLoader.getTodayScheduledResources();
    }

    private long countRoomsOccupied() {
        return todayAttendances().stream()
                .map(Attendance::getResourceConfiguration)
                .distinct()
                .count();
    }

    private long countAllRooms() {
        return todayScheduledResources().size();
    }

    private int countAllBeds() {
        return todayScheduledResources().stream()
                .mapToInt(ScheduledResource::getMax)
                .sum();
    }

    private long countBedsAvailable() {
        // The "booked" field is an extra computed fields added by the ReactiveEntitiesMapper in AccommodationGanttCanvas
        return todayScheduledResources().stream()
                .mapToInt(scheduledResource -> scheduledResource.getMax() - scheduledResource.getIntegerFieldValue("booked"))
                .sum();
    }

    private long countGuests() {
        return todayAttendances().size();
    }

    public GridPane createStatusBar() {
        GridPane statusBar = new GridPane();
        FXProperties.runNowAndOnPropertiesChange(() -> updateStatusBar(statusBar), FXToday.todayProperty());
        ObservableLists.runOnListChange(c -> updateStatusBar(statusBar), todayAttendances());
        ObservableLists.runOnListChange(c -> updateStatusBar(statusBar), todayScheduledResources());
        return statusBar;
    }

    private void updateStatusBar(GridPane statusBar) {
        statusBar.setAlignment(Pos.CENTER); // Makes a difference for the Web version (otherwise children appears on top)
        long numRoomsOccupied = countRoomsOccupied();
        long numRoomsAvailable = countAllRooms() - numRoomsOccupied;
        int numBeds = countAllBeds();
        long numBedsAvailable = countBedsAvailable();
        long numBedsOccupied = numBeds - numBedsAvailable;
        long numGuests = countGuests();

        statusBar.getChildren().clear();
        statusBar.add(buildLabel("Status today " + formatToday()), 0, 0);
        statusBar.add(buildLabel("Rooms occupied: " + numRoomsOccupied), 1, 0);
        statusBar.add(buildLabel("Rooms available: " + numRoomsAvailable), 2, 0);
        statusBar.add(buildLabel("Beds occupied: " + numBedsOccupied), 3, 0);
        statusBar.add(buildLabel("Beds available: " + numBedsAvailable), 4, 0);
        statusBar.add(buildLabel("Guests: " + numGuests), 5, 0);

        updateColumnWidths(statusBar);
    }

    private static Label buildLabel(String text) {
        Label label = new Label(text);
        GridPane.setHalignment(label, HPos.CENTER);
        return label;
    }

    private static String formatToday() {
        return DATE_FORMATTER.format(FXToday.getToday());
    }

    private static void updateColumnWidths(GridPane statusBar) {
        int numColumns = statusBar.getColumnCount();
        double columnPercentageWidth = 100.0 / numColumns;
        double percentageRemaining = 100.0;
        List<ColumnConstraints> columnConstraints = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns - 1; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(columnPercentageWidth);
            columnConstraints.add(column);
            percentageRemaining -= columnPercentageWidth;
        }
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentageRemaining);
        columnConstraints.add(column);
        statusBar.getColumnConstraints().setAll(columnConstraints);
    }

    private static TodayAccommodationStatus INSTANCE;

    public static TodayAccommodationStatus getOrCreate(AccommodationPresentationModel pm) {
        if (INSTANCE == null)
            INSTANCE = new TodayAccommodationStatus(pm);
        return INSTANCE;
    }

}
