package one.modality.base.client.activities.console;

import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityBase;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

/**
 * @author Bruno Salmon
 */
public final class ConsoleActivity extends ViewActivityBase<ViewActivityContextFinal> {

    private static final Font FONT = new Font("Courier", 12);

    private Scheduled textAreaUpdateScheduled;

    @Override
    public Node buildUi() {
        TextArea textArea = new TextArea();
        textArea.setFont(FONT);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        BufferedConsoleProvider.setListener(() -> {
            if (textAreaUpdateScheduled == null || textAreaUpdateScheduled.isFinished()) {
                textAreaUpdateScheduled = UiScheduler.scheduleDeferred(() -> {
                            textArea.setText(BufferedConsoleProvider.getBufferContent());
                            textArea.setScrollTop(Double.MAX_VALUE);
                        }
                );
            }
        });

        return textArea;
    }

}
