package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class Step5ErrorSlide extends StepSlide{

    private Text errorMessage = new Text();

    public Step5ErrorSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep5ErrorSlide(this);
    }


    public Node buildUi() {
        mainVbox.setSpacing(10);
        mainVbox.setAlignment(Pos.CENTER);
        mainVbox.getChildren().addAll(errorMessage);//,imageView);
        return mainVbox;
    }
    public void reset() {
        errorMessage.setText("");
        super.reset();
    }


    public void setErrorMessage(String errorMessage) {
        this.errorMessage.setText(errorMessage);
    }
}
