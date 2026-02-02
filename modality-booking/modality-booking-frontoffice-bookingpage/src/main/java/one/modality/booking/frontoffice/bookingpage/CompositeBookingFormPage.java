package one.modality.booking.frontoffice.bookingpage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
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
    private final VBox container = new VBox(32); // Spacing between sections (per JSX mockup)
    { container.setMinWidth(0); } // Allow shrinking for responsive design
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty canGoBackProperty = new SimpleBooleanProperty(true);
    private boolean isStep = true;
    private boolean isHeaderVisible = true;
    private boolean showingOwnSubmitButton = false;
    private BookingFormButton[] buttons;
    private Node footerContent; // Extra content shown after all sections (e.g., validation warnings)

    public CompositeBookingFormPage(Object titleI18nKey, BookingFormSection... sections) {
        this.titleI18nKey = titleI18nKey;
        container.setAlignment(Pos.TOP_CENTER);
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
        // Re-add footer content if set (e.g., validation warning zone)
        if (footerContent != null) {
            container.getChildren().add(footerContent);
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    @Override
    public ObservableBooleanValue canGoBackProperty() {
        return canGoBackProperty;
    }

    public CompositeBookingFormPage setCanGoBack(boolean canGoBack) {
        canGoBackProperty.set(canGoBack);
        return this;
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
    public boolean isShowingOwnSubmitButton() {
        return showingOwnSubmitButton;
    }

    public CompositeBookingFormPage setShowingOwnSubmitButton(boolean showingOwnSubmitButton) {
        this.showingOwnSubmitButton = showingOwnSubmitButton;
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

    /**
     * Returns the list of sections in this page.
     * Useful for finding sections implementing specific interfaces.
     */
    public List<BookingFormSection> getSections() {
        return sections;
    }

    /**
     * Sets extra content to display after all sections (e.g., validation warnings).
     * This content persists through setWorkingBookingProperties calls.
     *
     * @param footerContent The node to display after sections, or null to remove
     * @return this page for chaining
     */
    public CompositeBookingFormPage setFooterContent(Node footerContent) {
        // Remove old footer content if present
        if (this.footerContent != null) {
            container.getChildren().remove(this.footerContent);
        }
        this.footerContent = footerContent;
        // Add new footer content at the end
        if (footerContent != null && !container.getChildren().contains(footerContent)) {
            container.getChildren().add(footerContent);
        }
        return this;
    }
}
