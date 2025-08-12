package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.blob.Blob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;

import java.util.Objects;

/**
 * @author David Hello
 */
final class ChangePictureUI {

    static final long CLOUDINARY_RELOAD_DELAY = 10000;
    private static final int MAX_PICTURE_SIZE = 240;

    private final VBox container = new VBox();
    private final Slider zoomSlider;
    private final ImageView imageView;
    private double deltaX = 0;
    private double deltaY = 0;
    private double zoomFactor = 1;
    private Blob cloudPictureFileToUpload;
    private Object recentlyUploadedCloudPictureId;
    private final UserProfileActivity parentActivity;
    private DialogCallback callback;
    //Those two boolean property are used to know if we have to delete the picture, upload a new one, or if we are currently processing
    // (because we do the processing when we press the confirm button and not when we upload the picture)
    private final BooleanProperty isPictureToBeDeleted = new SimpleBooleanProperty(false);
    private final BooleanProperty isPictureToBeUploaded = new SimpleBooleanProperty(false);
    //This boolean property is used to bound the confirm button to the fact that we are currently processing, together with the fact that we have a picture to upload or delete
    private final BooleanProperty isCurrentlyProcessing = new SimpleBooleanProperty(false);
    private final BooleanProperty zoomPropertyChanged = new SimpleBooleanProperty(false);

    private final Button saveButton;


    public ChangePictureUI(UserProfileActivity activity) {
        //container.setMinWidth(500);
        parentActivity = activity;
        container.setPadding(new Insets(15, 0, 0, 0));

        StackPane imageStackPane = new StackPane();
        imageStackPane.setMaxWidth(MAX_PICTURE_SIZE);
        String noPictureImage = "images/large/no-picture.png";
        ImageView noPictureImageView = new ImageView(new Image(noPictureImage));
        noPictureImageView.setPreserveRatio(true);
        noPictureImageView.setFitHeight(MAX_PICTURE_SIZE);

        imageView = new ImageView();
        //imageView.setPreserveRatio(true);
        imageView.setFitHeight(MAX_PICTURE_SIZE);
        imageView.setFitWidth(MAX_PICTURE_SIZE);
        imageStackPane.getChildren().addAll(noPictureImageView, imageView);
        imageStackPane.setAlignment(Pos.CENTER);

        Circle clip = new Circle(MAX_PICTURE_SIZE / 2.0);
        clip.setCenterX(MAX_PICTURE_SIZE / 2.0);
        clip.setCenterY(MAX_PICTURE_SIZE / 2.0);
        imageStackPane.setClip(clip);

        // Variables to track mouse position
        double[] mouseAnchorX = new double[1];
        double[] mouseAnchorY = new double[1];
        double[] initialTranslateX = new double[1];
        double[] initialTranslateY = new double[1];
        // Add mouse press event to set anchors
        imageView.setOnMousePressed(event -> {
            mouseAnchorX[0] = event.getSceneX();
            mouseAnchorY[0] = event.getSceneY();
            initialTranslateX[0] = imageView.getTranslateX();
            initialTranslateY[0] = imageView.getTranslateY();
        });

        // Add mouse drag event to move the ImageView
        imageView.setOnMouseDragged(event -> {
            deltaX = event.getSceneX() - mouseAnchorX[0] + initialTranslateX[0];
            deltaY = event.getSceneY() - mouseAnchorY[0] + initialTranslateY[0];
            imageView.setTranslateX(deltaX);
            imageView.setTranslateY(deltaY);
        });

        Label infoMessage = Bootstrap.textDanger(new Label());
        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);

        // Zoom slider
        zoomSlider = new Slider(0.5, 2.0, 1.0);
        zoomSlider.setMaxWidth(150);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomFactor = newVal.doubleValue();
            imageView.setScaleX(zoomFactor);
            imageView.setScaleY(zoomFactor);
            zoomPropertyChanged.setValue(true);
        });
        SVGPath zoomOutIcon = SvgIcons.createZoomInOutPath();
        SVGPath zoomInIcon = SvgIcons.createZoomInOutPath();
        zoomOutIcon.setFill(Color.LIGHTGRAY);
        zoomInIcon.setFill(Color.LIGHTGRAY);
        zoomInIcon.setScaleX(1.5);
        zoomInIcon.setScaleY(1.5);
        HBox zoomSliderHBox = new HBox(5, zoomOutIcon, zoomSlider, zoomInIcon);
        zoomSliderHBox.setPadding(new Insets(0, 0, 15, 0));
        BorderPane zoomSliderPane = new BorderPane();
        zoomSliderPane.setTop(zoomSliderHBox);
        zoomSliderHBox.setAlignment(Pos.CENTER);

        Hyperlink removePictureLink = Bootstrap.strong(Bootstrap.textDanger(I18nControls.newHyperlink(UserProfileI18nKeys.RemovePicture)));
        removePictureLink.setMinWidth(Region.USE_PREF_SIZE);
        Hyperlink addPictureLink = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newHyperlink(UserProfileI18nKeys.UploadPicture)));
        addPictureLink.setMinWidth(Region.USE_PREF_SIZE);

        FilePicker filePicker = FilePicker.create();
        filePicker.getAcceptedExtensions().addAll("image/*");
        filePicker.setGraphic(addPictureLink);
        filePicker.getSelectedFiles().addListener((InvalidationListener) obs -> {
            ObservableList<File> fileList = filePicker.getSelectedFiles();
            cloudPictureFileToUpload = fileList.get(0);
            Image imageToDisplay = new Image(cloudPictureFileToUpload.getObjectURL(), true);
            FXProperties.runOnPropertiesChange(property -> {
                if (imageToDisplay.progressProperty().get() == 1) {
                    setImage(imageToDisplay);
                    isPictureToBeDeleted.setValue(false);
                    //and upload the new one
                    isPictureToBeUploaded.setValue(true);
                }
            }, imageToDisplay.progressProperty());
        });


        //  addPictureLink.setOnAction(e -> addPicture());
        removePictureLink.setOnAction(e -> removePicture());

        // Layout
        Region hGrowable = Layouts.createHGrowable(70);
        hGrowable.setMinWidth(10);
        HBox hyperlinks = new HBox(removePictureLink, hGrowable, filePicker.getView());
        hyperlinks.setAlignment(Pos.CENTER);
        zoomSliderPane.setBottom(hyperlinks);

        // Save button
        saveButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Confirm));

        //We bind the disable property of the save button to the fact that we have a picture to upload or delete, and also if the picture is currently being processed
        //(because it is bind, the turn on/turn off can't change the state of the button)
        saveButton.disableProperty().bind((isPictureToBeUploaded.not().and(isPictureToBeDeleted.not().and(zoomPropertyChanged.not())).or(isCurrentlyProcessing)));

        saveButton.setOnAction(e -> {
            OperationUtil.turnOnButtonsWaitMode(saveButton);
            isCurrentlyProcessing.setValue(true);
            String cloudImagePath = ModalityCloudinary.personImagePath(parentActivity.getCurrentPerson());
            // Create a Canvas to draw the original image
            Image originalImage = imageView.getImage();
            if (originalImage == null) {
                //Here we choose to remove the picture
                deleteIfNeededAndUploadIfNeededCloudPicture(cloudImagePath);
            } else {
                ModalityCloudinary.prepareImageForUpload(originalImage, true, zoomFactor, deltaX, deltaY, MAX_PICTURE_SIZE, MAX_PICTURE_SIZE)
                    .onFailure(Console::log)
                    .onSuccess(blob -> {
                        cloudPictureFileToUpload = blob;
                        deleteIfNeededAndUploadIfNeededCloudPicture(cloudImagePath);
                    });
            }
        });

        Hyperlink cancel = Bootstrap.textSecondary(I18nControls.newHyperlink(UserProfileI18nKeys.Cancel));
        cancel.setMinWidth(Region.USE_PREF_SIZE);
        cancel.setOnAction(e -> {
            if (callback != null)
                callback.closeDialog();
        });
        hGrowable = Layouts.createHGrowable();
        hGrowable.setPrefWidth(60);
        hGrowable.setMinWidth(10);
        HBox actionHBox = new HBox(cancel, hGrowable, saveButton);
        actionHBox.setAlignment(Pos.CENTER);
        actionHBox.setPadding(new Insets(40, 0, 0, 0));
        container.getChildren().setAll(
            imageStackPane,
            zoomSliderPane,
            infoMessage,
            actionHBox);
        ChangePasswordUI.setupModalVBox(container);

        //We prevent the mouse click event to pass through the Pane
        container.setOnMouseClicked(Event::consume);
    }


    private void deleteIfNeededAndUploadIfNeededCloudPicture(String cloudImagePath) {
        //We delete the pictures, and all the cached picture in cloudinary that can have been transformed, related
        //to these assets, then upload the new picture
        if (isPictureToBeDeleted.getValue()) {
            ModalityCloudinary.deleteImage(cloudImagePath)
                .onFailure(fail -> Console.log("Error while deleting the picture: " + fail.getMessage()))
                .onSuccess(ok -> {
                    if (Objects.equals(cloudImagePath, recentlyUploadedCloudPictureId))
                        recentlyUploadedCloudPictureId = null;
                })
                .inUiThread()
                .onComplete(ar -> {
                    if (isPictureToBeUploaded.getValue())
                        uploadCloudPictureIfNeeded(cloudImagePath);
                        //Here is we don't upload a new picture, it means we want to only delete the picture
                    else {
                        parentActivity.removeUserProfilePicture();
                        callback.closeDialog();
                        OperationUtil.turnOffButtonsWaitMode(saveButton);
                        isCurrentlyProcessing.setValue(false);
                        reinitialize();
                    }
                });
        } else {
            uploadCloudPictureIfNeeded(cloudImagePath);
        }
    }

    public Region getView() {
        return container;
    }

    //TODO: implement the visible content saving

    /**
     * public void saveVisibleContent(ImageView imageView, Circle clip, File outputFile) {
     * // Create SnapshotParameters
     * SnapshotParameters params = new SnapshotParameters();
     * params.setFill(Color.TRANSPARENT); // Set transparent background if needed
     * <p>
     * // Set the viewport matching the circular clip
     * params.setViewport(new javafx.geometry.Rectangle2D(
     * clip.getCenterX() - clip.getRadius(),
     * clip.getCenterY() - clip.getRadius(),
     * clip.getRadius() * 2,
     * clip.getRadius() * 2
     * ));
     * <p>
     * // Take the snapshot
     * WritableImage snapshot = imageView.snapshot(params, null);
     * <p>
     * // Save the snapshot to a file
     * try {
     * ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }
     */


    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }

    private void removePicture() {
        setImage(null);
        isPictureToBeDeleted.setValue(true);
        isPictureToBeUploaded.setValue(false);
        zoomSlider.setValue(1.0);
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
    }

    public void uploadCloudPictureIfNeeded(String cloudImagePath) {
        if (isPictureToBeUploaded.get()) {
            ModalityCloudinary.uploadImage(cloudImagePath, cloudPictureFileToUpload)
                .onFailure(Console::log)
                .onSuccess(ok -> {
                    recentlyUploadedCloudPictureId = cloudImagePath;
                    callback.closeDialog();
                })
                .inUiThread()
                .onComplete(event -> {
                    parentActivity.showProgressIndicator();
                    //We wait for some time before reloading the image, so cloudinary has the time to process it (it can take up to 10 seconds)
                    UiScheduler.scheduleDelay(CLOUDINARY_RELOAD_DELAY, parentActivity::loadProfilePictureIfExist);
                    OperationUtil.turnOffButtonsWaitMode(saveButton);
                    isCurrentlyProcessing.setValue(false);
                    reinitialize();
                });
        }
    }

    public Object getRecentlyUploadedCloudPictureId() {
        return recentlyUploadedCloudPictureId;
    }

    private void reinitialize() {
        isPictureToBeDeleted.setValue(false);
        isPictureToBeUploaded.setValue(false);
        recentlyUploadedCloudPictureId = null;
    }

    public void setImage(Image imageToDisplay) {
        imageView.setImage(imageToDisplay);
        imageView.setPreserveRatio(true);
        if (imageToDisplay != null && imageToDisplay.getWidth() > imageToDisplay.getHeight())
            imageView.setFitWidth(MAX_PICTURE_SIZE);
        else
            imageView.setFitHeight(MAX_PICTURE_SIZE);

        // Reset zoom
        zoomSlider.setValue(1.0);
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
    }

}
