package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class Step5ErrorSlide extends StepSlide{

    private HtmlText errorMessage = new HtmlText();
    private Button cancelRegistrationButton = I18nControls.bindI18nProperties(new Button(), "CancelRegistration");;
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
        //We manage the property of the button in css
        cancelRegistrationButton.getStyleClass().addAll("event-button", "danger-button");
        cancelRegistrationButton.setMaxWidth(250);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(20, 20);
        progressIndicator.setStyle("-fx-progress-color: white;");
        errorMessage.setPadding(new Insets(0,0,20,0));
        cancelRegistrationButton.setOnAction(event -> {
            cancelRegistrationButton.graphicProperty().unbind();
            cancelRegistrationButton.setGraphic(progressIndicator);
            bookEventData.getCurrentBooking().cancelBooking();
            bookEventData.getCurrentBooking().submitChanges("Online User Cancellation")
                    .onFailure(result -> Platform.runLater(() -> {
                        controller.displayErrorMessage("ErrorWhileCancelingTheBooking");
                        Console.log(result);
                    }))
                    .onSuccess(result -> Platform.runLater(() -> {
                        I18nControls.bindI18nProperties(cancelRegistrationButton, "CancelRegistration");
                        setErrorMessage("RegistrationCanceled",false);
                    }));
        });
        cancelRegistrationButton.setVisible(false);
        mainVbox.getChildren().add(cancelRegistrationButton);
        return mainVbox;
    }
    public void reset() {
        errorMessage.setText("");
        super.reset();
    }


    public void setErrorMessage(String errorMessageDictionaryKey,boolean doWeDisplayTheButtonToCancelRegistration) {
        this.errorMessage.setText(I18n.getI18nText(errorMessageDictionaryKey));
        cancelRegistrationButton.setVisible(doWeDisplayTheButtonToCancelRegistration);
    }
}
