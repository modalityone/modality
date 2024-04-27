package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.stack.i18n.I18n;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventState;

/**
 * @author Bruno Salmon
 */
final class EventRenderers {

    static {
        ValueRendererRegistry.registerValueRenderer("eventStateRenderer", (value, context) -> {
            EventState state = EventState.of((String) value);
            Text toReturn = new Text();
            if(state==null) {
                I18n.bindI18nProperties(toReturn, "NOT_DEFINED");
                toReturn.getStyleClass().add("font-grey");
                return toReturn;
            }
            I18n.bindI18nProperties(toReturn, state.toString());
            switch (state) {
                case DRAFT:
                case OPENABLE:
                    toReturn.getStyleClass().add("font-orange");
                    break;
                case OPEN:
                    toReturn.getStyleClass().add("font-green");
                    break;
                case CLOSED:
                case ARCHIVED:
                case RECONCILIED:
                case RESTRICTED:
                case FINALISED:
                    toReturn.getStyleClass().add("font-grey");
                    break;
                case ON_HOLD:
                    toReturn.getStyleClass().add("font-orange");
                    break;
                default:
                    toReturn.getStyleClass().add("font-orange");

            }
            return toReturn;
        });

        ValueRendererRegistry.registerValueRenderer("editEventRenderer", (value, context) -> {
            Event event = (Event) value;
            Hyperlink editHyperLink = new Hyperlink("Edit");
            editHyperLink.setOnAction(e -> {
                //   displayEventDetails(event);
            });
            SVGPath trashSVGPath = SvgIcons.createTrashSVGPath();
            trashSVGPath.getStyleClass().add("font-red");
            trashSVGPath.setOnMouseClicked(e ->  {
                System.out.println(e.toString());
            });
            HBox line = new HBox(editHyperLink);
            return line;
        });

    }

    static void registerRenderers() {
        // done in the static initializer (needs to be done only once)
    }

}
