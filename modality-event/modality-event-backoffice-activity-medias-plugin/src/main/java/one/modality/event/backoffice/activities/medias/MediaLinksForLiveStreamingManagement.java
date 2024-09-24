package one.modality.event.backoffice.activities.medias;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;

public class MediaLinksForLiveStreamingManagement extends MediaLinksManagement {
    public MediaLinksForLiveStreamingManagement(EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase) {
        //TODO: what is the linkedItem for those scheduledItem
        super(null, entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        ObservableLists.bindConverted(teachingDatesVBox.getChildren(),teachingsDates,this::computeTeachingDateLine);
    }
}
