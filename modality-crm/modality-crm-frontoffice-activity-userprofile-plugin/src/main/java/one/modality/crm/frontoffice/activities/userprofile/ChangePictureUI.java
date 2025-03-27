package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.canvas.blob.CanvasBlob;
import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;

import java.util.Objects;

final class ChangePictureUI {

    static final long CLOUDINARY_RELOAD_DELAY = 10000;

    private final VBox changePictureVBox = new VBox();
    private final Slider zoomSlider;
    private final ImageView imageView;
    private double deltaX = 0;
    private double deltaY = 0;
    private double zoomFactor = 1;
    private dev.webfx.platform.file.File cloudPictureFileToUpload;
    private Object recentlyUploadedCloudPictureId;
    private final ScalePane container = new ScalePane(changePictureVBox);
    private final UserProfileActivity parentActivity;
    private DialogCallback callback;
    private final int MAX_PICTURE_SIZE = 240;
    //Those two boolean property are used to know if we have to delete the picture, upload a new one, or if we are currently processing
    // (because we do the processing when we press the confirm button and not when we upload the picture)
    private final BooleanProperty isPictureToBeDeleted = new SimpleBooleanProperty(false);
    private final BooleanProperty isPictureToBeUploaded = new SimpleBooleanProperty(false);
    //This boolean property is used to bound the confirm button to the fact that we are currently processing, together with the fact that we have a picture to upload or delete
    private final BooleanProperty isCurrentlyProcessing = new SimpleBooleanProperty(false);
    private final BooleanProperty zoomPropertyChanged = new SimpleBooleanProperty(false);

    private final Button saveButton;


    public ChangePictureUI(UserProfileActivity activity) {
        changePictureVBox.setMinWidth(500);
        parentActivity = activity;
        changePictureVBox.setPadding(new Insets(15, 0, 0, 0));

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
        Hyperlink addPictureLink = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newHyperlink(UserProfileI18nKeys.UploadPicture)));

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
        HBox hyperlinks = new HBox(70, removePictureLink, filePicker.getView());
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
            // Create a Canvas to draw the original image
            Image originalImage = imageView.getImage();
            Image resultImageToUpload = originalImage;
            if (originalImage != null) {
                double imageWidth = originalImage.getWidth();
                double imageHeight = originalImage.getHeight();
                if (imageWidth != imageHeight) {
                    //First, in case the image is not squared, we make a square one by adding transparent bg in the missing part
                    double newWidth = Math.max(imageWidth, imageHeight);
                    double newHeight = Math.max(imageWidth, imageHeight);
                    WritableImage paddedImage = new WritableImage((int) newWidth, (int) newHeight);
                    resultImageToUpload = paddedImage;
                    // Draw the original image onto the new image with transparency
                    Canvas canvas = new Canvas(newWidth, newHeight);
                    GraphicsContext gc = canvas.getGraphicsContext2D();

                    // Fill the background with transparent color
                    gc.setFill(Color.TRANSPARENT);
                    gc.fillRect(0, 0, newWidth, newHeight);

                    // Draw the original image centered in the new image
                    double x = (newWidth - originalImage.getWidth()) / 2;
                    double y = (newHeight - originalImage.getHeight()) / 2; // Center vertically
                    gc.drawImage(originalImage, x, y);
                    // Snapshot the canvas into the WritableImage
                    canvas.snapshot(null, paddedImage);
                }

                imageWidth = resultImageToUpload.getWidth();
                imageHeight = resultImageToUpload.getHeight();
                double scalingPercentage = Math.max(resultImageToUpload.getWidth() / MAX_PICTURE_SIZE, resultImageToUpload.getHeight() / MAX_PICTURE_SIZE);

                double canvasWidth = MAX_PICTURE_SIZE * 2;
                double canvasHeight = MAX_PICTURE_SIZE * 2;
                Canvas canvas = new Canvas(canvasWidth, canvasHeight);
                GraphicsContext gc = canvas.getGraphicsContext2D();

                // Calculate the scaled width and height of the image
                double scaledWidth = imageWidth / zoomFactor;
                double scaledHeight = imageHeight / zoomFactor;
                // scalingPercentage = 1;
                // Calculate offsets to center the image on the canvas
                double xOffset = (imageWidth - scaledWidth) / 2 - deltaX * scalingPercentage / zoomFactor;
                double yOffset = (imageHeight - scaledHeight) / 2 - deltaY * scalingPercentage / zoomFactor;
                // Clear the canvas
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                // Draw the image scaled and centered
                gc.drawImage(resultImageToUpload, xOffset, yOffset, scaledWidth, scaledHeight, 0, 0, canvasWidth, canvasHeight);
                CanvasBlob.createCanvasBlob(canvas)
                    .onFailure(Console::log)
                    .onSuccess(blob -> {
                        cloudPictureFileToUpload = (File) blob;
                        String cloudImagePath = ModalityCloudinary.personImagePath(parentActivity.getCurrentPerson());
                        deleteIfNeededAndUploadIfNeededCloudPicture(cloudImagePath);
                    });
            } else {
                //Here we choose to remove the picture
                String cloudImagePath = ModalityCloudinary.personImagePath(parentActivity.getCurrentPerson());
                deleteIfNeededAndUploadIfNeededCloudPicture(cloudImagePath);
            }
        });

        Hyperlink cancel = Bootstrap.textSecondary(I18nControls.newHyperlink(UserProfileI18nKeys.Cancel));
        cancel.setOnAction(e -> {
            if (callback != null)
                callback.closeDialog();
        });
        HBox actionHBox = new HBox(60, cancel, saveButton);
        actionHBox.setAlignment(Pos.CENTER);
        actionHBox.setPadding(new Insets(40, 0, 0, 0));
        changePictureVBox.getChildren().setAll(imageStackPane, zoomSliderPane, infoMessage, actionHBox);
        changePictureVBox.setMaxWidth(MAX_PICTURE_SIZE);
        changePictureVBox.setBackground(Background.fill(Color.WHITE));
        changePictureVBox.setAlignment(Pos.CENTER);
        changePictureVBox.setSpacing(20);
        ChangePasswordUI.setupModalVBox(changePictureVBox);

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
                .onComplete(ar -> {
                    if (isPictureToBeUploaded.getValue())
                        uploadCloudPictureIfNeeded(cloudImagePath);
                        //Here is we don't upload a new picture, it means we want to only delete the picture
                    else {
                        parentActivity.removeUserProfilePicture();
                        callback.closeDialog();
                        Platform.runLater(() -> OperationUtil.turnOffButtonsWaitMode(saveButton));
                        isCurrentlyProcessing.setValue(false);
                        reinitialize();
                    }
                });
        } else {
            uploadCloudPictureIfNeeded(cloudImagePath);
        }
    }

    public ScalePane getView() {
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
                .onComplete(event -> {
                    Platform.runLater(parentActivity::showProgressIndicator);
                    //We wait for some time before reloading the image so cloudinary has the time to process it (it can take up to 10 seconds)
                    UiScheduler.scheduleDelay(CLOUDINARY_RELOAD_DELAY, parentActivity::loadProfilePictureIfExist);
                    Platform.runLater(() -> OperationUtil.turnOffButtonsWaitMode(saveButton));
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
