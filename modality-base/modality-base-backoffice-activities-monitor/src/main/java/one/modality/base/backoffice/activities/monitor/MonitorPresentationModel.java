package one.modality.base.backoffice.activities.monitor;

import dev.webfx.extras.visual.VisualResult;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
final class MonitorPresentationModel {

    private final Property<VisualResult> memoryVisualResultProperty = new SimpleObjectProperty<>();

    Property<VisualResult> memoryVisualResultProperty() {
        return memoryVisualResultProperty;
    }

    private final Property<VisualResult> cpuVisualResultProperty = new SimpleObjectProperty<>();

    Property<VisualResult> cpuVisualResultProperty() {
        return cpuVisualResultProperty;
    }
}
