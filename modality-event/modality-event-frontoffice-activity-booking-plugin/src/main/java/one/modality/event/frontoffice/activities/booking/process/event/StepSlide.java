package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.function.Supplier;

public abstract class StepSlide implements Supplier<Node> {
    protected VBox mainVbox;
    protected SlideController controller;
    protected BookEventData bookEventData;

    public StepSlide(SlideController control,BookEventData bed) {
        controller = control;
        bookEventData = bed;
        mainVbox = new VBox();
    }

    public VBox getMainVBox() {
        return mainVbox;
    }

    public Node get() {
        return getMainVBox();
    }

    public void reset() {
        mainVbox.getChildren().clear();
    }
}
