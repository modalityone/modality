package one.modality.base.client.entities;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.util.Strings;
import javafx.scene.control.Label;
import one.modality.base.client.entities.functions.DateIntervalFormat;
import one.modality.base.client.entities.functions.I18nFunction;

/**
 * @author Bruno Salmon
 */
public class ClientEntitiesAdditionalRegisteringJob implements ApplicationJob {

    @Override
    public void onInit() {
        // Registering functions
        new I18nFunction().register();
        new DateIntervalFormat().register();

        // Registering value renderers
        ValueRendererRegistry.registerValueRenderer("ellipsisLabel", (name, context) -> {
            Label label = new Label(Strings.toString(name));
            label.setWrapText(true);
            label.getStyleClass().add("ellipsis");
            return label;
        });
    }
}
