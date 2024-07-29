package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;

final class Step7ErrorSlide extends StepSlide {

    private final HtmlText errorMessage = Bootstrap.h5(new HtmlText());

    Step7ErrorSlide(SlideController control) {
        super(control);
    }

    Node buildUi() {
        mainVbox.setSpacing(10);
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        Label title = Bootstrap.textDanger(Bootstrap.h3(new Label("An error has occurred")));
        title.setWrapText(true);
        title.setPadding(new Insets(50,0,30,0));

        mainVbox.getChildren().setAll(title, errorMessage);

        return mainVbox;
    }

    public void reset() {
        errorMessage.setText("");
        super.reset();
    }

    public void setErrorMessage(String errorMessageI18nKey) {
        this.errorMessage.setText(I18n.getI18nText(errorMessageI18nKey));
    }
}
