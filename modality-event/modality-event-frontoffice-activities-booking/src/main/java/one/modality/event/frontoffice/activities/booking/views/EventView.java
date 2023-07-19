package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;

public final class EventView {

    private final Label title = GeneralUtility.getMediumLabel(null, StyleUtility.MAIN_BLUE);
    private final Label subTitle = GeneralUtility.createLabel("LOWER DESCRIPTION", Color.web(StyleUtility.VICTOR_BATTLE_BLACK), 10);
    private final Text date = TextUtility.getText(null, 10, StyleUtility.VICTOR_BATTLE_BLACK);
    private final Node location = GeneralUtility.createVList(0, 0,
            TextUtility.weight(TextUtility.getText("At Manjushri Kadampa Meditation Center", 8, StyleUtility.ELEMENT_GRAY), FontWeight.THIN),
            TextUtility.weight(TextUtility.getText("Ulverston, United Kingdom", 8, StyleUtility.ELEMENT_GRAY), FontWeight.MEDIUM)
            //distance < 0 ? new VBox() : TextUtility.getText(distance.toString(), 8, StyleUtility.ELEMENT_GRAY)
    );
    private final Button book = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Book now", 11);

    private final VBox container = new VBox(10,
            GeneralUtility.createSplitRow(title, date, 80, 0),
            GeneralUtility.createSplitRow(subTitle, new Text(""), 80, 0),
            GeneralUtility.createSplitRow(location, book, 80, 0)
    );

    {
        container.setPadding(new Insets(40));
        container.setBackground(Background.fill(Color.web(StyleUtility.BACKGROUND_GRAY)));
    }

    public void setEvent(Event event) {
        Object language = I18n.getLanguage();
        String titleStr = (event.getLabel() != null) ? event.getLabel().getStringFieldValue(language) : event.getName();
        titleStr = (titleStr == null) ? event.getName() : titleStr;
        title.setText(titleStr);
        date.setText(event.getStartDate().toString());
    }

    public Node getView() {
        return container;
    }
}
