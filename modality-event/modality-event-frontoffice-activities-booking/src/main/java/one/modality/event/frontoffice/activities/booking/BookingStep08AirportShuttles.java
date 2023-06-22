package one.modality.event.frontoffice.activities.booking;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.Utility;

public class BookingStep08AirportShuttles {
    public static BookingStep createPage(VBox container, Button tab) {
        VBox page = new VBox();
        BookingStep step = new BookingStep(container, page, tab, "Airport Shuttles");

        Button back = new Button("Back");
        Button next = new Button("Next");

        back.setOnAction(e -> step.back());
        next.setOnAction(e -> step.next());

        page.getChildren().addAll(
                new Text(step.getName()),
                Utility.createSplitRow(back, next, 50, 0)
        );

        return step;
    }
}