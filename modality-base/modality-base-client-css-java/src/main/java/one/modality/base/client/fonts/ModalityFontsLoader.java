package one.modality.base.client.fonts;

import dev.webfx.platform.boot.spi.ApplicationJob;
import javafx.scene.text.Font;

/**
 * @author Bruno Salmon
 */
public final class ModalityFontsLoader implements ApplicationJob {

  @Override
  public void onStart() {
    String[] files = {
      "Montserrat-Bold.ttf",
      "Montserrat-BoldItalic.ttf",
      "Montserrat-Italic.ttf",
      "Montserrat-Regular.ttf",
      "Roboto-Bold.ttf",
      "Roboto-BoldItalic.ttf",
      "Roboto-Italic.ttf",
      "Roboto-Regular.ttf",
    };
    for (String file : files) Font.loadFont(getClass().getResourceAsStream(file), 16);
  }
}
