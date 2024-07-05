package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
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

    protected static void turnOnButtonWaitMode(Button button) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20); // Note setMaxSize() doesn't work with WebFX but setPrefSize() does
        progressIndicator.setStyle("-fx-progress-color: white;"); // Works only on OpenJFX TODO: move this to JavaFX CSS
        FXProperties.setEvenIfBound(button.graphicProperty(), progressIndicator);
        FXProperties.setIfNotBound(button.disableProperty(), true);
    }

    protected static void turnOffButtonWaitMode(Button button, String i18nKey) {
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
        FXProperties.setIfNotBound(button.disableProperty(), false);
    }
}
