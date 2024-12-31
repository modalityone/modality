package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.activities.audiorecordings.AudioRecordingsI18nKeys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class SessionVideoPlayerActivity extends ViewDomainActivityBase {

    private final ObjectProperty<Object> scheduledVideoItemIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledVideoItemProperty is enough)
    private Label videoExpirationLabel;
    private final Label sessionTitleLabel = Bootstrap.h4(Bootstrap.strong(new Label()));
    private final Label sessionCommentLabel = new Label();
    private List<MultiPlayer> sessionVideoPlayers;
    private final MultiPlayer sessionVideoPlayer = AllPlayers.createAllVideoPlayer();
    private Label eventDescriptionLabel = new Label();
    private Label eventLabel = Bootstrap.h2(Bootstrap.strong(new Label()));
    private final CloudImageService cloudImageService = new ClientImageService();
    private static final int IMAGE_HEIGHT = 240;
    private MonoPane imageMonoPane;
    private ImageView imageView;
    private VBox playersVBoxContainer;


    @Override
    protected void updateModelFromContextParameters() {
        scheduledVideoItemIdProperty.set(Numbers.toInteger(getParameter(SessionVideoPlayerRouting.SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object scheduledVideoItemId = scheduledVideoItemIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (scheduledVideoItemId == null || userPersonId == null) {
                publishedMedias.clear();
                scheduledVideoItemProperty.set(null); // Will update UI
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select expirationDate, comment, date, startTime, endTime, programScheduledItem.timeline.(startTime, endTime), programScheduledItem.name, event.name, event.shortDescription, event.vodExpirationDate" +
                            " from ScheduledItem si" +
                            " where id=? and published and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(person=? and price_balance<=0)))",
                            new Object[]{scheduledVideoItemId, userPersonId}),
                        new EntityStoreQuery("select url" +
                            " from Media" +
                            " where scheduledItem.(id=? and online)",
                            new Object[]{scheduledVideoItemId}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        Collections.setAll(publishedMedias, entityLists[1]);
                        scheduledVideoItemProperty.set((ScheduledItem) Collections.first(entityLists[0]));  // Will update UI
                    }));
            }
        }, scheduledVideoItemIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the session video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the session video player restarts.
        //updateSessionTitleAndVideoPlayerState();
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(50);
        headerHBox.setPadding(new Insets(0, 20, 0, 20));
        imageMonoPane = new MonoPane();
        imageView = new ImageView();

        headerHBox.getChildren().add(imageMonoPane);
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));

        eventDescriptionLabel.setWrapText(true);
        eventDescriptionLabel.setTextAlignment(TextAlignment.LEFT);
        eventDescriptionLabel.managedProperty().bind(FXProperties.compute(eventDescriptionLabel.textProperty(), Strings::isNotEmpty));
        eventDescriptionLabel.setMaxHeight(60);
        videoExpirationLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AvailableUntil);
        videoExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionLabel, videoExpirationLabel);

        headerHBox.getChildren().add(titleVBox);


        // Session title
        VBox sessionDescriptionVBox = new VBox(20);
        sessionDescriptionVBox.setAlignment(Pos.TOP_LEFT);
        sessionDescriptionVBox.setPadding(new Insets(100, 20, 0, 20));
        sessionTitleLabel.setWrapText(true);
        sessionTitleLabel.setTextAlignment(TextAlignment.CENTER);
        sessionCommentLabel.setWrapText(true);

        sessionDescriptionVBox.getChildren().addAll(sessionTitleLabel, sessionCommentLabel);

        playersVBoxContainer = new VBox(30);

        VBox pageContainer = new VBox(40,
            headerHBox,
            sessionDescriptionVBox,
            playersVBoxContainer);

        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.getStyleClass().add("livestream");

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertyChange(this::updateSessionTitleAndVideoPlayerState, scheduledVideoItemProperty);


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }

    private void updateSessionTitleAndVideoPlayerState() {
        ScheduledItem scheduledVideoItem = scheduledVideoItemProperty.get();
        Media firstMedia = Collections.first(publishedMedias);
        if (scheduledVideoItem != null && firstMedia != null) { // may not yet be loaded on first call
            String title = scheduledVideoItem.getProgramScheduledItem().getName();
            String url = firstMedia.getUrl();
            eventLabel.setText(scheduledVideoItem.getEvent().getName());
            eventDescriptionLabel.setText(scheduledVideoItem.getEvent().getShortDescription());
            LocalDateTime startTime = LocalDateTime.of(scheduledVideoItem.getDate(), scheduledVideoItem.getProgramScheduledItem().getTimeline().getStartTime());
            if (startTime != null)
                sessionTitleLabel.setText(title + " (" + startTime.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")) + ")");
            else {
                sessionTitleLabel.setText(title + " (" + scheduledVideoItem.getDate().format(DateTimeFormatter.ofPattern("d MMMM, yyyy")) + ")");
            }
            sessionCommentLabel.setText(scheduledVideoItem.getComment());
            if (scheduledVideoItem.getComment() != null) {
                sessionCommentLabel.setManaged(true);
            } else {
                sessionCommentLabel.setManaged(false);
            }
            Object imageTag;
            if (I18n.getLanguage() == null || "en".equals(I18n.getLanguage().toString())) {
                //We do add .jpg even if the image is not jpg, because for some reason, if we don't put an extension file, cloudinary doesn't always find the image, but it works when adding .jpg.
                imageTag = scheduledVideoItem.getEvent().getId().getPrimaryKey() + "-cover.jpg";
            } else {
                imageTag = scheduledVideoItem.getEvent().getId().getPrimaryKey() + "-cover-" + I18n.getLanguage().toString() + ".jpg";
            }

            String pictureId = String.valueOf(imageTag);

            cloudImageService.exists(pictureId)
                .onFailure(Console::log)
                .onSuccess(exists -> Platform.runLater(() -> {
                    Console.log("exists: " + exists);
                    if (exists) {
                        imageMonoPane.setBackground(null);
                        //First, we need to get the zoom factor of the screen
                        double zoomFactor = Screen.getPrimary().getOutputScaleX();
                        String imageUrl = cloudImageService.url(pictureId, -1, (int) (IMAGE_HEIGHT * zoomFactor));
                        imageView.setFitHeight(IMAGE_HEIGHT);
                        imageView.setPreserveRatio(true);
                        Image imageToDisplay = new Image(imageUrl, true);
                        imageView.setImage(imageToDisplay);
                        imageMonoPane.getChildren().setAll(imageView);
                    } else {
                        SVGPath videoCoverPath = SvgIcons.createVideoIconPath();
                        imageMonoPane.setBackground(new Background(
                            new BackgroundFill(Color.LIGHTGRAY, null, null)
                        ));
                        imageMonoPane.getChildren().setAll(videoCoverPath);
                        imageMonoPane.setAlignment(Pos.CENTER);
                    }
                }));
            //We look at the expiration Date on the scheduledItem, if it is null, we look at the expiration date on the event
            LocalDateTime expirationDate = scheduledVideoItem.getExpirationDate();
            if (expirationDate == null)
                expirationDate = scheduledVideoItem.getEvent().getVodExpirationDate();
            if (expirationDate != null) {
                if (LocalDateTime.now().isBefore(expirationDate))
                    I18nControls.bindI18nProperties(videoExpirationLabel, VideosI18nKeys.VideoAvailableUntil, expirationDate.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")));
                else
                    I18nControls.bindI18nProperties(videoExpirationLabel, VideosI18nKeys.VideoExpiredSince, expirationDate.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")));
                videoExpirationLabel.setVisible(true);
            } else {
                videoExpirationLabel.setVisible(false);
            }

            // Create a Player for each Media, and initialize it.
            boolean autoPlay = true;
            playersVBoxContainer.getChildren().clear();
            for (Media mediaEntity : publishedMedias) {
                MultiPlayer currentVideoPlayer = AllPlayers.createAllVideoPlayer();
                currentVideoPlayer.setMedia(currentVideoPlayer.acceptMedia(mediaEntity.getUrl()));
                currentVideoPlayer.setStartOptions(new StartOptionsBuilder()
                    .setAutoplay(autoPlay)
                    .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                    .build());
                Node videoView = currentVideoPlayer.getMediaView();
                playersVBoxContainer.getChildren().add(videoView);
                currentVideoPlayer.play();
                // we autoplay only the first video
                autoPlay = false;
            }
        }
    }
}
