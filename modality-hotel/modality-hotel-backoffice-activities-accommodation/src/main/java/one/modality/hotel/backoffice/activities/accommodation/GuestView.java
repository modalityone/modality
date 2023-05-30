package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.hotel.backoffice.accommodation.AccommodationGanttCanvas;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;

public class GuestView extends AccommodationGanttCanvas {

    public GuestView(AccommodationPresentationModel pm, AccommodationStatusBarUpdater controller) {
        super(pm, controller);
    }
}
