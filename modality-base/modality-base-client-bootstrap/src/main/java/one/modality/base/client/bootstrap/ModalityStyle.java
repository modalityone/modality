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

    // Admin module semantic badge styles
    String BADGE_OPERATION = "badge-operation";
    String BADGE_OPERATION_GROUP = "badge-operation-group";
    String BADGE_RULE = "badge-rule";
    String BADGE_ROLE = "badge-role";
    String BADGE_USER = "badge-user";

    static <N extends Node> N blackButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_BLACK);
    }
    static <N extends Node> N whiteButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_WHITE);
    }

    // Admin module semantic badges
    static <N extends Node> N badgeOperation(N badge) {
        return Bootstrap.style(badge, BADGE_OPERATION);
    }

    static <N extends Node> N badgeOperationGroup(N badge) {
        return Bootstrap.style(badge, BADGE_OPERATION_GROUP);
    }

    static <N extends Node> N badgeRule(N badge) {
        return Bootstrap.style(badge, BADGE_RULE);
    }

    static <N extends Node> N badgeRole(N badge) {
        return Bootstrap.style(badge, BADGE_ROLE);
    }

    static <N extends Node> N badgeUser(N badge) {
        return Bootstrap.style(badge, BADGE_USER);
    }

}
