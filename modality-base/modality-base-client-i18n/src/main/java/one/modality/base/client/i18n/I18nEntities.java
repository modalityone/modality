package one.modality.base.client.i18n;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.lciimpl.EntityDomainReader;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import one.modality.base.client.entities.functions.I18nFunction;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class I18nEntities {

    public static Object expressionKey(Entity entity, String expression) {
        return expressionKey((Object) entity, expression);
    }

    public static Object expressionKey(ObservableValue<? extends Entity> entityProperty, String expression) {
        return expressionKey((Object) entityProperty, expression);
    }

    private static Object expressionKey(Object entityOrProperty, String expression) {
        return new I18nSubKey("expression: " + expression, entityOrProperty);
    }

    public static void bindExpressionTextProperty(Property<String> textProperty, Entity entity, String expression, Object... args) {
        I18n.bindI18nTextProperty(textProperty, expressionKey(entity, expression), args);
    }

    public static void bindExpressionTextProperty(Property<String> textProperty, ObservableValue<? extends Entity> entityProperty, String expression, Object... args) {
        I18n.bindI18nTextProperty(textProperty, expressionKey(entityProperty, expression), argsAndEntityProperty(args, entityProperty));
    }

    public static <T extends Text> T bindExpressionProperties(T text, ObservableValue<? extends Entity> entityProperty, String expression, Object... args) {
        return I18n.bindI18nProperties(text, expressionKey(entityProperty, expression), entityProperty, argsAndEntityProperty(args, entityProperty));
    }

    public static <T extends Text> T bindExpressionProperties(T text, Entity entity, String expression, Object... args) {
        return I18n.bindI18nProperties(text, expressionKey(entity, expression), args);
    }

    public static <T extends Labeled> T bindExpressionProperties(T labeled, Entity entity, String expression, Object... args) {
        return I18nControls.bindI18nProperties(labeled, expressionKey(entity, expression), args);
    }

    public static <T extends Labeled> T bindExpressionProperties(T labeled, ObservableValue<? extends Entity> entityProperty, String expression, Object... args) {
        return I18nControls.bindI18nProperties(labeled, expressionKey(entityProperty, expression), argsAndEntityProperty(args, entityProperty));
    }

    public static <T extends Labeled> T bindExpressionTextProperty(T labeled, Entity entity, String expression, Object... args) {
        return I18nControls.bindI18nTextProperty(labeled, expressionKey(entity, expression), args);
    }

    public static Text newExpressionText(Entity entity, String expression, Object... args) {
        return bindExpressionProperties(new Text(), entity, expression, args);
    }

    public static Text newExpressionText(ObservableValue<? extends Entity> entityProperty, String expression, Object... args) {
        return bindExpressionProperties(new Text(), entityProperty, expression, args);
    }

    public static Label newExpressionLabel(Entity entity, String expression, Object... args) {
        return bindExpressionProperties(new Label(), entity, expression, args);
    }

    public static Label newExpressionLabel(ObservableValue<? extends Entity> entityProperty, String expression) {
        return bindExpressionProperties(new Label(), entityProperty, expression);
    }

    public static String translateEntity(Entity entity) {
        return translateEntity(entity, null);
    }

    public static String translateEntity(Entity entity, Object language) {
        if (entity == null)
            return null;
        return (String) I18nFunction.evaluate(entity, language, new EntityDomainReader<>(entity.getStore()));
    }

    private static Object[] argsAndEntityProperty(Object[] args, ObservableValue<? extends Entity> entityProperty) {
        return Arrays.add(Object[]::new, args, entityProperty);
    }

    public static String convertAudioLanguageCodeToWrittenLanguageCode(String code) {
        if(Objects.equals(code, "zhma")) {
            //Mandarin use mainly simplified chinese
            return "zhs";
        }
        if(Objects.equals(code, "zhca")) {
            //Cantonese use mainly traditional chinese
            return "zht";
        }
        return code;
    }
}
