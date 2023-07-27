package one.modality.base.backoffice2018.activities.monitor;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import dev.webfx.extras.visual.VisualResult;

/**
 * @author Bruno Salmon
 */
final class MonitorPresentationModel {

    private final Property<VisualResult> memoryVisualResultProperty = new SimpleObjectProperty<>();
    Property<VisualResult> memoryVisualResultProperty() { return memoryVisualResultProperty; }

    private final Property<VisualResult> cpuVisualResultProperty = new SimpleObjectProperty<>();
    Property<VisualResult> cpuVisualResultProperty() { return cpuVisualResultProperty; }
}
