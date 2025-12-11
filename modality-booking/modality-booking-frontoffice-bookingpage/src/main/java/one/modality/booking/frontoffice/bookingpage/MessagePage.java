package one.modality.booking.frontoffice.bookingpage;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public class MessagePage implements BookingFormPage {

    private final VBox container = new VBox();
    private boolean isStep = false;
    private boolean isHeaderVisible = false;
    private BookingFormButton[] buttons;

    public MessagePage(String message) {
        Label label = new Label(message);
        label.setFont(Font.font(18));
        label.setTextFill(Color.web("#333"));
        container.getChildren().add(label);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new javafx.geometry.Insets(50));
    }

    @Override
    public Object getTitleI18nKey() {
        return "Message"; // Placeholder
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        // No binding needed for simple message
    }

    @Override
    public boolean isStep() {
        return isStep;
    }

    public MessagePage setStep(boolean step) {
        isStep = step;
        return this;
    }

    @Override
    public boolean isHeaderVisible() {
        return isHeaderVisible;
    }

    public MessagePage setHeaderVisible(boolean headerVisible) {
        isHeaderVisible = headerVisible;
        return this;
    }

    @Override
    public BookingFormButton[] getButtons() {
        return buttons;
    }

    public MessagePage setButtons(BookingFormButton... buttons) {
        this.buttons = buttons;
        return this;
    }
}
