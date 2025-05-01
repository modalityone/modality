package one.modality.base.client.bootstrap;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.scene.Node;

/**
 * @author David Hello
 */
public interface ModalityStyle {

    String BTN_BLACK = "btn-black";
    String BTN_WHITE = "btn-white";
    String TEXT_COMMENT = "comment";

    static <N extends Node> N blackButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_BLACK);
    }
    static <N extends Node> N whiteButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_WHITE);
    }

}
