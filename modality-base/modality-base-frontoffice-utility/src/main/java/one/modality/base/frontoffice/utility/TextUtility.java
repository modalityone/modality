package one.modality.base.frontoffice.utility;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.fx.FXApp;

import java.util.function.Function;

public class TextUtility {

    public static Text getText(String content, double size, String color) {
        Text t = new Text(escapeHtmlUnicode(content));
        t.setFill(Color.web(color));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            double fontSize = size * FXApp.fontRatio.get();
            t.setFont(Font.font(StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(500), fontSize));
            t.setStyle("-fx-font-family: " + StyleUtility.TEXT_FAMILY + "; -fx-font-size: " + fontSize);
        }, FXApp.fontRatio);

        return t;
    }

    public static String escapeHtmlUnicode(String t) {
        // https://oinam.github.io/entities/

        t = t.replace("&#160;", " ");
        t = t.replace("&#60;", "<");
        t = t.replace("&#62;", "<");
        t = t.replace("&#38;", "&");
        t = t.replace("&#34;", "\"");
        t = t.replace("&#39;", "'");
        t = t.replace("&#162;", "¢");
        t = t.replace("&#163;", "£");
        t = t.replace("&#165;", "¥");
        t = t.replace("&#8364;", "€");
        t = t.replace("&#169;", "©");
        t = t.replace("&#174;", "®");
        t = t.replace("&#8211;", "-");

        return t;
    }

    public static void setFontFamily(Text t, String family, int size) {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double fontSize = size * FXApp.fontRatio.get();
            t.setFont(Font.font(family));
            t.setStyle("-fx-font-family: " + family);
        }, FXApp.fontRatio);
    }

    public static Text getBindedText(StringProperty property, Function<String, Text> textFunction) {
        Text t = textFunction.apply("");
        t.textProperty().bind(property);
        return t;
    }

    public static Text getMainText(String content, String color) {
        return getText(content, StyleUtility.MAIN_TEXT_SIZE, color);
    }

    public static Text getMediumText(String content, String color) {
        return getText(content, StyleUtility.MEDIUM_TEXT_SIZE, color);
    }

    public static Text getSubText(String content, String color) {
        return getText(content, StyleUtility.SUB_TEXT_SIZE, color);
    }

    public static Text getAccountHeaderText(String content) {
        return weight(getText(content, 20, StyleUtility.VICTOR_BATTLE_BLACK), FontWeight.findByWeight(600));
    }

    public static Text getMainHeaderText(String content) {
        return weight(getText(content, 21, StyleUtility.MAIN_BLUE), FontWeight.findByWeight(600));
    }

    public static Text getSubText(String content) {
        return getText(content, 9, StyleUtility.ELEMENT_GRAY);
    }

    public static Text getNameText(String content) {
        return getText(content, 30, StyleUtility.VICTOR_BATTLE_BLACK);
    }

    public static Text weight(Text t, FontWeight weight) {
        t.setFont(Font.font(StyleUtility.TEXT_FAMILY, weight, t.getFont().getSize()));
        return t;
    }

    public static Text getSettingSectionText(String content) {
        return weight(getText(content, StyleUtility.MAIN_TEXT_SIZE, StyleUtility.VICTOR_BATTLE_BLACK), FontWeight.BOLD);
    }
}
