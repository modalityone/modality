package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class MediaLinksForAudioRecordingsManagement extends MediaLinksManagement {

    private final ObservableList<Media> workingMediasForCurrentLanguage = FXCollections.observableArrayList();
    private final RecordingsView parentRecordingView;
    private final Item languageItem;

    public MediaLinksForAudioRecordingsManagement(Item language, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase, RecordingsView recordingsView) {
        super(language.getCode(), entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        languageItem = language;
        parentRecordingView = recordingsView;
        buildContainer();
    }

    private void buildContainer() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        VBox topContent = new VBox();

        Label languageLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.Language, languageItem.getName());
        languageLabel.setPadding(new Insets(30, 0, 60, 0));
        languageLabel.getStyleClass().add(Bootstrap.H4);
        languageLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        topContent.getChildren().add(languageLabel);

        Label publishAllLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.PublishAll, currentItemCode);
        Switch publishAllSwitch = new Switch();
        //We add the media to the update store
        workingMediasForCurrentLanguage.setAll(recordingsMediasReadFromDatabase.stream().map(localUpdateStore::updateEntity).collect(Collectors.toList()));

        publishAllSwitch.selectedProperty().addListener(observable -> {
            if (publishAllSwitch.isSelected()) {
                workingMediasForCurrentLanguage.forEach(currentMedia -> currentMedia.setPublished(true));
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button saveButton = Bootstrap.successButton(I18nControls.bindI18nProperties(new Button(), ModalityI18nKeys.Save));
        saveButton.disableProperty().bind(localUpdateStore.hasChangesProperty().not());
        HBox publishAllHBox = new HBox(publishAllLabel, publishAllSwitch, spacer, saveButton);
        publishAllHBox.setSpacing(10);
        publishAllHBox.setPadding(new Insets(0, 0, 30, 0));
        publishAllHBox.setAlignment(Pos.CENTER_LEFT);
        //TODO: implement the publish all functionality
        //  topContent.getChildren().add(publishAllHBox);

        mainContainer.setTop(topContent);
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        teachingsDates.forEach(date -> teachingDatesVBox.getChildren().add(computeTeachingDateLine(date)));
    }

    protected BorderPane computeTeachingDateLine(LocalDate date) {
        MediaLinksPerDateManagement mediaLinksPerDateManagement = new MediaLinksForAudioRecordingPerDateManagement(date);
        return mediaLinksPerDateManagement.drawPanel();
    }

    protected class MediaLinksForAudioRecordingPerDateManagement extends MediaLinksPerDateManagement {

        protected MediaLinksForAudioRecordingPerDateManagement(LocalDate date) {
            super(date);
        }

        protected BorderPane drawPanel() {
            parentRecordingView.addUpdateStoreHasChangesProperty(updateStore.hasChangesProperty());
            return super.drawPanel();
        }
    }
}
