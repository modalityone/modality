package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;

public class MediaLinksForRecordingsManagement extends MediaLinksManagement {

    public MediaLinksForRecordingsManagement(Item language, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase) {
        super(language, entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        Label languageLabel = I18nControls.bindI18nProperties(new Label(), "Language",language.getName());
        languageLabel.setPadding(new Insets(30,0,60,0));
        languageLabel.getStyleClass().add(Bootstrap.H4);
        languageLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        mainContainer.setTop(languageLabel);
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        ObservableLists.bindConverted(teachingDatesVBox.getChildren(),teachingsDates,this::computeTeachingDateLine);
    }
}
