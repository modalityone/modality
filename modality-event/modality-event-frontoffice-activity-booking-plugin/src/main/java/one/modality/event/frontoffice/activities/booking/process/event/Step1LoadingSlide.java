package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;

public class Step1LoadingSlide extends StepSlide{


    public Step1LoadingSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep1LoadingSlide(this);
    }

    public Node buildUi() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        mainVbox.setSpacing(10);
        mainVbox.setAlignment(Pos.CENTER);
        mainVbox.getChildren().add(progressIndicator);
        return mainVbox;
    }
}
