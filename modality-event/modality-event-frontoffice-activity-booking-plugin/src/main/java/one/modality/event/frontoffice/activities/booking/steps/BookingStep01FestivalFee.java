package one.modality.event.frontoffice.activities.booking.steps;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;

public class BookingStep01FestivalFee {
    public static BookingStep createPage(VBox container, Button tab) {
        VBox page = new VBox();
        BookingStep step = new BookingStep(container, page, tab, "Festival Fee");

        Button next = new Button("Next");

        next.setOnAction(e -> step.next());

        page.getChildren().addAll(
                new Text(step.getName()),
                GeneralUtility.createSplitRow(new Button(), next, 50, 0)
        );

        return step;
    }
}