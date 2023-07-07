package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;

public class EventView {
    Event event;

    public EventView(Event event) {
        this.event = event;
    }

    public Node getView(Double distance) {
        Object language = I18n.getLanguage();
        String titleStr = (event.getLabel() != null) ? event.getLabel().getStringFieldValue(language) : event.getName();
        titleStr = (titleStr == null) ? event.getName() : titleStr;
        Text title = TextUtility.getMediumText(titleStr, StyleUtility.MAIN_BLUE);
        Text subTitle = TextUtility.getText("LOWER DESCRIPTION", 10, StyleUtility.VICTOR_BATTLE_BLACK);
        Text date = TextUtility.getText(event.getStartDate().toString(), 10, StyleUtility.VICTOR_BATTLE_BLACK);
        Node location = GeneralUtility.createVList(0, 0,
                TextUtility.weight(TextUtility.getText("At Manjushri Kadampa Meditation Center", 8, StyleUtility.ELEMENT_GRAY), FontWeight.THIN),
                TextUtility.weight(TextUtility.getText("Ulverston, United Kingdom", 8, StyleUtility.ELEMENT_GRAY), FontWeight.MEDIUM),
                distance < 0 ? new VBox() : TextUtility.getText(distance.toString(), 8, StyleUtility.ELEMENT_GRAY)
        );
        Button book = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Book now", 9);

        VBox container = new VBox(10);

        container.getChildren().addAll(
                GeneralUtility.createSplitRow(title, date, 80, 0),
                GeneralUtility.createSplitRow(subTitle, new Text(""), 80, 0),
                GeneralUtility.createSplitRow(location, book, 80, 0)
        );

        container.setPadding(new Insets(20));
        container.setBackground(Background.fill(Color.web(StyleUtility.BACKGROUND_GRAY)));
        FXProperties.runNowAndOnPropertiesChange(() -> title.setWrappingWidth(container.getWidth() * 0.75), container.widthProperty());

        return container;
    }
}
