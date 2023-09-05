package one.modality.event.frontoffice.activities.booking.steps;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;

public class BookingStep10Requests {
    public static BookingStep createPage(VBox container, Button tab) {
        VBox page = new VBox();
        BookingStep step = new BookingStep(container, page, tab, "Requests");

        Button back = new Button("Back");
        Button next = new Button("Next");

        back.setOnAction(e -> step.back());

        page.getChildren().addAll(
                new Text(step.getName()),
                GeneralUtility.createSplitRow(back, new Button(), 50, 0)
        );

        return step;
    }
}
