package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class MediaLinksForAudioRecordingsManagement extends MediaLinksManagement {

    private final ObservableList<Media> workingMediasForCurrentLanguage = FXCollections.observableArrayList();
    private final RecordingsView parentRecordingView;
    private final Item languageItem;
    private ImageView imageView;
    private final ModalityCloudinary modalityCloudinary = new ModalityCloudinary(ModalityCloudinary.CloudinaryPrefix.AUDIO_COVER);

    public MediaLinksForAudioRecordingsManagement(Item language, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase, RecordingsView recordingsView) {
        super(language.getCode(), entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        languageItem = language;
        parentRecordingView = recordingsView;
        //Language code is 'audio-en", audio-fr", "audio-es", ...
        String languageCode= language.getCode().split("-")[1];
        modalityCloudinary.setLanguage(languageCode);
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

        int eventId;
        eventId = Integer.parseInt(FXEvent.getEvent().getPrimaryKey().toString());

        //TODO: Here we add the possibility to upload a cover in cloudinary
        //We look if the image for this cover is existing in cloudinary
        int IMAGE_SIZE = 272;
        StackPane thumbailStackPane = new StackPane();
        thumbailStackPane.setMaxHeight(IMAGE_SIZE);
        thumbailStackPane.setMaxWidth(IMAGE_SIZE);
        thumbailStackPane.setPadding(new Insets(0,0,25,0));

        SVGPath audioCoverPath = SvgIcons.createAudioCoverPath();
        MonoPane audioCoverPictureMonoPane = new MonoPane(audioCoverPath);
        audioCoverPictureMonoPane.setBackground(new Background(
            new BackgroundFill(Color.LIGHTGRAY, null, null)
        ));

        thumbailStackPane.getChildren().add(audioCoverPictureMonoPane);
        audioCoverPictureMonoPane.setMinWidth(IMAGE_SIZE);
        audioCoverPictureMonoPane.setMinHeight(IMAGE_SIZE);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setImage(null);
        thumbailStackPane.getChildren().add(imageView);


        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(150,150);
        thumbailStackPane.getChildren().add(indicator);
        StackPane.setAlignment(indicator,Pos.CENTER);
        indicator.setVisible(false);

        var ref = new Object() {
            SVGPath trashImage = SvgIcons.createTrashSVGPath();
        };
        ref.trashImage = SvgIcons.armButton(SvgIcons.createTrashSVGPath(), () -> modalityCloudinary.deleteCloudPicture(eventId)
            .onFailure(Console::log)
            .onSuccess(e-> Platform.runLater(()-> {
                imageView.setImage(null);
                ref.trashImage.setVisible(false);
            })));
        ref.trashImage.setVisible(false);
        ShapeTheme.createSecondaryShapeFacet(ref.trashImage).style();
        MonoPane trashImageMonoPane = new MonoPane(ref.trashImage);
        thumbailStackPane.getChildren().add(trashImageMonoPane);
        trashImageMonoPane.setPadding(new Insets(0,10,10,0));
        StackPane.setAlignment(trashImageMonoPane,Pos.BOTTOM_RIGHT);

        double zoomFactor = Screen.getPrimary().getOutputScaleX();
        modalityCloudinary.doesCloudPictureExist(eventId)
            .onFailure(Console::log)
            .onSuccess(exist-> {
                if(exist) {
                    Image image = modalityCloudinary.getImage(eventId, (int) (imageView.getFitWidth() * zoomFactor), -1);
                    imageView.setImage(image);
                    ref.trashImage.setVisible(true);
                    indicator.setVisible(false);
                }
                else {
                    ref.trashImage.setVisible(false);
                    indicator.setVisible(false);
                }
            });

        topContent.getChildren().add(thumbailStackPane);

        FilePicker filePicker = FilePicker.create();
        Button uploadButton = Bootstrap.primaryButton(I18nControls.newButton(MediasI18nKeys.Upload));
        uploadButton.setMinWidth(200);
        filePicker.setGraphic(uploadButton);
        StackPane.setAlignment(filePicker.getGraphic(),Pos.CENTER_LEFT);
        ((StackPane) filePicker.getView()).setPadding(new Insets(0,0,40,0));

        filePicker.getSelectedFiles().addListener((InvalidationListener) obs -> {
            ObservableList<File> fileList = filePicker.getSelectedFiles();
            indicator.setVisible(true);
            File fileToUpload = fileList.get(0);
            modalityCloudinary.deleteCloudPicture(eventId)
                .onFailure(e-> {
                    Console.log(e);
                    //We wait for 1 second (if we don't wait, the picture doesn't change below, probably because
                    //cloudinary server didn't have enough time to delete the old/proceed the old and new picture
                    modalityCloudinary.uploadCloudPicture(eventId,fileToUpload)
                        .onFailure(ev->{
                            Console.log(ev);
                            Platform.runLater(()->indicator.setVisible(false));
                        })
                        .onSuccess(ev-> Platform.runLater(() -> {
                            ref.trashImage.setVisible(true);
                            Image image = modalityCloudinary.getImage(eventId, (int) (imageView.getFitWidth() * zoomFactor), -1);
                            imageView.setImage(image);
                            indicator.setVisible(false);
                        }));
                })
                    .onSuccess(e-> modalityCloudinary.uploadCloudPicture(eventId,fileToUpload)
                        .onFailure(ev->{
                            Console.log(ev);
                            Platform.runLater(()->indicator.setVisible(false));
                        })
                    .onSuccess(evt-> Platform.runLater(() -> {
                        //We wait for 2 second (if we don't wait, the picture doesn't change below, probably because
                        //cloudinary server didn't have enough time to delete the old/proceed the old and new picture
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        ref.trashImage.setVisible(true);
                        Image image = modalityCloudinary.getImage(eventId, (int) (imageView.getFitWidth() * zoomFactor), -1);
                        imageView.setImage(image);
                        indicator.setVisible(false);
                    })));

        });

        topContent.getChildren().add(filePicker.getView());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(ModalityI18nKeys.Save));
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
