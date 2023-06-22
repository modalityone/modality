package one.modality.event.frontoffice.activities.booking;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class BookingStep {
    private BookingStep back = null;
    private BookingStep next = null;
    private Node page = null;
    private Button tab = null;

    private VBox container = null;
    private String name = null;

    public BookingStep(VBox container, Node page, Button tab, String name) {
        this.container = container;
        this.page = page;
        this.tab = tab;
        this.name = name;
        this.tab.textProperty().set(name);
    }

    public void setBack(BookingStep back) {
        this.back = back;
    }

    public void setNext(BookingStep next) {
        this.next = next;
    }

    public void go(BookingStep step) {
        this.container.getChildren().add(step.page);
        this.container.getChildren().remove(this.page);

        BookingStepAll.notLastStep.set(step.name != "Requests");
        step.beingSelectedFrom(this);
    }

    public void next() {go(next);}
    public void back() {go(back);}

    public Node getPage() {
        return page;
    }

    public void beingSelectedFrom(BookingStep step) {
        if (step != null) step.tab.textProperty().set(step.name);
        this.tab.textProperty().set(this.name + " selected");
    }

    public String getName() {
        return name;
    }
}
