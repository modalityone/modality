package one.modality.base.frontoffice.utility;

import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Function;

public class TextUtility {
    public static Text getText(String content, double size, Color color) {
        Text t = new Text(content);
        t.setFont(Font.font(t.getFont().getFamily(), FontWeight.findByWeight(500), size));
        t.setFill(color);

        return t;
    }

    public static Text getBindedText(StringProperty property, Function<String, Text> textFunction) {
        Text t = textFunction.apply("");
        t.textProperty().bind(property);
        return t;
    }

    public static Text getMainText(String content) {
        return getText(content, StyleUtility.MAIN_TEXT_SIZE, Color.BLACK);
    }

    public static Text getAccountHeaderText(String content) {
        return weight(getText(content, 20, Color.BLACK), FontWeight.findByWeight(600));
    }

    public static Text getMainHeaderText(String content) {
        return weight(getText(content, 21, Color.web(StyleUtility.MAIN_BLUE)), FontWeight.findByWeight(600));
    }

    public static Text getSubText(String content) {
        return getText(content, 9, Color.web(StyleUtility.ELEMENT_GRAY));
    }

    public static Text getNameText(String content) {
        return getText(content, 30, Color.BLACK);
    }

    private static Text weight(Text t, FontWeight weight) {
        t.setFont(Font.font(t.getFont().getFamily(), weight, t.getFont().getSize()));
        return t;
    }

    public static Text getSettingSectionText(String content) {
        return weight(getText(content, StyleUtility.MAIN_TEXT_SIZE, Color.BLACK), FontWeight.BOLD);
    }
}
