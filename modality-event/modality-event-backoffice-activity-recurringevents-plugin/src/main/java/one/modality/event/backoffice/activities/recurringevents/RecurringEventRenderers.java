package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.stack.i18n.I18n;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.EventState;

/**
 * @author Bruno Salmon
 */
final class RecurringEventRenderers {

    static {
        // eventStateRenderer
        ValueRendererRegistry.registerValueRenderer("eventStateRenderer", (value, context) -> {
            EventState state = EventState.of((String) value);
            Text toReturn = new Text();
            if (state == null) {
                I18n.bindI18nProperties(toReturn, "NOT_DEFINED");
                toReturn.getStyleClass().add("secondary-text");
                return toReturn;
            }
            I18n.bindI18nProperties(toReturn, state.toString());
            switch (state) {
                case DRAFT:
                case OPENABLE:
                    toReturn.getStyleClass().add("warning-text");
                    break;
                case OPEN:
                    toReturn.getStyleClass().add("success-text");
                    break;
                case CLOSED:
                case ARCHIVED:
                case RECONCILIED:
                case RESTRICTED:
                case FINALISED:
                    toReturn.getStyleClass().add("secondary-text");
                    break;
                case ON_HOLD:
                    toReturn.getStyleClass().add("warning-text");
                    break;
                default:
                    toReturn.getStyleClass().add("warning-text");
                    break;
            }
            return toReturn;
        });

        // editEventRenderer
        ValueRendererRegistry.registerValueRenderer("editEventRenderer", (value, context) -> {
            Hyperlink editHyperLink = new Hyperlink("Edit");
            editHyperLink.setOnAction(e -> {
                //   displayEventDetails(event);
            });
            SVGPath trashSVGPath = SvgIcons.createTrashSVGPath();
            trashSVGPath.getStyleClass().add("danger-text");
            trashSVGPath.setOnMouseClicked(e -> System.out.println(e.toString()));
            return new HBox(editHyperLink);
        });

        // confirmRenderer (used for the last column of the bookings displayed in RecurringEventAttendanceView)
        ValueRendererRegistry.registerValueRenderer("confirmRenderer", (value, context) -> {
            Document document = (Document) value;
            Text confirmText = I18n.bindI18nProperties(new Text(), document.isConfirmed() ? "BookingConfirmed" : "BookingUnconfirmed");
            confirmText.getStyleClass().add(document.isConfirmed() ? "booking-status-confirmed" : "booking-status-unconfirmed");
            return confirmText;
        });

    }

    static void registerRenderers() {
        // done in the static initializer (needs to be done only once)
    }

}
