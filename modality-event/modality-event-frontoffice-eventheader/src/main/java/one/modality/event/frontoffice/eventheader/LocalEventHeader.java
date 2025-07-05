package one.modality.event.frontoffice.eventheader;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.async.Future;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public final class LocalEventHeader extends AbstractEventHeader {

    private static final double MAX_PAGE_WIDTH = FOPageUtil.MAX_PAGE_WIDTH;

    private final MonoPane eventImageContainer = new MonoPane();
    private final Label eventLabel = newEventExpressionLabel("i18n(this)");
    private final HtmlText eventShortDescriptionHtmlText = newEventExpressionHtmlText("'<center>' + shortDescription + '</center>'");
    private final GridPane gridPane = new GridPane();

    public LocalEventHeader() {
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.getStyleClass().add("event-title");
        eventLabel.fontProperty().bind(eventFontProperty());

        Label venueAddress = newEventExpressionLabel("coalesce(venue.address, i18n(venue))");
        venueAddress.setTextAlignment(TextAlignment.CENTER);
        venueAddress.getStyleClass().add("event-title");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(SvgIcons.createPinpointSVGPath());
        venueAddress.fontProperty().bind(descriptionFontProperty());

        eventShortDescriptionHtmlText.fontProperty().bind(descriptionFontProperty());
        eventShortDescriptionHtmlText.getStyleClass().add("event-title");
        eventShortDescriptionHtmlText.setFocusTraversable(false);

        ScalePane imageScalePane = new ScalePane(ScaleMode.BEST_FIT, eventImageContainer);
        imageScalePane.setCanGrow(false);

        VBox eventShortTextBox = new VBox(
            eventLabel,
            venueAddress,
            eventShortDescriptionHtmlText
        );
        eventShortTextBox.setMinWidth(Region.USE_PREF_SIZE);
        ScalePane eventShortTextScalePane = new ScalePane(ScaleMode.BEST_FIT, eventShortTextBox);
        eventShortTextBox.setAlignment(Pos.CENTER);

        gridPane.setHgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(33);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(66);
        gridPane.getColumnConstraints().addAll(c1, c2);
        gridPane.add(imageScalePane, 0, 0);
        gridPane.add(eventShortTextScalePane, 1, 0);
        GridPane.setHalignment(imageScalePane, HPos.LEFT);
        GridPane.setValignment(eventShortTextScalePane, VPos.CENTER);
    }

    @Override
    public String getLoadEventFields() {
        return "name, label, shortDescription, shortDescriptionLabel, description, venue.(name, label, address), organization.country";
    }

    public HtmlText newEventExpressionHtmlText(String eventExpression) {
        HtmlText text = new HtmlText();
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    public Label newEventExpressionLabel(String eventExpression) {
        Label label = new Label();
        bindI18nEventExpression(label.textProperty(), eventExpression);
        return label;
    }

    private void bindI18nEventExpression(Property<String> textProperty, String eventExpression) {
        I18nEntities.bindExpressionTextProperty(textProperty, eventProperty(), eventExpression, eventLoadedProperty());
    }

    @Override
    public Region getView() {
        return gridPane;
    }

    @Override
    public Future<Event> loadAndSetEvent(Event event) {
        //eventImageContainer.setContent(null);
        String cloudImagePath = ModalityCloudinary.eventImagePath(event);
        ModalityCloudinary.loadImage(cloudImagePath, eventImageContainer, -1, -1, null);
        return super.loadAndSetEvent(event);
    }

    @Override
    public void setMaxPageWidth(double maxPageWidth) {
        VBox.setMargin(eventLabel, new Insets(0, 0, 5 * maxPageWidth / MAX_PAGE_WIDTH, 0));
        VBox.setMargin(eventShortDescriptionHtmlText, new Insets(20 * maxPageWidth / MAX_PAGE_WIDTH, 0, 0, 0));
        eventShortDescriptionHtmlText.setMaxWidth(maxPageWidth * 0.6);
        super.setMaxPageWidth(maxPageWidth);
    }
}
