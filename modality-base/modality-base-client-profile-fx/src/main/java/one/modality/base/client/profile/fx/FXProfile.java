package one.modality.base.client.profile.fx;

import dev.webfx.extras.materialdesign.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bruno Salmon
 */
public final class FXProfile {

    private final static ObjectProperty<Node> profileButtonProperty = new SimpleObjectProperty<>();


    public static ObjectProperty<Node> profileButtonProperty() {
        return profileButtonProperty;
    }

    public static Node getProfileButton() {
        return profileButtonProperty.get();
    }

    public static void setProfileButton(Node button) {
        profileButtonProperty.set(button);
    }

    private final static Set<Scene> LISTENED_SCENES = new HashSet<>();
    private final static ObjectProperty<Node> profilePanelProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            SceneUtil.onSceneReady(get(), scene -> {
                if (!LISTENED_SCENES.contains(scene)) {
                    scene.focusOwnerProperty().addListener(observable -> hideProfilePanelIfNodeOutsideProfile(scene.getFocusOwner()));
                    // TODO: implement scene.addEventFilter() in WebFX Kit.
                    scene.getRoot().addEventFilter(MouseEvent.MOUSE_CLICKED, me -> hideProfilePanelIfNodeOutsideProfile(me.getPickResult().getIntersectedNode()));
                    LISTENED_SCENES.add(scene);
                }
            });
        }
    };

    private static void hideProfilePanelIfNodeOutsideProfile(Node node) {
        if (node != null && getProfilePanel() != null) {
            // Does the new focus owner belong to the profile panel?
            if (SceneUtil.hasAncestor(node, getProfilePanel()) || SceneUtil.hasAncestor(node, getProfileButton()))
                return;
            // If not, we hide the profile panel
            setProfilePanel(null);
        }
    }


    public static ObjectProperty<Node> profilePanelProperty() {
        return profilePanelProperty;
    }

    public static Node getProfilePanel() {
        return profilePanelProperty.get();
    }

    public static void setProfilePanel(Node panel) {
        profilePanelProperty.set(panel);
    }

    static {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (!FXLoggedIn.isLoggedIn()) {
                setProfileButton(null);
                setProfilePanel(null);
            }
        }, FXLoggedIn.loggedInProperty());
    }

}
