package one.modality.base.client.time;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.util.Numbers;
import javafx.scene.text.Text;

import java.time.Duration;

/**
 * @author Bruno Salmon
 */
public class TimeRenderersRegistererJob implements ApplicationJob {

    @Override
    public void onInit() {
        ValueRendererRegistry.registerValueRenderer("durationMillisRenderer", (value, context) -> {
            Long durationMillis = Numbers.toLong(value);
            if (durationMillis == null)
                return null;
            Duration duration = Duration.ofMillis(durationMillis);
            int seconds = duration.toSecondsPart();
            int minutes = duration.toMinutesPart();
            int hours = duration.toHoursPart();
            String durationText = Numbers.twoDigits(minutes) + ":" + Numbers.twoDigits(seconds);
            if (hours > 0) { // we need to display hours
                durationText = hours + ":" + durationText;
            }
            return new Text(durationText);
        });
    }
}
