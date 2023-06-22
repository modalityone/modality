package one.modality.base.frontoffice.utility;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextUtility {
    public static Text getText(String content, double size, Color color) {
        Text t = new Text(content);
        t.setFont(Font.font(t.getFont().getFamily(), FontWeight.findByWeight(500), size));
        t.setFill(color);

        return t;
    }

    public static Text getMainText(String content) {
        return getText(content, StyleUtility.MAIN_TEXT_SIZE, Color.BLACK);
    }

    public static Text getSubText(String content) {
        return getText(content, 9, Color.web(StyleUtility.INPUT_BORDER));
    }

    public static Text getNameText(String content) {
        return getText(content, 30, Color.BLACK);
    }

    public static Text getSettingSectionText(String content) {
        Text t = getText(content, StyleUtility.MAIN_TEXT_SIZE, Color.BLACK);
        t.setFont(Font.font(t.getFont().getFamily(), FontWeight.BOLD, t.getFont().getSize()));

        return t;
    }
}
