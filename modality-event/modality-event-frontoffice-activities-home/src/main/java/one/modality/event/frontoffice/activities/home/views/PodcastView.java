package one.modality.event.frontoffice.activities.home.views;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.frontoffice.entities.Podcast;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import javafx.scene.Node;

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
        System.out.println(podcast.title);
        Text t = TextUtility.getMainText(podcast.title.toUpperCase(), StyleUtility.MAIN_BLUE);

        Text c = TextUtility.getMainText(podcast.excerpt, StyleUtility.VICTOR_BATTLE_BLACK);

        Image img = new Image(podcast.image.replace("\\", ""), true);
        ImageView imgV = new ImageView(img);

        imgV.setPreserveRatio(true);
        FXProperties.runNowAndOnPropertiesChange(() -> imgV.setFitWidth(100* FXApp.fontRatio.get()), FXApp.fontRatio);

        MediaPlayer player = new MediaPlayer(new Media(podcast.link.replace("\\", "")));

        Button play = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Play", 9);
        Button forward = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "--> 30s", 9);
        Button backward = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "<-- 10s>", 9);

        ProgressBar bar = new ProgressBar();
        Text duration = TextUtility.getSubText("", StyleUtility.ELEMENT_GRAY);
        Text current = TextUtility.getSubText("", StyleUtility.ELEMENT_GRAY);

        player.getMedia().durationProperty().addListener(e -> duration.setText("/" + Math.round(player.getMedia().durationProperty().get().toSeconds())));
        player.currentTimeProperty().addListener(e -> current.setText(String.valueOf(Math.round(player.currentTimeProperty().get().toSeconds()))));

        javafx.scene.Node barContainer = GeneralUtility.createHList(5,0, bar, current, duration);

        bindProgress(player, bar);
        addSeekBehavior(player, bar);

        play.setOnAction(e -> {
            if (!player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                if (FXHome.player != null) FXHome.player.pause();
                FXHome.player = player;
                player.play();
            } else {
                player.pause();
            }
        });

        forward.setOnAction(e -> {
            player.seek(player.getCurrentTime().add(Duration.seconds(30)));
        });

        backward.setOnAction(e -> {
            player.seek(player.getCurrentTime().subtract(Duration.seconds(10)));
        });

        player.statusProperty().addListener(change -> {
            if (player.getStatus().equals(MediaPlayer.Status.PLAYING)) play.setText("Paused");
            else play.setText("Play");
        });

        VBox vList = GeneralUtility.createVList(5, 0,
                t,
                barContainer,
                GeneralUtility.createHList(10, 0, play, backward, forward));

        vList.setMinWidth(0);
        HBox.setHgrow(vList, Priority.ALWAYS);
        vList.widthProperty().addListener((observableValue, number, width) -> {
            double wrappingWidth = width.doubleValue() - 10;
            t.setWrappingWidth(wrappingWidth);
            c.setWrappingWidth(wrappingWidth);
        });

        HBox hList = GeneralUtility.createHList(10, 0, imgV, vList);
        VBox podcastBanner = GeneralUtility.createVList(10, 0,
                hList,
                c, GeneralUtility.createSpace(20));
        hList.maxWidthProperty().bind(page.widthProperty());

        return podcastBanner;
    }
}
