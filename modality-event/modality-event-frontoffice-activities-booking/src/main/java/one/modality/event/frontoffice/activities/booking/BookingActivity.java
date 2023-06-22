package one.modality.event.frontoffice.activities.booking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class BookingActivity extends ViewDomainActivityBase {
    private Node activePage = null;
    private VBox container = new VBox();
    private VBox bookingWelcome = new VBox();
    private Node bookingSteps = BookingStepAll.createPage(this);
    private VBox bookingConfirmed = new VBox();

    @Override
    public Node buildUi() {
        Button startBooking = new Button("Start Booking");
        bookingWelcome.getChildren().addAll(
                startBooking
        );

        startBooking.setOnAction(e -> {
            container.getChildren().remove(bookingWelcome);
            container.getChildren().add(bookingSteps);
        });

        Button restartBooking = new Button("Restart");

        restartBooking.setOnAction(e -> {
            container.getChildren().remove(bookingConfirmed);
            container.getChildren().add(bookingWelcome);
            BookingStepAll.step10.go(BookingStepAll.step1);
        });

        bookingConfirmed.getChildren().addAll(
                new Text("How wonderful! Let's Festival!"),
                restartBooking
        );

        container.getChildren().add(bookingWelcome);

        return container;
    }

    public void goToBookingConfirmed() {
        container.getChildren().remove(bookingSteps);
        container.getChildren().add(bookingConfirmed);
    }
}
