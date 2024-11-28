package one.modality.base.frontoffice.activities.mainframe;

import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
final class ButtonSegment<T> {

    private final Node graphic;
    private final T state;

    public ButtonSegment(Node graphic, T state) {
        this.graphic = graphic;
        this.state = state;
    }

    public Node getGraphic() {
        return graphic;
    }

    public T getState() {
        return state;
    }

}
