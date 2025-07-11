package one.modality.event.frontoffice.eventheader;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import one.modality.base.client.i18n.I18nEntities;

/**
 * @author Bruno Salmon
 */
public class EventTitleHeader extends AbstractEventHeader {

    private final Label eventTitleLabel = Bootstrap.h3(Bootstrap.strong(I18nEntities.newExpressionLabel(eventProperty(), "i18n(this)")));

    public EventTitleHeader() {
        eventTitleLabel.setAlignment(Pos.CENTER);
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
