package one.modality.base.client.activity.themes;

import javafx.scene.paint.Color;
import dev.webfx.stack.ui.util.background.BackgroundFactory;
import dev.webfx.stack.ui.util.border.BorderFactory;

/**
 * @author Bruno Salmon
 */
public final class DarkTheme implements ThemeProvider {

    @Override
    public void apply() {
        Theme.setMainBackground(BackgroundFactory.newWebColorBackground("#101214"));
        Theme.setMainTextFill(Color.WHITE);
        Theme.setDialogBackground(BackgroundFactory.newBackground(Color.grayRgb(42),10));
        Theme.setDialogBorder(BorderFactory.newBorder(Color.rgb(237, 162, 57),10));
        Theme.setDialogTextFill(Color.WHITE);
    }
}
