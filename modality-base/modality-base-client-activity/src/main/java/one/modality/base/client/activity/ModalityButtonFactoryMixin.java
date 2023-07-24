package one.modality.base.client.activity;

import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.controls.button.ButtonBuilder;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;

import javafx.scene.control.Button;

import one.modality.base.client.activity.themes.Theme;

/**
 * @author Bruno Salmon
 */
public interface ModalityButtonFactoryMixin extends ButtonFactoryMixin, MaterialFactoryMixin {

    @Override
    default Button styleButton(Button button) {
        button.textFillProperty().bind(Theme.mainTextFillProperty());
        return button;
    }

    default Button newBookButton() {
        return newBookButtonBuilder().build();
    }

    default ButtonBuilder newBookButtonBuilder() {
        return newColorButtonBuilder("Book>>", "#7fd504", "#2a8236");
    }

    default Button newSoldoutButton() {
        return newSoldoutButtonBuilder().build();
    }

    default ButtonBuilder newSoldoutButtonBuilder() {
        return newColorButtonBuilder("Soldout", "#e92c04", "#853416");
    }
}
