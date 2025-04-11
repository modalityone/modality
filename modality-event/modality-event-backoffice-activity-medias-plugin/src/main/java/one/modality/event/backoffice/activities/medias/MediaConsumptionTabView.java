package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;

/**
 * @author Bruno Salmon
 */
final class MediaConsumptionTabView {

    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final VisualGrid visualGrid = new VisualGrid();
    private final CheckBox limitCheckbox = new CheckBox("Limit to 100");
    private final BorderPane container = new BorderPane(visualGrid, null, null, limitCheckbox, null);

    Node buildContainer() {
        limitCheckbox.setSelected(true);
        return container;
    }

    void startLogic(DataSourceModel dataSourceModel) {
        ReactiveVisualMapper.createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'MediaConsumption', orderBy: 'date desc'}")
            .setEntityColumns("""
                [
                {expression: 'attendance.documentLine.document.event', prefWidth: 120},
                'attendance.documentLine.document',
                {expression: 'attendance.documentLine.document.person_country', prefWidth: 150},
                'date',
                {expression: 'action', textAlign: 'center'},
                'mediaInfo',
                {expression: 'durationMillis', label: 'Duration', renderer: 'durationMillisRenderer', prefWidth: 70}
                ]""")
            .ifTrue(limitCheckbox.selectedProperty(), DqlStatement.limit("100"))
            .bindActivePropertyTo(activeProperty)
            .visualizeResultInto(visualGrid)
            .start();
    }

    void setActive(boolean active) {
        activeProperty.set(active);
    }

}
