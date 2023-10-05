package one.modality.base.client.profile.fx;

import dev.webfx.extras.materialdesign.util.scene.SceneUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

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
            hideProfilePanel();
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


    private final static BooleanProperty showProfilePanelProperty = new SimpleBooleanProperty(false);

    public static BooleanProperty showProfilePanelProperty() {
        return showProfilePanelProperty;
    }

    public static boolean isProfilePanelShown() {
        return showProfilePanelProperty.get();
    }

    public static void setShowProfilePanel(boolean showProfilePanel) {
        showProfilePanelProperty.set(showProfilePanel);
    }

    public static void showProfilePanel() {
        setShowProfilePanel(true);
    }

    public static void hideProfilePanel() {
        setShowProfilePanel(false);
    }

    public static void toggleShowProfilePanel() {
        setShowProfilePanel(!isProfilePanelShown());
    }

    private final static ObjectProperty<Supplier<Node>> profileButtonFactoryProperty = new SimpleObjectProperty<>();
    private final static ObjectProperty<Supplier<Node>> profilePanelFactoryProperty = new SimpleObjectProperty<>();


    public static void setProfileButtonFactory(Supplier<Node> profileButtonFactory) {
        profileButtonFactoryProperty.set(profileButtonFactory);
    }

    public static void setProfilePanelFactory(Supplier<Node> profilePanelFactory) {
        profilePanelFactoryProperty.set(profilePanelFactory);
    }

    static {
        // Managing profile button & panel creation, destruction, and visibility
        WebFxKitLauncher.onReady(() -> FXProperties.runNowAndOnPropertiesChange(() -> {
            Object userId = FXLoggedIn.isLoggedIn() ? FXUserId.getUserId() : null;
            if (userId == null)
                hideProfilePanel();
            Supplier<Node> profileButtonFactory = profileButtonFactoryProperty.get();
            FXProfile.setProfileButton(userId == null || profileButtonFactory == null ? null : profileButtonFactory.get());
            Supplier<Node> profilePanelFactory = profilePanelFactoryProperty.get();
            FXProfile.setProfilePanel(userId == null || profilePanelFactory == null ? null : profilePanelFactory.get());
        }, FXUserId.userIdProperty(), FXLoggedIn.loggedInProperty(), profileButtonFactoryProperty, profilePanelFactoryProperty));
    }

}
