package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.extras.panes.MonoPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public class CompositeBookingFormPage implements BookingFormPage {

    private final Object titleI18nKey;
    private final List<BookingFormSection> sections = new ArrayList<>();
    private final VBox container = new VBox(20); // Spacing between sections
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    private boolean isStep = true;
    private boolean isHeaderVisible = true;
    private BookingFormButton[] buttons;

    public CompositeBookingFormPage(Object titleI18nKey, BookingFormSection... sections) {
        this.titleI18nKey = titleI18nKey;
        for (BookingFormSection section : sections) {
            addSection(section);
        }
    }

    public void addSection(BookingFormSection section) {
        sections.add(section);
        container.getChildren().add(section.getView());
        // Combine validity of all sections
        section.validProperty().addListener((observable, oldValue, newValue) -> updateValidity());
        updateValidity();
    }

    private void updateValidity() {
        boolean allValid = true;
        for (BookingFormSection section : sections) {
            if (!section.isValid()) {
                allValid = false;
                break;
            }
        }
        validProperty.set(allValid);
    }

    @Override
    public Object getTitleI18nKey() {
        return titleI18nKey;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void onTransitionFinished() {
        for (BookingFormSection section : sections) {
            section.onTransitionFinished();
        }
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // If any section is applicable, the page is applicable?
        // Or should it be if ALL are applicable?
        // Or maybe we filter out non-applicable sections but still show the page if at
        // least one remains?
        // For now, let's say the page is applicable if at least one section is
        // applicable.
        for (BookingFormSection section : sections) {
            if (section.isApplicableToBooking(workingBooking)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        container.getChildren().clear();
        for (BookingFormSection section : sections) {
            if (section.isApplicableToBooking(workingBookingProperties.getWorkingBooking())) {
                section.setWorkingBookingProperties(workingBookingProperties);
                container.getChildren().add(section.getView());
            }
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    @Override
    public boolean isStep() {
        return isStep;
    }

    public CompositeBookingFormPage setStep(boolean step) {
        isStep = step;
        return this;
    }

    @Override
    public boolean isHeaderVisible() {
        return isHeaderVisible;
    }

    public CompositeBookingFormPage setHeaderVisible(boolean headerVisible) {
        isHeaderVisible = headerVisible;
        return this;
    }

    @Override
    public BookingFormButton[] getButtons() {
        return buttons;
    }

    public CompositeBookingFormPage setButtons(BookingFormButton... buttons) {
        this.buttons = buttons;
        return this;
    }
}
