package org.modality_project.all.backoffice.activities.event.clone.openjfx;

import javafx.scene.control.DatePicker;
import org.modality_project.base.shared.domainmodel.formatters.DateFormatter;
import org.modality_project.event.backoffice.activities.cloneevent.CloneEventPresentationModel;
import org.modality_project.event.backoffice.activities.cloneevent.CloneEventPresentationViewActivity;

/**
 * @author Bruno Salmon
 */
final class FxCloneEventPresentationViewActivity extends CloneEventPresentationViewActivity {

    @Override
    protected void createViewNodes(CloneEventPresentationModel pm) {
        super.createViewNodes(pm);
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(150d);
        gp.getChildren().remove(dateTextField);
        gp.add(datePicker, 1, 1);
        datePicker.valueProperty().bindBidirectional(pm.dateProperty());
        datePicker.setConverter(DateFormatter.SINGLETON.toStringConverter());
    }

}
