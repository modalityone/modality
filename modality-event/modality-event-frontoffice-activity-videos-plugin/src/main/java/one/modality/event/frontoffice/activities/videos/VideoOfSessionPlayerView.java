package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import one.modality.base.client.icons.SvgIcons;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
final class VideoOfSessionPlayerView {

    private static final double PLAYER_HEIGHT = 500;
    private static final double PLAYER_WIDTH = PLAYER_HEIGHT * 1.511;

    private final String title;
    private final String url;
    private final Node parent;
    private final Consumer<Node> nodeShower;

    private final VBox container = new VBox();

    public VideoOfSessionPlayerView(String title, String url, Node parent, Consumer<Node> nodeShower) {
        this.title = title;
        this.url = url;
        this.parent = parent;
        this.nodeShower = nodeShower;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    private void buildUi() {
        Label titleLabel = Bootstrap.strong(new Label(title));
        titleLabel.setWrapText(true);
        HBox firstLine = new HBox(titleLabel);

        // Load the video player URL
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(url);

        if (parent != null) {
            MonoPane backArrowPane = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), () -> {
                webEngine.load(null);
                nodeShower.accept(parent);
            });

            firstLine.getChildren().setAll(backArrowPane, titleLabel);
            firstLine.setSpacing(40);
            firstLine.setAlignment(Pos.CENTER_LEFT);
        }

        container.setSpacing(40);
        container.setMaxWidth(PLAYER_WIDTH);
        container.setMaxHeight(PLAYER_HEIGHT);
        container.getChildren().addAll(
            firstLine,
            webView
        );
    }
}
