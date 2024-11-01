package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.I18n;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
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
            if (state == null) {
                return Bootstrap.textSecondary(I18n.newText(RecurringEventsI18nKeys.NOT_DEFINED));
            }
            Text toReturn = I18n.newText(state.toString());
            switch (state) {
                case DRAFT:
                case OPENABLE:
                    return Bootstrap.textWarning(toReturn);
                case OPEN:
                    return Bootstrap.textSuccess(toReturn);
                case CLOSED:
                case ARCHIVED:
                case RECONCILIED:
                case RESTRICTED:
                case FINALISED:
                    return Bootstrap.textSecondary(toReturn);
                case ON_HOLD:
                    return Bootstrap.textWarning(toReturn);
                default:
                    return Bootstrap.textWarning(toReturn);
            }
        });

        // editEventRenderer
        ValueRendererRegistry.registerValueRenderer("editEventRenderer", (value, context) -> {
            Hyperlink editHyperLink = new Hyperlink("Edit");
            editHyperLink.setOnAction(e -> {
                //   displayEventDetails(event);
            });
            return new MonoPane(editHyperLink);
        });

        // confirmRenderer (used for the last column of the bookings displayed in RecurringEventAttendanceView)
        ValueRendererRegistry.registerValueRenderer("confirmRenderer", (value, context) -> {
            Document document = (Document) value;
            Text confirmText = I18n.newText(document.isConfirmed() ? "BookingConfirmed" : "BookingUnconfirmed");
            confirmText.getStyleClass().add(document.isConfirmed() ? "booking-status-confirmed" : "booking-status-unconfirmed");
            return confirmText;
        });

    }

    static void registerRenderers() {
        // done in the static initializer (needs to be done only once)
    }

}
