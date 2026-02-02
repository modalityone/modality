package one.modality.booking.frontoffice.bookingpage.pages.summary;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import one.modality.ecommerce.frontoffice.order.OrderDetails;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingFormI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;

/**
 * @author Bruno Salmon
 */
public class SummaryPage implements BookingFormPage {

    private final MonoPane embeddedLoginContainer = new MonoPane();
    private final TextArea requestTextArea = BookingElements.createTextArea();
    private final MonoPane summaryContainer = new MonoPane();
    private final VBox container = BookingElements.createFormPageVBox(false,
        BookingElements.createWordingLabel(BookingFormI18nKeys.SummaryTopMessage),
        BookingElements.createSecondaryWordingLabel(BookingFormI18nKeys.SummarySubTopMessage),
        summaryContainer,
        BookingElements.twoLabels(5, false,
            BookingElements.createWordingLabel(BookingFormI18nKeys.AnyRequest),
            BookingElements.createSecondaryWordingLabel(BookingFormI18nKeys.writeRequest)),
        requestTextArea,
        embeddedLoginContainer
    );
    private final BooleanProperty validProperty = new SimpleBooleanProperty();
    private WorkingBookingProperties workingBookingProperties;

    public SummaryPage() {
        summaryContainer.setMaxWidth(450);
        FXProperties.runOnPropertyChange(request -> {
            if (workingBookingProperties != null) { // actually probably never null at this point
                // Note: this will generate an AddRequestEvent on each keystroke, but it's ok because DocumentEvents
                // will simplify this and remove the previous instance as the user is typing
                workingBookingProperties.getWorkingBooking().addRequest(request);
            }
        }, requestTextArea.textProperty());
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingFormI18nKeys.Summary;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public MonoPane getEmbeddedLoginContainer() {
        return embeddedLoginContainer;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        OrderDetails orderDetails = new OrderDetails(workingBooking);
        summaryContainer.setContent(orderDetails.getView());
        validProperty.bind(workingBookingProperties.hasChangesProperty().or(workingBookingProperties.balanceProperty().greaterThan(0)));
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }
}
