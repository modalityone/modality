package one.modality.event.frontoffice.eventheader;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.i18n.I18nEntities;

/**
 * @author Bruno Salmon
 */
public class EventTitleHeader extends AbstractEventHeader {

    private final Label eventTitleLabel = Bootstrap.h3(Bootstrap.strong(I18nEntities.newTranslatedEntityLabel(eventProperty())));

    public EventTitleHeader() {
        eventTitleLabel.setAlignment(Pos.CENTER);
        // Additional settings for mobiles (when the title doesn't fit in one line):
        eventTitleLabel.setTextAlignment(TextAlignment.CENTER);
        Controls.setupTextWrapping(eventTitleLabel, true, false);
    }

    @Override
    public Region getView() {
        return eventTitleLabel;
    }

    @Override
    public String getLoadEventFields() {
        return "name,label";
    }
}
