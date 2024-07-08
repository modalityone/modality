package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;

class Step5ErrorSlide extends StepSlide{

    private final HtmlText errorMessage = new HtmlText();
    public Step5ErrorSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep5ErrorSlide(this);
    }


    public Node buildUi() {
        mainVbox.setSpacing(10);
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        Label title = new Label("An error has occurred");
        title.setPadding(new Insets(50,0,30,0));
        title.getStyleClass().addAll("book-event-primary-title", "emphasize");
        mainVbox.getChildren().add(title);
        mainVbox.getChildren().add(errorMessage);

        return mainVbox;
    }
    public void reset() {
        errorMessage.setText("");
        super.reset();
    }


    public void setErrorMessage(String errorMessageDictionaryKey) {
        this.errorMessage.setText(I18n.getI18nText(errorMessageDictionaryKey));
    }
}
