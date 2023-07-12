package one.modality.event.frontoffice.activities.home.views;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Podcast;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class PodcastView {
    private Podcast podcast;

    public PodcastView(Podcast podcast) {
        this.podcast = podcast;
    }

    private boolean isValidDuration(Duration d) {
        return d != null && !d.isIndefinite() && !d.isUnknown();
    }
    private void bindProgress(MediaPlayer player, ProgressBar bar) {
        var binding =
                Bindings.createDoubleBinding(
                        () -> {
                            var currentTime = player.getCurrentTime();
                            var duration = player.getMedia().getDuration();
                            if (isValidDuration(currentTime) && isValidDuration(duration)) {
                                return currentTime.toMillis() / duration.toMillis();
                            }
                            return ProgressBar.INDETERMINATE_PROGRESS;
                        },
                        player.currentTimeProperty(),
                        player.getMedia().durationProperty());

        bar.progressProperty().bind(binding);
    }

    private void addSeekBehavior(MediaPlayer player, ProgressBar bar) {
        EventHandler<MouseEvent> onClickAndOnDragHandler =
                e -> {
                    var duration = player.getMedia().getDuration();
                    if (isValidDuration(duration)) {
                        var seekTime = duration.multiply(e.getX() / bar.getWidth());
                        player.seek(seekTime);
                        e.consume();
                    }
                };
        bar.addEventHandler(MouseEvent.MOUSE_CLICKED, onClickAndOnDragHandler);
        bar.addEventHandler(MouseEvent.MOUSE_DRAGGED, onClickAndOnDragHandler);
    }

    public Node getView(VBox page) {
        System.out.println(podcast.getTitle());
        Text t = TextUtility.getMainText(podcast.getTitle().toUpperCase(), StyleUtility.MAIN_BLUE);
        String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(podcast.getDate());
        Text d = TextUtility.getSubText(date);
        Text c = TextUtility.getMainText(podcast.getExcerpt(), StyleUtility.VICTOR_BATTLE_BLACK);

        Image img = new Image(podcast.getImageUrl().replace("\\", ""), true);
        ImageView imgV = new ImageView(img);
        GeneralUtility.roundClipImageView(imgV);

        imgV.setPreserveRatio(true);

        FXProperties.runNowAndOnPropertiesChange(() -> imgV.setFitWidth(100* FXApp.fontRatio.get()), FXApp.fontRatio);

        MediaPlayer player = new MediaPlayer(new Media(podcast.getAudioUrl().replace("\\", "")));

        StackPane playHolder = new StackPane();
        Pane play = PodcastButtons.createPlayButton();
        Pane pause = PodcastButtons.createPauseButton();

        playHolder.getChildren().addAll(play, pause);

        Pane forward = PodcastButtons.createForwardButton();
        Pane backward = PodcastButtons.createBackwardButton();

        ProgressBar bar = new ProgressBar();
        Text duration = TextUtility.getSubText("", StyleUtility.ELEMENT_GRAY);
        Text current = TextUtility.getSubText("00:00", StyleUtility.ELEMENT_GRAY);

        TextUtility.setFontFamily(duration, StyleUtility.CLOCK_FAMILY, 9);
        TextUtility.setFontFamily(current, StyleUtility.CLOCK_FAMILY, 9);

        player.getMedia().durationProperty().addListener(e -> {
            int secondsTotal = (int) Math.floor(player.getMedia().durationProperty().get().toSeconds());
            int minutesTotal = (int) Math.floor(player.getMedia().durationProperty().get().toMinutes());
            duration.setText(String.format("/ %02d:%02d", minutesTotal, (secondsTotal - minutesTotal*60)));
        });

        player.currentTimeProperty().addListener(e -> {
            int secondsTotal = (int) Math.floor(player.currentTimeProperty().get().toSeconds());
            int minutesTotal = (int) Math.floor(player.currentTimeProperty().get().toMinutes());
            current.setText(String.format("%02d:%02d", minutesTotal, (secondsTotal - minutesTotal*60)));
        });

        javafx.scene.Node barContainer = GeneralUtility.createVList(5,0, bar,
                GeneralUtility.createHList(5, 0, current, duration)
        );

        bindProgress(player, bar);
        addSeekBehavior(player, bar);

        playHolder.setOnMouseClicked(e -> {
            if (!player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                if (FXHome.player != null) FXHome.player.pause();
                FXHome.player = player;
                player.play();
            } else {
                player.pause();
            }
        });

        forward.setOnMouseClicked(e -> {
            player.seek(player.getCurrentTime().add(Duration.seconds(30)));
        });

        backward.setOnMouseClicked(e -> {
            player.seek(player.getCurrentTime().subtract(Duration.seconds(10)));
        });

        player.statusProperty().addListener(change -> {
            boolean isPlay = player.getStatus().equals(MediaPlayer.Status.PLAYING);
            pause.setVisible(isPlay);
            play.setVisible(!isPlay);
        });

        BorderPane borderPane = new BorderPane();

        borderPane.setTop(GeneralUtility.createVList(5, 0, d, t));
        borderPane.setBottom(GeneralUtility.createHList(10, 0, backward, playHolder, forward, barContainer));

        borderPane.setMinWidth(0);
        HBox.setHgrow(borderPane, Priority.ALWAYS);
        borderPane.widthProperty().addListener((observableValue, number, width) -> {
            double wrappingWidth = width.doubleValue() - 10;
            t.setWrappingWidth(wrappingWidth);
            c.setWrappingWidth(wrappingWidth);

            bar.setMinWidth(width.doubleValue()*0.5);
        });

        HBox hList = GeneralUtility.createHList(10, 0, imgV, borderPane);
        VBox podcastBanner = GeneralUtility.createVList(10, 0,
                hList,
                GeneralUtility.createSpace(20));
        hList.maxWidthProperty().bind(page.widthProperty());

        return podcastBanner;
    }
}
