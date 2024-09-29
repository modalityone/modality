package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class MediaLinksForRecordingsManagement extends MediaLinksManagement {

    private final ObservableList<Media> workingMediasForCurrentLanguage = FXCollections.observableArrayList();

    public MediaLinksForRecordingsManagement(Item language, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase) {
        super(language.getCode(), entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        buildContainer();
    }

    private void buildContainer() {
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        VBox topContent = new VBox();

        Label languageLabel = I18nControls.bindI18nProperties(new Label(), "Language",currentItemCode);
        languageLabel.setPadding(new Insets(30,0,60,0));
        languageLabel.getStyleClass().add(Bootstrap.H4);
        languageLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        topContent.getChildren().add(languageLabel);

        Label publishAllLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.PublishAll,currentItemCode);
        Switch publishAllSwitch = new Switch();
        //We add the media to the update store
        workingMediasForCurrentLanguage.setAll(recordingsMediasReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));

        publishAllSwitch.selectedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(publishAllSwitch.isSelected()) {
                    workingMediasForCurrentLanguage.forEach(currentMedia->currentMedia.setPublished(true));
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button saveButton = Bootstrap.successButton(I18nControls.bindI18nProperties(new Button(), "Save"));
        saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
        HBox publishAllHBox = new HBox(publishAllLabel,publishAllSwitch,spacer,saveButton);
        publishAllHBox.setSpacing(10);
        publishAllHBox.setPadding(new Insets(0,0,30,0));
        publishAllHBox.setAlignment(Pos.CENTER_LEFT);
        topContent.getChildren().add(publishAllHBox);

        mainContainer.setTop(topContent);
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        ObservableLists.bindConverted(teachingDatesVBox.getChildren(),teachingsDates,this::computeTeachingDateLine);
    }
}
