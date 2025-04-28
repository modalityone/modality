package one.modality.base.client.bootstrap;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.scene.Node;

/**
 * @author David Hello
 */
public interface ModalityStyle {

    String BTN_BLACK = "btn-black";
    String TEXT_COMMENT = "comment";

    static <N extends Node> N blackButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_BLACK);
    }
}
