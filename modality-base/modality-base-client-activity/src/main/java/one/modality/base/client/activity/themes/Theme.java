package one.modality.base.client.activity.themes;

import dev.webfx.stack.ui.controls.dialog.DialogUtil;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Paint;

/**
 * @author Bruno Salmon
 */
public final class Theme {

    private static final Property<Background> mainBackgroundProperty = new SimpleObjectProperty<>();

    public static Property<Background> mainBackgroundProperty() {
        return mainBackgroundProperty;
    }

    static void setMainBackground(Background mainBackground) {
        mainBackgroundProperty.setValue(mainBackground);
    }

    private static final Property<Paint> mainTextFillProperty = new SimpleObjectProperty<>();

    public static Property<Paint> mainTextFillProperty() {
        return mainTextFillProperty;
    }

    static void setMainTextFill(Paint mainTextFill) {
        mainTextFillProperty.setValue(mainTextFill);
    }

    private static final Property<Background> dialogBackgroundProperty =
            new SimpleObjectProperty<>();

    public static Property<Background> dialogBackgroundProperty() {
        return dialogBackgroundProperty;
    }

    static void setDialogBackground(Background dialogBackground) {
        dialogBackgroundProperty.setValue(dialogBackground);
    }

    private static final Property<Border> dialogBorderProperty = new SimpleObjectProperty<>();

    public static Property<Border> dialogBorderProperty() {
        return dialogBorderProperty;
    }

    static void setDialogBorder(Border dialogBorder) {
        dialogBorderProperty.setValue(dialogBorder);
    }

    private static final Property<Paint> dialogTextFillProperty = new SimpleObjectProperty<>();

    public static Property<Paint> dialogTextFillProperty() {
        return dialogTextFillProperty;
    }

    static void setDialogTextFill(Paint dialogTextFill) {
        dialogTextFillProperty.setValue(dialogTextFill);
    }

    static {
        DialogUtil.dialogBackgroundProperty().bind(dialogBackgroundProperty);
        DialogUtil.dialogBorderProperty().bind(dialogBorderProperty);
        new LightTheme().apply();
    }
}
