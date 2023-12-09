package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.RatioPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;

/**
 * @author Bruno Salmon
 */
final class OrangeFrame {

    public static VBox createOrangeFrame(String headerI18nKey, Node center, Node bottom) {
        RatioPane ratioPane = new RatioPane(16d / 9, center);
        VBox blueFrame = new VBox(20,
                I18n.bindI18nProperties(TextUtility.getMainText(null, StyleUtility.RUPAVAJRA_WHITE), headerI18nKey),
                ratioPane,
                bottom
        );

        blueFrame.setBackground(Background.fill(Color.web(StyleUtility.MAIN_ORANGE)));
        blueFrame.setAlignment(Pos.CENTER);

        FXProperties.runOnPropertiesChange(() -> {
            double width = blueFrame.getWidth();
            double space = Math.min(35, width * 0.03);
            blueFrame.setSpacing(space);
            blueFrame.setPadding(new Insets(space));
            double screenHeight = Screen.getPrimary().getBounds().getHeight();
            ratioPane.setRatio(Math.max(1.5, width / (screenHeight * 0.6)));
        }, blueFrame.widthProperty());

        return blueFrame;
    }

}
