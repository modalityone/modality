package one.modality.base.client.bootstrap;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.scene.Node;

public interface ModalityStyle {
    static <N extends Node> N h1Primary(N node) {
        return Bootstrap.style(Bootstrap.style(node,Bootstrap.TEXT_PRIMARY),Bootstrap.H1);
    }
    static <N extends Node> N h2Primary(N node) {
        return Bootstrap.style(Bootstrap.style(node,Bootstrap.TEXT_PRIMARY),Bootstrap.H2);
    }
    static <N extends Node> N textSuccess(N node) {
        return Bootstrap.style(node,Bootstrap.TEXT_SUCCESS);
    }
    static <N extends Node> N textDanger(N node) {
        return Bootstrap.style(node,Bootstrap.TEXT_DANGER);
    }

    static String primaryColor() {
        return "0096D6";
    }

    static String successColor() {return "41BA4D";}
}
