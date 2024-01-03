package one.modality.base.frontoffice.utility;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.fx.FXApp;

import java.util.function.Function;

public class TextUtility {

    public static Text getText(String content, String color, double size) {
        return getText(content, Color.web(color), size);
    }

    public static Text getText(String content, Color color, double size) {
        Text text = createText(content, color);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            setTextFont(text, StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(500), size * FXApp.fontRatio.get());
        }, FXApp.fontRatio);

        return text;
    }

    public static Text createText(Color color) {
        return createText(null, color);
    }

    public static Text createText(String i18nKey, Color color) {
        Text text = new Text();
        if (i18nKey != null)
            I18n.bindI18nProperties(text, i18nKey);
        text.setFill(color);
        return text;
    }

    public static Text createText(String i18nKey, Color color, double size) {
        Text text = createText(i18nKey, color);
        setTextFont(text, StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(500), size);
        return text;
    }

    /*public static void setFontFamily(Text text, String family, double size) {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            setTextFont(text, family, FontWeight.NORMAL, size * FXApp.fontRatio.get());
        }, FXApp.fontRatio);
    }*/

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
        return getText(content, color, StyleUtility.MAIN_TEXT_SIZE);
    }

    /*public static Text getSubText(String content, String color) {
        return getText(content, StyleUtility.SUB_TEXT_SIZE, color);
    }*/

    public static Text getAccountHeaderText(String content) {
        return weight(getText(content, StyleUtility.BLACK, 20), FontWeight.findByWeight(600));
    }

    public static Text getSubText(String content) {
        return getText(content, StyleUtility.ELEMENT_GRAY, 9);
    }

    public static Text getNameText(String content) {
        return getText(content, StyleUtility.BLACK, 30);
    }

    public static Text weight(Text text, FontWeight weight) {
        setTextFont(text, StyleUtility.TEXT_FAMILY, weight, text.getFont().getSize());
        return text;
    }

    public static Text getSettingSectionText(String content) {
        return weight(getText(content, StyleUtility.BLACK, StyleUtility.MAIN_TEXT_SIZE), FontWeight.BOLD);
    }
}
