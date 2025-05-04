package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.event.fx.FXEventId;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class MediaLinksForAudioRecordingsManagement extends MediaLinksManagement {

    private static final double IMAGE_SIZE = 272;

    private final ObservableList<Media> workingMediasForCurrentLanguage = FXCollections.observableArrayList();
    private final RecordingsTabView parentRecordingView;
    private final Item languageItem;
    private final String eventCoverCloudImagePath;
    private final MonoPane eventCoverImageContainer = new MonoPane();
    private final ProgressIndicator replaceProgressIndicator = Controls.createProgressIndicator(150);
    private final SVGPath trashImage = SvgIcons.createTrashSVGPath();

    public MediaLinksForAudioRecordingsManagement(Item languageItem, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> audioScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase, RecordingsTabView recordingsTabView) {
        super(languageItem.getCode(), entityStore, teachingsDates, audioScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        this.languageItem = languageItem;
        parentRecordingView = recordingsTabView;
        //Language code is 'audio-en", audio-fr", "audio-es", ...
        String languageCode = languageItem.getCode().split("-")[1];
        eventCoverCloudImagePath = ModalityCloudinary.eventCoverImagePath(FXEventId.getEventId(), languageCode);
        buildContainer();
    }

    private void buildContainer() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        VBox topContent = new VBox();

        Label languageLabel = I18nControls.newLabel(MediasI18nKeys.Language, languageItem.getName());
        languageLabel.setPadding(new Insets(30, 0, 60, 0));
        languageLabel.getStyleClass().add(Bootstrap.H4);
        languageLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        topContent.getChildren().add(languageLabel);

        Label publishAllLabel = I18nControls.newLabel(MediasI18nKeys.PublishAll, currentItemCode);
        Switch publishAllSwitch = new Switch();
        //We add the media to the update store
        workingMediasForCurrentLanguage.setAll(recordingsMediasReadFromDatabase.stream().map(localUpdateStore::updateEntity).collect(Collectors.toList()));

        publishAllSwitch.selectedProperty().addListener(observable -> {
            if (publishAllSwitch.isSelected()) {
                workingMediasForCurrentLanguage.forEach(currentMedia -> currentMedia.getScheduledItem().setPublished(true));
            }
        });

        //TODO: Here we add the possibility to upload a cover in cloudinary
        //We look if the image for this cover is existing in cloudinary

        SvgIcons.armButton(trashImage, () ->
            ModalityCloudinary.deleteImage(eventCoverCloudImagePath)
                .onFailure(Console::log)
                .onSuccess(e -> Platform.runLater(() -> {
                    eventCoverImageContainer.setContent(null);
                    trashImage.setVisible(false);
                }))
        );
        trashImage.setVisible(false);
        ShapeTheme.createSecondaryShapeFacet(trashImage).style();
        MonoPane trashImageMonoPane = new MonoPane(trashImage);
        trashImageMonoPane.setPadding(new Insets(0, 10, 10, 0));

        loadAudioCoverPicture();

        replaceProgressIndicator.setVisible(false);
        StackPane thumbailStackPane = new StackPane(eventCoverImageContainer, trashImageMonoPane, replaceProgressIndicator);
        thumbailStackPane.setMaxSize(IMAGE_SIZE, IMAGE_SIZE);
        thumbailStackPane.setPadding(new Insets(0, 0, 25, 0));
        StackPane.setAlignment(trashImageMonoPane, Pos.BOTTOM_RIGHT);
        eventCoverImageContainer.setMaxSize(IMAGE_SIZE, IMAGE_SIZE);

        topContent.getChildren().add(thumbailStackPane);

        FilePicker filePicker = FilePicker.create();
        filePicker.getAcceptedExtensions().addAll("image/*");
        Button uploadButton = Bootstrap.primaryButton(I18nControls.newButton(MediasI18nKeys.Upload));
        uploadButton.setMinWidth(200);
        filePicker.setGraphic(uploadButton);
        StackPane.setAlignment(filePicker.getGraphic(), Pos.CENTER_LEFT);
        ((Region) filePicker.getView()).setPadding(new Insets(0, 0, 40, 0));

        FXProperties.runOnPropertyChange(fileToUpload -> {
            replaceProgressIndicator.setVisible(true);
            ModalityCloudinary.replaceImage(eventCoverCloudImagePath, fileToUpload)
                .onComplete(ar -> {
                    if (ar.failed())
                        Platform.runLater(() -> replaceProgressIndicator.setVisible(false));
                    else
                        loadAudioCoverPicture();
                });
        }, filePicker.selectedFileProperty());

        topContent.getChildren().add(filePicker.getView());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(BaseI18nKeys.Save));
        saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(localUpdateStore).not());
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

    private void loadAudioCoverPicture() {
        ModalityCloudinary.loadImage(eventCoverCloudImagePath, eventCoverImageContainer, IMAGE_SIZE, IMAGE_SIZE, SvgIcons::createAudioCoverPath)
            .onComplete(ar -> {
                trashImage.setVisible(ar.succeeded());
                replaceProgressIndicator.setVisible(false);
            });
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
            parentRecordingView.addUpdateStoreHasChangesProperty(EntityBindings.hasChangesProperty(updateStore));
            return super.drawPanel();
        }
    }
}
