package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.I18n;
import javafx.scene.control.Hyperlink;
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
                return Bootstrap.textSecondary(I18n.bindI18nProperties(toReturn, "NOT_DEFINED"));
            }
            I18n.bindI18nProperties(toReturn, state.toString());
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
            SVGPath trashSVGPath = SvgIcons.createTrashSVGPath();
            trashSVGPath.getStyleClass().add(Bootstrap.TEXT_DANGER);
            return new MonoPane(editHyperLink);
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
