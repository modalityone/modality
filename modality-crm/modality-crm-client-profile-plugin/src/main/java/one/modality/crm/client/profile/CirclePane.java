package one.modality.crm.client.profile;

import dev.webfx.extras.util.pane.ClipPane;
import dev.webfx.extras.util.pane.MonoPane;
import javafx.scene.Node;
import javafx.scene.shape.Circle;

/**
 * @author Bruno Salmon
 */
public class CirclePane extends MonoPane {

    private final Circle clip = new Circle(); { setClip(clip); }

    public CirclePane() {
    }

    public CirclePane(Node content) {
        super(content);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        ClipPane.resizeClip(this, clip);
    }
}
