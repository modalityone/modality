package one.modality.hotel.backoffice.activities.household;

import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.accommodation.AccommodationGanttCanvas;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;
import one.modality.hotel.backoffice.accommodation.AttendanceBlock;

public class HouseholdView extends AccommodationGanttCanvas {

    public HouseholdView(AccommodationPresentationModel pm, AccommodationStatusBarUpdater controller) {
        super(pm, controller);
    }

    protected Color getBarColor(AttendanceBlock block) {
        if (block.isCheckedIn()) {
            return Color.GRAY;
        } else {
            return super.getBarColor(block);
        }
    }

}
