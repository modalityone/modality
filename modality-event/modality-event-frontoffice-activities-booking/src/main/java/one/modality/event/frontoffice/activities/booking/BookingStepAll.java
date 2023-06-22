package one.modality.event.frontoffice.activities.booking;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BookingStepAll {
    static BooleanProperty notLastStep = new SimpleBooleanProperty(true);
    public static BookingStep step1 = null;
    public static BookingStep step10 = null;
    public static Node createPage(BookingActivity bookingActivity) {
        BorderPane container = new BorderPane();
        VBox stepHolder = new VBox();
        VBox stepSummary = new VBox();
        container.setCenter(stepHolder);
        container.setBottom(stepSummary);

        GridPane gp = new GridPane();
        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(10);
            gp.getColumnConstraints().add(c);

            Button b = new Button();
            gp.add(b, i,0);
            buttons.add(b);
        }

        step1 = BookingStep01FestivalFee.createPage(stepHolder, buttons.get(0));
        BookingStep step2 = BookingStep02Accomodation.createPage(stepHolder, buttons.get(1));
        BookingStep step3 = BookingStep03Meals.createPage(stepHolder, buttons.get(2));
        BookingStep step4 = BookingStep04Translation.createPage(stepHolder, buttons.get(3));
        BookingStep step5 = BookingStep05Recordings.createPage(stepHolder, buttons.get(4));
        BookingStep step6 = BookingStep06Parking.createPage(stepHolder, buttons.get(5));
        BookingStep step7 = BookingStep07EarlyLate.createPage(stepHolder, buttons.get(6));
        BookingStep step8 = BookingStep08AirportShuttles.createPage(stepHolder, buttons.get(7));
        BookingStep step9 = BookingStep09SpecialNeeds.createPage(stepHolder, buttons.get(8));
        step10 = BookingStep10Requests.createPage(stepHolder, buttons.get(9));

        step1.beingSelectedFrom(null);
        step1.setNext(step2);
        step2.setBack(step1);
        step2.setNext(step3);
        step3.setBack(step2);
        step3.setNext(step4);
        step4.setBack(step3);
        step4.setNext(step5);
        step5.setBack(step4);
        step5.setNext(step6);
        step6.setBack(step5);
        step6.setNext(step7);
        step7.setBack(step6);
        step7.setNext(step8);
        step8.setBack(step7);
        step8.setNext(step9);
        step9.setBack(step8);
        step9.setNext(step10);
        step10.setBack(step9);

        stepHolder.getChildren().addAll(
                gp,
                step1.getPage()
        );

        Button confirm = new Button("Confirm");

        confirm.disableProperty().bind(notLastStep);

        stepSummary.setMinHeight(200);
        stepSummary.getChildren().addAll(
                new Text("Booking Summary"),
                confirm
        );

        confirm.setOnAction(e -> bookingActivity.goToBookingConfirmed());

        return container;
    }
}
