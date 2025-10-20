package one.modality.event.frontoffice.medias;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View displayed when user has no booked content.
 * Reusable for both video streaming and audio library.
 */
public class NoContentView {
    private final VBox container;

    public NoContentView(String audioOrVideo) {
        Label titleLabel = Bootstrap.h3(I18nControls.newLabel(MediasI18nKeys.NoContentAvailable));
        titleLabel.setContentDisplay(ContentDisplay.TOP);
        titleLabel.setGraphicTextGap(20);
        Label descriptionLabel = I18nControls.newLabel(MediasI18nKeys.NoContentPublished);
        container = new VBox(30, titleLabel, descriptionLabel);
        container.setAlignment(Pos.TOP_CENTER);
    }

    public Node getView() {
        return container;
    }
}
