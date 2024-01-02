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
        Text text = new Text(content);
        text.setFill(Color.web(color));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            setTextFont(text, StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(500), size * FXApp.fontRatio.get());
        }, FXApp.fontRatio);

        return text;
    }

    public static void setFontFamily(Text text, String family, double size) {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            setTextFont(text, family, FontWeight.NORMAL, size * FXApp.fontRatio.get());
        }, FXApp.fontRatio);
    }

    public static void setTextFont(Text text, String fontFamily, FontWeight fontWeight, double fontSize) {
        text.setFont(Font.font(fontFamily, fontWeight, fontSize));
        GeneralUtility.setNodeFontStyle(text, fontFamily, fontWeight, fontSize);
    }

    public static Text getBindedText(StringProperty property, Function<String, Text> textFunction) {
        Text text = textFunction.apply("");
        text.textProperty().bind(property);
        return text;
    }

    public static Text getMainText(String content, String color) {
        return getText(content, StyleUtility.MAIN_TEXT_SIZE, color);
    }

    public static Text getSubText(String content, String color) {
        return getText(content, StyleUtility.SUB_TEXT_SIZE, color);
    }

    public static Text getAccountHeaderText(String content) {
        return weight(getText(content, 20, StyleUtility.BLACK), FontWeight.findByWeight(600));
    }

    public static Text getSubText(String content) {
        return getText(content, 9, StyleUtility.ELEMENT_GRAY);
    }

    public static Text getNameText(String content) {
        return getText(content, 30, StyleUtility.BLACK);
    }

    public static Text weight(Text text, FontWeight weight) {
        setTextFont(text, StyleUtility.TEXT_FAMILY, weight, text.getFont().getSize());
        return text;
    }

    public static Text getSettingSectionText(String content) {
        return weight(getText(content, StyleUtility.MAIN_TEXT_SIZE, StyleUtility.BLACK), FontWeight.BOLD);
    }
}
