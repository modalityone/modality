package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
abstract class AbstractVideoPlayerActivity extends ViewDomainActivityBase {

    protected final Label sessionTitleLabel = Bootstrap.h4(Bootstrap.strong(new Label()));
    protected final Label sessionCommentLabel = new Label();
    private List<MultiPlayer> sessionVideoPlayers;
    private final MultiPlayer sessionVideoPlayer = AllPlayers.createAllVideoPlayer();
    protected HtmlText eventDescriptionHtmlText = new HtmlText();
    protected Label eventLabel = Bootstrap.h2(Bootstrap.strong(new Label()));
    private final CloudImageService cloudImageService = new ClientImageService();
    private static final int IMAGE_HEIGHT = 240;
    private MonoPane imageMonoPane;
    private ImageView imageView;
    protected VBox playersVBoxContainer;
    protected final ObjectProperty<Object> scheduledVideoItemIdProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    protected final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledVideoItemProperty is enough)
    protected VBox titleVBox;
    protected VBox pageContainer;
    protected HBox headerHBox;
    protected VBox sessionDescriptionVBox;


    @Override
    protected abstract void updateModelFromContextParameters();

    @Override
    protected abstract void startLogic();

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

        headerHBox = new HBox();
        headerHBox.setSpacing(50);
        headerHBox.setPadding(new Insets(0, 20, 0, 20));
        imageMonoPane = new MonoPane();
        imageView = new ImageView();

        headerHBox.getChildren().add(imageMonoPane);
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));

        eventDescriptionHtmlText.managedProperty().bind(FXProperties.compute(eventDescriptionHtmlText.textProperty(), Strings::isNotEmpty));
        eventDescriptionHtmlText.setMaxHeight(60);
        titleVBox = new VBox(eventLabel, eventDescriptionHtmlText);

        headerHBox.getChildren().add(titleVBox);


        // Session title
        sessionDescriptionVBox = new VBox(20);
        sessionDescriptionVBox.setAlignment(Pos.TOP_LEFT);
        sessionTitleLabel.setWrapText(true);
        sessionTitleLabel.setTextAlignment(TextAlignment.CENTER);
        sessionCommentLabel.setWrapText(true);

        sessionDescriptionVBox.getChildren().addAll(sessionTitleLabel, sessionCommentLabel);

        playersVBoxContainer = new VBox(30);

        pageContainer = new VBox(40,
            headerHBox,
            sessionDescriptionVBox,
            playersVBoxContainer);

        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.getStyleClass().add("livestream");


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************
        ScalePane scalePane = new ScalePane(pageContainer);
        scalePane.setVAlignment(VPos.TOP);
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(scalePane);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }

    protected void updateSessionTitleAndVideoPlayerState() {
        syncHeader();
        syncPlayerContent();
    }

    protected abstract void syncHeader();

    protected void updatePicture(Event event) {
        Object imageTag = ModalityCloudinary.getEventCoverImageTag(event.getPrimaryKey().toString(),I18n.getLanguage());
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
    }

    protected abstract void syncPlayerContent();
}
