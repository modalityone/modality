package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;

final class Step6CancellationSlide extends StepSlide {

    private final HtmlText cancellationMessage = Bootstrap.h5(new HtmlText());

    Step6CancellationSlide(SlideController control) {
        super(control);
    }

    Node buildUi() {
        mainVbox.setSpacing(10);
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        Label title = Bootstrap.textWarning(Bootstrap.h3(new Label("Cancellation")));
        title.setWrapText(true);
        title.setPadding(new Insets(50,0,30,0));
        I18n.bindI18nTextProperty(cancellationMessage.textProperty(), "ErrorUserCanceledPayment");

        mainVbox.getChildren().setAll(title, cancellationMessage);

        return mainVbox;
    }

}
