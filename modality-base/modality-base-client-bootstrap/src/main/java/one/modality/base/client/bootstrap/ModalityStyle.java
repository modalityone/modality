package one.modality.base.client.bootstrap;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.scene.Node;


public interface ModalityStyle {

    String BTN_BLACK = "btn-black";
    String BTN_WHITE = "btn-white";
    String TEXT_COMMENT = "comment";

    static <N extends Node> N blackButton(N button) {
        return dev.webfx.extras.styles.bootstrap.Bootstrap.style(Bootstrap.button(button), BTN_BLACK);
    }
}
