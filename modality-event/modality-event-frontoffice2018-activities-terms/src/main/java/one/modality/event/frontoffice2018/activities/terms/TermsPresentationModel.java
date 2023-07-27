package one.modality.event.frontoffice2018.activities.terms;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.ecommerce.client2018.activity.bookingprocess.BookingProcessPresentationModel;
import dev.webfx.extras.visual.VisualResult;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationModel extends BookingProcessPresentationModel {

    // Display output

    private final Property<VisualResult> termsLetterVisualResultProperty = new SimpleObjectProperty<>();
    Property<VisualResult> termsLetterVisualResultProperty() { return termsLetterVisualResultProperty; }

}
