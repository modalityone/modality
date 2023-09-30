package one.modality.base.client.activity.themes;

import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.border.BorderFactory;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class LightTheme implements ThemeProvider {

    @Override
    public void apply() {
        //Theme.setMainBackground(new Background(new BackgroundImage(new Image("images/theme/light/tt-white-leather.png"), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        Theme.setMainBackground(BackgroundFactory.newBackground(Color.grayRgb(0xF4), 10));
        Theme.setMainTextFill(Color.grayRgb(0x40));
        Theme.setDialogBackground(BackgroundFactory.newBackground(Color.WHITE, 10));
        //Theme.setDialogBackground(new Background(new BackgroundFill[]{new BackgroundFill(Color.WHITE, new CornerRadii(10), new Insets(3))}, new BackgroundImage[]{new BackgroundImage(new Image("images/theme/light/tt-fabric-plaid.png"), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)}));
        //Theme.setDialogBorder(new Border(new BorderStroke(LinearGradient.valueOf("#c7c7c7 0%, #d8d8d8 90%, #ccc 100%"), BorderStrokeStyle.SOLID, new CornerRadii(10), BorderStroke.THIN)));
        Theme.setDialogBorder(BorderFactory.newBorder(Color.grayRgb(0xc7), 10));
        Theme.setDialogTextFill(Color.grayRgb(0x70));
    }
}
