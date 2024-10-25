package one.modality.event.frontoffice.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public final class EventThumbnailView {

    private final Event event;

    private final VBox container = new VBox();

    public EventThumbnailView(Event event) {
        this.event = event;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    private void buildUi() {
        container.setMinHeight(100);
        container.setBorder(BorderFactory.newBorder(Color.LIGHTGRAY, 2));

        Label eventLabel = Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), new I18nSubKey("expression: i18n(this)", event)));
        VBox.setMargin(eventLabel, new Insets(15));

        Label shortDescriptionLabel = new Label(event.getShortDescription());
        VBox.setMargin(shortDescriptionLabel, new Insets(15));

        container.getChildren().setAll(
            eventLabel,
            shortDescriptionLabel
        );
    }
}
