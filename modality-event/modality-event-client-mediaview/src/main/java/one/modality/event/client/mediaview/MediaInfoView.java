package one.modality.event.client.mediaview;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.HPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.Media;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Status;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.css.Fonts;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.frontoffice.utility.tyler.StyleUtility;
import one.modality.base.frontoffice.utility.tyler.TextUtility;
import one.modality.base.shared.entities.Video;
import one.modality.base.shared.entities.markers.HasAudioUrl;
import one.modality.base.shared.entities.markers.HasMediaInfo;
import one.modality.base.shared.entities.markers.HasWistiaVideoId;
import one.modality.base.shared.entities.markers.HasYoutubeVideoId;

import java.time.format.DateTimeFormatter;

public abstract class MediaInfoView {

    private static final double WIDE_VIDEO_MAX_WIDTH = 880;
    private static final String FAVORITE_PATH = "M 24.066331,0 C 21.212473,0 18.540974,1.2921301 16.762259,3.4570778 14.983544,1.2920563 12.312119,0 9.4581876,0 4.2429514,0 0,4.2428782 0,9.4581873 0,13.54199 2.4351327,18.265558 7.237612,23.497667 c 3.695875,4.026405 7.716386,7.143963 8.860567,8.003592 L 16.762038,32 17.425897,31.501333 c 1.144181,-0.859629 5.164839,-3.977113 8.860788,-8.003518 4.802627,-5.23211 7.237834,-9.955751 7.237834,-14.0396277 C 33.524519,4.2428782 29.281567,0 24.066331,0 Z";

    // The media player associated with this particular podcast. Note that this podcast view can be recycled, which
    // means its associated podcast can change (through setPodcast()). When recycled, the media player can eventually
    // be retrieved from the already existing media players (if the user already played that podcast) so its visual
    // state can be re-established in that case. Otherwise - if the podcast hasn't been played so far in this session -
    // the media player will be null until the user presses the play button.
    private final Player player = AllPlayers.createAllAudioVideoPlayer();
    protected boolean isAudio, isVideo, isWistiaVideo, isYoutubeVideo, isWideVideo;
    private Unregisterable mediaPlayerBinding; // will allow to unbind a recycled view from its previous associated media player.
    protected HasMediaInfo mediaInfo;
    protected Duration mediaDuration;
    private double wideVideoMaxWidth = WIDE_VIDEO_MAX_WIDTH;
    private boolean decorated = true;
    private final MonoPane videoContainer = new MonoPane();
    protected final ImageView imageView = new ImageView();
    private Image image;
    private final Rectangle imageClip = new Rectangle();
    protected final Text dateText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    protected final Label titleLabel = GeneralUtility.createLabel(Brand.getBrandMainColor());
    private final Label excerptLabel = GeneralUtility.createLabel(Color.BLACK);
    protected final Pane playButton = MediaButtons.createPlayButton();
    protected final Pane pauseButton = MediaButtons.createPauseButton();
    protected final Pane forwardButton = MediaButtons.createForwardButton();
    protected final Pane backwardButton = MediaButtons.createBackwardButton();
    protected final ProgressBar progressBar = new ProgressBar();
    protected final Text elapsedTimeText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    private final SVGPath favoriteSvgPath = new SVGPath();
    protected final Pane favoritePane = new MonoPane(favoriteSvgPath);
    private Timeline imageFadeTimeline;
    private boolean timelineShowImage;
    private double lastComputeLayoutWidth;

    protected Pane mediaPane = new HPane(videoContainer, imageView, dateText, titleLabel, excerptLabel, backwardButton, pauseButton, playButton, forwardButton, progressBar, elapsedTimeText, favoritePane) {
        private double fontFactor;
        private double leftX, imageY, imageWidth, imageHeight, rightX, rightWidth, dateY, dateHeight, titleY, titleHeight, excerptY, excerptHeight, buttonY, buttonSize, favoriteY, favoriteHeight;
        private HPos titleHPos, favoriteHPos;
        private double wideVideoVSpace;

        @Override
        protected void layoutChildren(double width, double height) {
            computeLayout(width);
            imageView.setFitWidth(isAudio ? imageWidth : 0);
            imageView.setFitHeight(imageHeight);
            layoutInArea(imageView, leftX, imageY, imageWidth, imageHeight, HPos.CENTER, VPos.CENTER);
            layoutInArea(dateText, rightX, dateY, rightWidth, dateHeight, HPos.LEFT, VPos.TOP);
            layoutInArea(titleLabel, rightX, titleY, rightWidth, titleHeight, titleHPos, VPos.TOP);
            layoutInArea(excerptLabel, rightX, excerptY, rightWidth, excerptHeight, HPos.LEFT, VPos.TOP);
            layoutInArea(favoritePane, rightX, favoriteY, rightWidth, favoriteHeight, favoriteHPos, VPos.TOP);
            if (isVideo) {
                layoutInArea(videoContainer, leftX, imageY, imageWidth, imageHeight, HPos.CENTER, VPos.CENTER);
                layoutInArea(playButton, rightX, buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                layoutInArea(pauseButton, rightX, buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                layoutInArea(elapsedTimeText, rightX + buttonSize + 20, buttonY, rightWidth, buttonSize, HPos.LEFT, VPos.CENTER);
            } else {
                imageClip.setWidth(imageWidth);
                imageClip.setHeight(imageHeight);
                double arcWidthHeight = imageWidth / 4;
                imageClip.setArcWidth(arcWidthHeight);
                imageClip.setArcHeight(arcWidthHeight);
                layoutInArea(backwardButton, rightX, buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                layoutInArea(playButton, rightX + buttonSize + 5, buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                layoutInArea(pauseButton, rightX + buttonSize + 5, buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                layoutInArea(forwardButton, rightX + 2 * (buttonSize + 5), buttonY, buttonSize, buttonSize, HPos.LEFT, VPos.TOP);
                double progressBarX = rightX + 3 * (buttonSize + 5), progressBarHeight = 10, progressBarY = buttonY + buttonSize / 2 - progressBarHeight / 2;
                progressBar.setPrefWidth(getWidth() - progressBarX);
                layoutInArea(progressBar, progressBarX, progressBarY, progressBar.getPrefWidth(), progressBarHeight, HPos.LEFT, VPos.CENTER);
                layoutInArea(elapsedTimeText, progressBarX, buttonY + buttonSize, rightWidth, buttonSize, HPos.LEFT, VPos.TOP);
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            computeLayout(width);
            return Math.max(imageY + imageHeight, favoriteY + favoriteHeight) + (isWideVideo && decorated ? wideVideoVSpace : 0);
        }

        private void computeLayout(double width) {
            boolean videoTransition = mediaInfo instanceof Video && (imageFadeTimeline != null || imageView.getOpacity() < 1);
            if (width == lastComputeLayoutWidth && !videoTransition)
                return;
            if (width == -1)
                width = getWidth();
            /* Updating fonts if necessary (required before layout) */
            double fontFactor = GeneralUtility.computeFontFactor(width);
            if (fontFactor != this.fontFactor) {
                this.fontFactor = fontFactor;
                GeneralUtility.setLabeledFont(  titleLabel, Fonts.MONTSERRAT_TEXT_FAMILY,  FontWeight.SEMI_BOLD, fontFactor * StyleUtility.MAIN_TEXT_SIZE);
                TextUtility.setTextFont(          dateText, Fonts.MONTSERRAT_TEXT_FAMILY,  FontWeight.NORMAL, fontFactor * StyleUtility.SUB_TEXT_SIZE);
                GeneralUtility.setLabeledFont(excerptLabel, Fonts.MONTSERRAT_TEXT_FAMILY,  FontWeight.NORMAL, fontFactor * StyleUtility.MEDIUM_TEXT_SIZE);
                TextUtility.setTextFont(   elapsedTimeText, StyleUtility.CLOCK_FAMILY, FontWeight.NORMAL,    fontFactor * StyleUtility.SUB_TEXT_SIZE);
            }
            double imageRatio;
            if (isAudio) {
                imageRatio = image == null || image.getWidth() == 0 ? 1 : image.getWidth() / image.getHeight();
            } else {
                imageRatio = 16d / 9;
                if (videoTransition) {
                    Video video = (Video) mediaInfo;
                    Integer videoWidth = video.getWidth();
                    Integer videoHeight = video.getHeight();
                    if (videoWidth != null && videoHeight != null)
                        imageRatio = videoWidth * 1d / videoHeight;
                }
            }
            if (isWideVideo) {
                // TODO Make this code looks like the other cases (comments, etc...)
                imageWidth = Math.min(width, wideVideoMaxWidth); imageHeight = imageWidth / imageRatio;
                double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
                wideVideoVSpace = Math.max(screenHeight * 0.7 - imageHeight, screenHeight / 4);
                leftX = width / 2 - imageWidth / 2;
                rightX = leftX; rightWidth = imageWidth;
                titleY = 0;  titleHeight = titleLabel.prefHeight(rightWidth);
                imageY = decorated ? titleY + titleHeight + 20 : 0;
                buttonY = decorated ? imageY + imageHeight + 10 : 0; buttonSize = 32;
                favoriteY = buttonY; favoriteHeight = 32;
                dateY = buttonY; dateHeight = dateText.prefHeight(rightWidth);
                titleHPos = HPos.CENTER; favoriteHPos = HPos.RIGHT;
            } else {
                titleHPos = HPos.LEFT; favoriteHPos = HPos.LEFT;
                if (width <= 400) { // Small screen => vertical alignment: image above title, date, excerpt, buttons & favorite
                /*Image:*/       imageY = 0;                                                 imageWidth = width; imageHeight = imageWidth / imageRatio;
                /*Right side:*/  rightX = 0; /* Actually no right side */                    rightWidth = width - rightX;
                /*Tile:*/        titleY = imageY + imageHeight + 10;                        titleHeight = titleLabel.prefHeight(rightWidth);
                } else { // Normal or large screen => image on left, title, date, excerpt, buttons & favorite on right
                /*Image:*/       imageY = 0;                                                 imageWidth = isVideo ? width / 2 : width / 4; imageHeight = imageWidth / imageRatio;
                /*Right side:*/  rightX = imageWidth + 20;                                   rightWidth = width - rightX;
                /*Tile:*/        titleY = 0;       titleHeight = titleLabel.prefHeight(rightWidth);
                }
                /*Date:*/         dateY = titleY + titleHeight + 10;                         dateHeight = dateText.prefHeight(rightWidth);
                /*Excerpt:*/   excerptY = dateY + dateHeight + 10;                        excerptHeight = excerptLabel.prefHeight(rightWidth);
                /*Buttons:*/    buttonY = excerptY + excerptHeight + (isAudio ? 30 : 20);    buttonSize = 32;
                /*Favorite:*/ favoriteY = buttonY + buttonSize + (isAudio ? 30 : 20);    favoriteHeight = 32;
            }
            lastComputeLayoutWidth = width;
        }
    };

    {
        // Arming buttons
        GeneralUtility.onNodeClickedWithoutScroll(e -> play(), playButton, imageView);
        GeneralUtility.onNodeClickedWithoutScroll(e -> pause(), pauseButton);
        GeneralUtility.onNodeClickedWithoutScroll(e -> seekRelative(+30), forwardButton);
        GeneralUtility.onNodeClickedWithoutScroll(e -> seekRelative(-10), backwardButton);
        GeneralUtility.onNodeClickedWithoutScroll(e -> seekX(e.getX()), progressBar);
        progressBar   .setOnMouseDragged(         e -> seekX(e.getX()));
        GeneralUtility.onNodeClickedWithoutScroll(e -> {
            toggleAsFavorite();
            updateFavorite();
            e.consume();
        }, favoritePane);
        favoriteSvgPath.setContent(FAVORITE_PATH);
        favoriteSvgPath.setStrokeWidth(2);
        updateFavorite();
    }

    public void setMediaInfo(HasMediaInfo mediaInfo) {
        if (mediaInfo == this.mediaInfo)
            return;
        lastComputeLayoutWidth = 0;
        this.mediaInfo = mediaInfo;
        isAudio = (mediaInfo instanceof HasAudioUrl) && ((HasAudioUrl) mediaInfo).getAudioUrl() != null;
        isWistiaVideo = !isAudio && (mediaInfo instanceof HasWistiaVideoId && ((HasWistiaVideoId) mediaInfo).getWistiaVideoId() != null);
        isYoutubeVideo = !isAudio && (mediaInfo instanceof HasYoutubeVideoId && ((HasYoutubeVideoId) mediaInfo).getYoutubeVideoId() != null);
        isVideo = isWistiaVideo || isYoutubeVideo;
        isWideVideo = isVideo && mediaInfo.getExcerpt() == null;
        titleLabel.setTextFill(isWideVideo ? StyleUtility.ELEMENT_GRAY_COLOR : Brand.getBrandMainColor());
        // Updating all fields and UI from the podcast
        imageView.setPreserveRatio(true);
        imageView.setClip(isAudio ? imageClip : null);
        updateText(dateText, mediaInfo.getDate() == null ? null : DateTimeFormatter.ofPattern("d MMMM yyyy").format(mediaInfo.getDate()));
        updateLabel(titleLabel, mediaInfo.getTitle());
        updateLabel(excerptLabel, mediaInfo.getExcerpt());
        image = ImageStore.getOrCreateImage(mediaInfo.getImageUrl());
        imageView.setImage(image);
        mediaDuration = mediaInfo.getDurationMillis() == null ? null : Duration.millis(mediaInfo.getDurationMillis());
        backwardButton.setVisible(decorated && isAudio);
        forwardButton.setVisible(decorated && isAudio);
        progressBar.setVisible(decorated && isAudio);
        elapsedTimeText.setVisible(decorated && !isWideVideo);
        favoritePane.setVisible(decorated);
        // playButton & pauseButton visibility are set by updatePlayPauseButtons()
        updateFavorite();
        // If no, the player associated with this podcast should be null
        // If this podcast view was previously associated with a player, we unbind it.
        unbindMediaPlayer(); // will unregister the possible existing binding, and reset the visual state
        // For videos with no image on top of them, we display the video straightaway (but don't play it yet)
        if (isVideo && image == null) {
            setPlayerMedia();
            player.displayVideo();
        }
        bindMediaPlayer(); // => will restore the visual state from the player (play/pause button & progress bar)
    }

    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    public void setWideVideoMaxWidth(double wideVideoMaxWidth) {
        this.wideVideoMaxWidth = wideVideoMaxWidth;
    }

    private String getMediaToken() {
        if (isAudio)
            return ((HasAudioUrl) mediaInfo).getAudioUrl();
        if (isWistiaVideo)
            return "wistia:" + ((HasWistiaVideoId) mediaInfo).getWistiaVideoId();
        return "youtube:" + ((HasYoutubeVideoId) mediaInfo).getYoutubeVideoId();
    }

    public Node getView() {
        return mediaPane;
    }

    private void updateFavorite() {
        boolean isFavorite = isMarkedAsFavorite();
        favoriteSvgPath.setStroke(isFavorite ? Color.RED : Color.GRAY);
        favoriteSvgPath.setFill(isFavorite ? Color.RED : null);
    }

    protected abstract boolean isMarkedAsFavorite();

    protected abstract void toggleAsFavorite();

    private void play() {
        // Setting the media of the player if not already done
        setPlayerMedia();
        // Starting playing
        player.play();
        updatePlayPauseButtons(true);
    }

    private void pause() {
        player.pause();
    }

    private void seekRelative(double relativeSeconds) {
        player.seek(player.getCurrentTime().add(Duration.seconds(relativeSeconds)));
    }

    private void seekX(double x) {
        double percentage = x / progressBar.getWidth();
        Duration seekTime = mediaDuration.multiply(percentage);
        player.seek(seekTime);
    }

    private void setPlayerMedia() {
        String mediaToken = getMediaToken();
        Media media = player.acceptMedia(mediaToken);
        player.setMedia(media);
        player.setOnEndOfPlaying(player::stop); // Forcing stop status (sometimes this doesn't happen automatically for any reason)
        // Binding this media player with this podcast view
        bindMediaPlayer();
    }

    private void bindMediaPlayer() {
        unbindMediaPlayer(); // in case this view was previously bound with another player
        videoContainer.setContent(player.getMediaView());
        if (isVideo) {
            mediaPlayerBinding = FXProperties.runNowAndOnPropertyChange(status -> {
                // Resetting the video to the beginning,
                if (status == Status.STOPPED)
                    player.resetToInitialState();
                updatePlayPauseButtons(null);
            }, player.statusProperty());
        } else { // audio
            mediaPlayerBinding = FXProperties.runNowAndOnPropertiesChange(() -> {
                updatePlayPauseButtons(null);
                boolean isPlaying = player.isPlaying();
                Status status = player.getStatus();
                if (status == null || status == Status.LOADING)
                    progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                else
                    updateElapsedTimeAndProgressBar(status == Status.NO_MEDIA ? Duration.ZERO : isPlaying || status == Status.PAUSED ? player.getCurrentTime() : mediaDuration);
            }, player.statusProperty(), player.currentTimeProperty());
        }
        // Not yet supported by WebFX
        //mediaPlayer.setOnError(() -> System.out.println("An error occurred: " + mediaPlayer.getError()));
    }

    private void unbindMediaPlayer() {
        if (mediaPlayerBinding != null) {
            mediaPlayerBinding.unregister();
            mediaPlayerBinding = null;
        }
        updatePlayPauseButtons(false);
        updateElapsedTimeAndProgressBar(Duration.ZERO);
    }

    protected void updatePlayPauseButtons(Boolean anticipatePlaying) {
        Status status = player.getStatus();
        boolean playing = status == Status.PLAYING;
        pauseButton.setVisible(decorated && !isWideVideo && (anticipatePlaying != null ? anticipatePlaying : playing));
        playButton.setVisible(decorated && !isWideVideo && (anticipatePlaying != null ? !anticipatePlaying : !playing));
        // Sometimes this method is called with an anticipated value for isPlaying (ex: play() method). So we check if
        // the player is really playing
        // Note: when paused, we prefer to show the image (if available) if the player is not able to resume (navigation api not supported)
        boolean showVideo = isVideo && (image == null || !Boolean.FALSE.equals(anticipatePlaying) && (playing || status == Status.PAUSED && player.getNavigationSupport().api()));
        // Note: using setVisible(false) doesn't prevent wistia player to appear sometimes, while setOpacity(0) does
        videoContainer.setOpacity(showVideo ? 1 : 0);
        imageView.setMouseTransparent(showVideo);
        showImage(!showVideo);
    }

    private void showImage(boolean showImage) {
        double requestedImageOpacity = showImage ? 1 : 0;
        if (imageFadeTimeline != null) { // animation already running
            if (timelineShowImage == showImage) // same as new request => we just keep going
                return;
            imageFadeTimeline.stop(); // different as new request => we stop the previous one
        } else { // no animation running => we need an animation only if the opacity is not the requested one
            if (imageView.getOpacity() == requestedImageOpacity)
                return;
        }
        timelineShowImage = showImage;
        imageFadeTimeline = Animations.animateProperty(imageView.opacityProperty(), requestedImageOpacity, Duration.seconds(1), Interpolator.EASE_BOTH, true);
        imageFadeTimeline.setOnFinished(e -> {
            imageFadeTimeline = null;
            lastComputeLayoutWidth = 0;
            mediaPane.requestLayout();
        });
        mediaPane.requestLayout();
    }

    private void updateElapsedTimeAndProgressBar(Duration elapsed) {
        if (isAudio) {
            updateText(elapsedTimeText, formatDuration(elapsed) + " / " + formatDuration(mediaDuration));
            if (mediaDuration != null)
                progressBar.setProgress(elapsed.toMillis() / mediaDuration.toMillis());
        } else
            updateText(elapsedTimeText, formatDuration(mediaDuration));
    }

    private static void updateText(Text text, String newContent) {
        if (!Objects.areEquals(newContent, text.getText()))
            text.setText(newContent);
    }

    private static void updateLabel(Label label, String newContent) {
        if (!Objects.areEquals(newContent, label.getText()))
            label.setText(newContent);
    }

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isIndefinite() || duration.isUnknown())
            return "xx:xx";
        int minutes = (int) duration.toMinutes();
        int seconds = ((int) duration.toSeconds()) % 60;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

}