package one.modality.base.client.profile.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

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


    private final static ObjectProperty<Node> profilePanelProperty = new SimpleObjectProperty<>();


    public static ObjectProperty<Node> profilePanelProperty() {
        return profilePanelProperty;
    }

    public static Node getProfilePanel() {
        return profilePanelProperty.get();
    }

    public static void setProfilePanel(Node panel) {
        profilePanelProperty.set(panel);
    }

}
