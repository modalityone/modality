package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
        OperationUtil.turnOnButtonsWaitMode(button);
    }

    protected static void turnOffButtonWaitMode(Button button, String i18nKey) {
        OperationUtil.turnOffButtonsWaitMode(button); // but this doesn't reestablish the possible i18n graphic
        // So we reestablish it using i18n
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
    }
}
