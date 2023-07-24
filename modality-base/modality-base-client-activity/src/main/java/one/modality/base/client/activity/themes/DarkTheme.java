package one.modality.base.client.activity.themes;

import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.border.BorderFactory;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class DarkTheme implements ThemeProvider {

  @Override
  public void apply() {
    Theme.setMainBackground(BackgroundFactory.newWebColorBackground("#101214"));
    Theme.setMainTextFill(Color.WHITE);
    Theme.setDialogBackground(BackgroundFactory.newBackground(Color.grayRgb(42), 10));
    Theme.setDialogBorder(BorderFactory.newBorder(Color.rgb(237, 162, 57), 10));
    Theme.setDialogTextFill(Color.WHITE);
  }
}
