package one.modality.event.frontoffice.activities.terms;

import dev.webfx.extras.visual.VisualResult;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.ecommerce.client.activity.bookingprocess.BookingProcessPresentationModel;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationModel extends BookingProcessPresentationModel {

  // Display output

  private final Property<VisualResult> termsLetterVisualResultProperty =
      new SimpleObjectProperty<>();

  Property<VisualResult> termsLetterVisualResultProperty() {
    return termsLetterVisualResultProperty;
  }
}
